import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError, listarDocentes } from '../../../api'
import { academicYears } from '../../../content/academicYear'
import { seedPlacements } from '../../../content/scheduleConstruction'
import {
  applyConflictStatus,
  defaultValidationGroupId,
  defaultValidationYearId,
  detectScheduleConflicts,
  filterConflicts,
  formatValidationTimestamp,
  scheduleVersionLabel,
  summarizeConflicts,
  validationClassroomOptions,
  validationGroupOptions,
  validationTeacherOptions,
  type ValidationFilters as ValidationFilterValues,
} from '../../../content/validations'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { ConflictDetailModal } from '../ConflictDetailModal'
import { ConflictsTable } from '../ConflictsTable'
import { ValidationReadyModal } from '../ValidationReadyModal'
import { ValidationFilters } from '../ValidationFilters'
import { ValidationMetrics } from '../ValidationMetrics'
import { ValidationScheduleCard } from '../ValidationScheduleCard'
import styles from './ValidationsWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function ValidationsWorkspace() {
  const navigate = useNavigate()
  const [yearId, setYearId] = useState(defaultValidationYearId)
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [catalogError, setCatalogError] = useState('')
  const [version, setVersion] = useState(1)
  const [validating, setValidating] = useState(false)
  const [resolvedIds, setResolvedIds] = useState<string[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [detectedAt, setDetectedAt] = useState(() => formatValidationTimestamp(new Date()))
  const [readyOpen, setReadyOpen] = useState(false)
  const [filters, setFilters] = useState<ValidationFilterValues>({
    severity: '',
    status: 'PENDIENTE',
    kind: '',
    groupId: defaultValidationGroupId(defaultValidationYearId()),
    teacherName: '',
    classroom: '',
  })

  useEffect(() => {
    const controller = new AbortController()

    listarDocentes({ page: 0, size: 100, estado: 'ACTIVO' }, controller.signal)
      .then((page) => {
        setTeachers(
          [...page.content].sort((left, right) =>
            formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
          ),
        )
        setCatalogError('')
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTeachers([])
        setCatalogError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudieron cargar los docentes. La validación usa los nombres del horario local.',
        )
      })

    return () => controller.abort()
  }, [])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const detected = useMemo(
    () => detectScheduleConflicts(year.id, seedPlacements, teachers),
    [year.id, teachers],
  )
  const reviewed = useMemo(() => applyConflictStatus(detected, resolvedIds), [detected, resolvedIds])
  const summary = summarizeConflicts(reviewed)
  const rows = filterConflicts(reviewed, filters)
  const selected = reviewed.find((item) => item.id === selectedId) ?? null

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setSelectedId(null)
    setFilters((current) => ({
      ...current,
      groupId: defaultValidationGroupId(nextYearId),
      teacherName: '',
      classroom: '',
    }))
  }

  const revalidate = () => {
    setValidating(true)
    window.setTimeout(() => {
      const nextResolved = resolvedIds.filter((id) => detected.some((item) => item.id === id))
      const nextSummary = summarizeConflicts(applyConflictStatus(detected, nextResolved))
      setVersion((current) => current + 1)
      setResolvedIds(nextResolved)
      setDetectedAt(formatValidationTimestamp(new Date()))
      setReadyOpen(nextSummary.readiness !== 'blocked')
      setValidating(false)
    }, 350)
  }

  const openSchedule = () => navigate('/dashboard/horario')

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Validaciones y conflictos
        </Text>
        <Text variant="body">
          Revisa los errores y advertencias detectados antes de publicar el horario.
        </Text>
      </div>

      <ValidationScheduleCard year={year} version={version} onYearChange={selectYear} />
      <ValidationMetrics {...summary} />
      <PublishBanner
        readiness={summary.readiness}
        onOpenReady={() => setReadyOpen(true)}
      />

      {catalogError ? <p className={styles.notice}>{catalogError}</p> : null}

      <ValidationFilters
        values={filters}
        groupOptions={validationGroupOptions(year.id)}
        teacherOptions={validationTeacherOptions(detected, teachers)}
        classroomOptions={validationClassroomOptions(detected)}
        validating={validating}
        onChange={(patch) => setFilters((current) => ({ ...current, ...patch }))}
        onRevalidate={revalidate}
      />

      <ConflictsTable
        rows={rows}
        emptyMessage={
          reviewed.length === 0
            ? 'Este año lectivo no tiene conflictos detectados en el horario local.'
            : 'No hay conflictos que coincidan con esos filtros.'
        }
        onDetail={(conflict) => setSelectedId(conflict.id)}
      />

      <ConflictDetailModal
        open={Boolean(selected)}
        conflict={selected}
        scheduleLabel={scheduleVersionLabel(year, version)}
        detectedAt={detectedAt}
        onClose={() => setSelectedId(null)}
        onOpenSchedule={openSchedule}
      />
      <ValidationReadyModal
        open={readyOpen}
        hasWarnings={summary.readiness === 'warnings'}
        onClose={() => setReadyOpen(false)}
        onOpenSchedule={openSchedule}
        onPreparePublish={() => navigate('/dashboard/publicacion')}
      />
    </section>
  )
}

function PublishBanner({
  readiness,
  onOpenReady,
}: {
  readiness: 'blocked' | 'warnings' | 'ready'
  onOpenReady: () => void
}) {
  if (readiness === 'blocked') {
    return (
      <div className={styles.blocked} role="status">
        <strong>No apto para publicación</strong>
        <p>Existen errores pendientes. Corrige el horario y vuelve a validar antes de continuar.</p>
      </div>
    )
  }

  if (readiness === 'warnings') {
    return (
      <button type="button" className={styles.caution} onClick={onOpenReady}>
        <strong>Apto con advertencias</strong>
        <p>No hay errores pendientes. Revisa las advertencias antes de publicar el horario.</p>
      </button>
    )
  }

  return (
    <button type="button" className={styles.ready} onClick={onOpenReady}>
      <strong>Apto para publicación</strong>
      <p>No hay errores ni advertencias pendientes en el horario validado.</p>
    </button>
  )
}
