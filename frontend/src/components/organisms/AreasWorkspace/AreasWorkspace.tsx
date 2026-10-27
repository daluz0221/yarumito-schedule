import { useEffect, useRef, useState } from 'react'
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
  const [subjectsNonce, setSubjectsNonce] = useState(0)
  const subjectLoadGeneration = useRef(0)

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
    const generation = subjectLoadGeneration.current + 1
    subjectLoadGeneration.current = generation

    listarAsignaturas(areaId, controller.signal)
      .then((subjects) => {
        if (generation !== subjectLoadGeneration.current) {
          return
        }

        setSubjectLoad({ areaId, subjects, error: '' })
      })
      .catch((cause) => {
        if (isAbortError(cause) || generation !== subjectLoadGeneration.current) {
          return
        }

        const message =
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar las asignaturas.'

        setSubjectLoad((current) => {
          const kept = current?.areaId === areaId ? current.subjects : []

          return {
            areaId,
            subjects: kept,
            error: kept.length > 0 ? '' : message,
          }
        })
      })

    return () => controller.abort()
  }, [selectedId, subjectsNonce])

  const handleAreaCreated = (area: AreaResponse) => {
    setAreas((current) =>
      current.some((item) => item.id === area.id) ? current : [...current, area],
    )
    setSelectedId(area.id)
    setQuery('')
    setStatus('')
    setError('')
  }

  const handleSubjectCreated = (subject: AsignaturaResponse) => {
    if (subject.areaId !== selectedId) {
      setSelectedId(subject.areaId)
      return
    }

    setSubjectLoad((current) => {
      const existing = current?.areaId === subject.areaId ? current.subjects : []

      if (existing.some((item) => item.id === subject.id)) {
        return { areaId: subject.areaId, subjects: existing, error: '' }
      }

      return {
        areaId: subject.areaId,
        subjects: [...existing, subject],
        error: '',
      }
    })
    setSubjectsNonce((current) => current + 1)
  }

  const handleSubjectUpdated = (subject: AsignaturaResponse) => {
    if (subject.areaId !== selectedId) {
      setSelectedId(subject.areaId)
      setSubjectsNonce((current) => current + 1)
      return
    }

    setSubjectLoad((current) => {
      const existing = current?.areaId === subject.areaId ? current.subjects : []
      const subjects = existing.some((item) => item.id === subject.id)
        ? existing.map((item) => (item.id === subject.id ? subject : item))
        : [...existing, subject]

      return { areaId: subject.areaId, subjects, error: '' }
    })
    setSubjectsNonce((current) => current + 1)
  }

  const handleSubjectMissing = (subjectId: string) => {
    setSubjectLoad((current) => {
      if (!current) {
        return current
      }

      return {
        ...current,
        subjects: current.subjects.filter((item) => item.id !== subjectId),
      }
    })
  }

  const handleAreaMissing = (areaId: string) => {
    setAreas((current) => current.filter((area) => area.id !== areaId))
    setSelectedId((current) => (current === areaId ? '' : current))
  }

  const handleAreaUpdated = (area: AreaResponse) => {
    setAreas((current) =>
      current.some((item) => item.id === area.id)
        ? current.map((item) => (item.id === area.id ? area : item))
        : [...current, area],
    )
    setSelectedId(area.id)
    setError('')
  }

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
        onAreaCreated={handleAreaCreated}
        onAreaUpdated={handleAreaUpdated}
        onAreaMissing={handleAreaMissing}
      />

      <SubjectsSection
        areas={areas}
        selectedAreaId={selectedId}
        areaName={selected?.nombre ?? null}
        subjects={subjects}
        subjectsLoading={Boolean(selectedId) && !subjectsReady}
        subjectsError={subjectLoad?.areaId === selectedId ? subjectLoad.error : ''}
        onSubjectCreated={handleSubjectCreated}
        onSubjectUpdated={handleSubjectUpdated}
        onSubjectMissing={handleSubjectMissing}
      />
    </section>
  )
}
