import { useEffect, useState } from 'react'
import { ApiError, listarAsignaturas, listarCatalogoAreas } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { AsignaturaResponse } from '../../../api/asignaturas'
import {
  filterAreas,
  type AreaStatusFilter,
} from '../../../content/areas'
import { Text } from '../../atoms/Text'
import { AcademicAreasSection } from '../AcademicAreasSection'
import { SubjectsSection } from '../SubjectsSection'
import styles from './AreasWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

type SubjectLoad = {
  areaId: string
  subjects: AsignaturaResponse[]
  error: string
}

export function AreasWorkspace() {
  const [areas, setAreas] = useState<AreaResponse[]>([])
  const [selectedId, setSelectedId] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<AreaStatusFilter>('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [subjectLoad, setSubjectLoad] = useState<SubjectLoad | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoAreas(controller.signal)
      .then((items) => {
        setAreas(items)
        setSelectedId((current) =>
          items.some((area) => area.id === current) ? current : (items[0]?.id ?? ''),
        )
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setAreas([])
        setSelectedId('')
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de áreas.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!selectedId) {
      return
    }

    const controller = new AbortController()
    const areaId = selectedId

    listarAsignaturas(areaId, controller.signal)
      .then((subjects) => {
        setSubjectLoad({ areaId, subjects, error: '' })
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setSubjectLoad({
          areaId,
          subjects: [],
          error:
            cause instanceof ApiError
              ? cause.message
              : 'No se pudo cargar las asignaturas.',
        })
      })

    return () => controller.abort()
  }, [selectedId])

  const selected = areas.find((area) => area.id === selectedId) ?? null
  const rows = filterAreas(areas, { query, status })
  const subjects = subjectLoad?.areaId === selectedId ? subjectLoad.subjects : []
  const subjectsReady = subjectLoad?.areaId === selectedId

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Áreas y asignaturas
        </Text>
        <Text variant="body">
          Administra el catálogo de áreas académicas y las asignaturas que
          conforman la estructura curricular de la institución.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <AcademicAreasSection
        areas={areas}
        rows={rows}
        selectedId={selectedId}
        selectedName={selected?.nombre ?? null}
        query={query}
        status={status}
        loading={loading}
        onQueryChange={setQuery}
        onStatusChange={setStatus}
        onSelect={setSelectedId}
      />

      <SubjectsSection
        areas={areas}
        selectedAreaId={selectedId}
        areaName={selected?.nombre ?? null}
        subjects={subjects}
        subjectsLoading={Boolean(selectedId) && !subjectsReady}
        subjectsError={subjectLoad?.areaId === selectedId ? subjectLoad.error : ''}
      />
    </section>
  )
}
