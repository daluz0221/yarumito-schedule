import { useEffect, useState } from 'react'
import { ApiError, consultarResumenDocentes, listarDocentes } from '../../../api'
import { teacherStatsFromCounts, type Teacher } from '../../../content/teachers'
import type { StatItemData } from '../../../content/dashboard'
import { StatsSection } from '../StatsSection'
import { TeachersFilters } from '../TeachersFilters'
import { TeachersTable } from '../TeachersTable'
import { TeachersToolbar } from '../TeachersToolbar'
import styles from './TeachersWorkspace.module.css'

const defaultFilters = {
  query: '',
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
  }, [debouncedQuery, filters.status, filters.contract, page, pageSize, reloadKey])

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
        status={filters.status}
        contract={filters.contract}
        onQueryChange={(query) => {
          setFilters((current) => ({ ...current, query }))
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
