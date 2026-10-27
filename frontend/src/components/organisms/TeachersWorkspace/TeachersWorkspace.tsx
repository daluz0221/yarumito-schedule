import { useEffect, useState } from 'react'
import {
  ApiError,
  consultarResumenDocentes,
  listarCatalogoAreas,
  listarDocentes,
  listarIdoneidadesPorDocente,
  listarTitulosPorDocente,
} from '../../../api'
import type { CatalogoAreasTabla } from '../TeachersTable'
import {
  resumenAcademicoDocente,
  type ResumenAcademicoDocente,
} from '../../../content/teacherProfile'
import { teacherStatsFromCounts, type Teacher } from '../../../content/teachers'
import type { StatItemData } from '../../../content/dashboard'
import { StatsSection } from '../StatsSection'
import { TeachersFilters } from '../TeachersFilters'
import { TeachersTable } from '../TeachersTable'
import { TeachersToolbar } from '../TeachersToolbar'
import styles from './TeachersWorkspace.module.css'

const defaultFilters = {
  query: '',
  area: '',
  status: '',
  contract: '',
}

const emptyStats = teacherStatsFromCounts({
  total: 0,
  active: 0,
  license: 0,
  retired: 0,
})

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function TeachersWorkspace() {
  const [filters, setFilters] = useState(defaultFilters)
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(6)
  const [rows, setRows] = useState<Teacher[]>([])
  const [summaries, setSummaries] = useState<
    Record<string, ResumenAcademicoDocente>
  >({})
  const [areas, setAreas] = useState<CatalogoAreasTabla>({ estado: 'cargando' })
  const [total, setTotal] = useState(0)
  const [stats, setStats] = useState<StatItemData[]>(emptyStats)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      setDebouncedQuery(filters.query.trim())
    }, 300)

    return () => window.clearTimeout(timeout)
  }, [filters.query])

  useEffect(() => {
    const controller = new AbortController()

    setLoading(true)
    setError('')

    listarDocentes(
      {
        texto: debouncedQuery,
        estado: filters.status,
        tipoVinculacion: filters.contract,
        areaId: filters.area,
        page: page - 1,
        size: pageSize,
      },
      controller.signal,
    )
      .then((result) => {
        const lastPage = Math.max(1, Math.ceil(result.total / pageSize) || 1)
        if (page > lastPage) {
          setPage(lastPage)
          return
        }

        setRows(result.content)
        setTotal(result.total)
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setRows([])
        setTotal(0)
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el listado de docentes.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [
    debouncedQuery,
    filters.status,
    filters.contract,
    filters.area,
    page,
    pageSize,
    reloadKey,
  ])

  useEffect(() => {
    if (rows.length === 0) {
      setSummaries({})
      return
    }

    const controller = new AbortController()
    let active = true
    setSummaries({})

    void Promise.all(
      rows.map(async (teacher) => {
        const [titlesResult, suitabilityResult] = await Promise.allSettled([
          listarTitulosPorDocente(teacher.id, controller.signal),
          listarIdoneidadesPorDocente(teacher.id, controller.signal),
        ])

        return { id: teacher.id, titlesResult, suitabilityResult }
      }),
    ).then((results) => {
      if (!active || controller.signal.aborted) {
        return
      }

      const next: Record<string, ResumenAcademicoDocente> = {}

      for (const result of results) {
        if (
          (result.titlesResult.status === 'rejected' &&
            isAbortError(result.titlesResult.reason)) ||
          (result.suitabilityResult.status === 'rejected' &&
            isAbortError(result.suitabilityResult.reason))
        ) {
          return
        }

        next[result.id] = resumenAcademicoDocente(
          result.titlesResult.status === 'fulfilled'
            ? result.titlesResult.value
            : null,
          result.suitabilityResult.status === 'fulfilled'
            ? result.suitabilityResult.value
            : null,
        )
      }

      setSummaries(next)
    })

    return () => {
      active = false
      controller.abort()
    }
  }, [rows])

  useEffect(() => {
    const controller = new AbortController()
    setAreas({ estado: 'cargando' })

    listarCatalogoAreas(controller.signal)
      .then((catalog) => {
        const porId: Record<string, string> = {}

        for (const area of catalog) {
          const nombre = area.nombre?.trim()

          if (area.id && nombre) {
            porId[area.id] = nombre
          }
        }

        setAreas({ estado: 'listo', porId })
      })
      .catch((cause) => {
        if (!isAbortError(cause)) {
          setAreas({ estado: 'error' })
        }
      })

    return () => controller.abort()
  }, [reloadKey])

  useEffect(() => {
    const controller = new AbortController()

    consultarResumenDocentes(controller.signal)
      .then(setStats)
      .catch((cause) => {
        if (!isAbortError(cause)) {
          setStats(emptyStats)
        }
      })

    return () => controller.abort()
  }, [reloadKey])

  const resetFilters = () => {
    setFilters(defaultFilters)
    setDebouncedQuery('')
    setPage(1)
  }

  const areaOptions = [
    { value: '', label: 'Todas' },
    ...(areas.estado === 'listo'
      ? Object.entries(areas.porId)
          .map(([id, nombre]) => ({ value: id, label: nombre }))
          .sort((left, right) => left.label.localeCompare(right.label, 'es'))
      : []),
  ]

  return (
    <section className={styles.workspace}>
      <TeachersToolbar
        onRefresh={() => {
          setReloadKey((current) => current + 1)
        }}
      />
      <StatsSection items={stats} label="Resumen de docentes" />
      <TeachersFilters
        query={filters.query}
        area={filters.area}
        status={filters.status}
        contract={filters.contract}
        areaOptions={areaOptions}
        areasLoading={areas.estado === 'cargando'}
        areasError={
          areas.estado === 'error' ? 'No se pudo cargar el catálogo de áreas.' : ''
        }
        onQueryChange={(query) => {
          setFilters((current) => ({ ...current, query }))
          setPage(1)
        }}
        onAreaChange={(area) => {
          setFilters((current) => ({ ...current, area }))
          setPage(1)
        }}
        onStatusChange={(status) => {
          setFilters((current) => ({ ...current, status }))
          setPage(1)
        }}
        onContractChange={(contract) => {
          setFilters((current) => ({ ...current, contract }))
          setPage(1)
        }}
        onClear={resetFilters}
      />
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      <TeachersTable
        rows={rows}
        summaries={summaries}
        areas={areas}
        page={page}
        pageSize={pageSize}
        total={total}
        emptyMessage={
          loading
            ? 'Cargando docentes...'
            : 'No se encontraron docentes con esos filtros.'
        }
        onPageChange={setPage}
        onPageSizeChange={(nextSize) => {
          setPageSize(nextSize)
          setPage(1)
        }}
      />
    </section>
  )
}
