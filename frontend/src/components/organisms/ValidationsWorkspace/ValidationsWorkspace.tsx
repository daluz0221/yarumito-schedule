import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  aConflictoRevisado,
  anioEscolarVisible,
  ApiError,
  consultarHorarioGrupo,
  listarConflictosPendientes,
  listarConflictosPorHorario,
  listarConflictosPorSeveridad,
  marcarConflictoResuelto,
  opcionesAniosEscolares,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
  type ConflictoVista,
  type EstadoHorario,
  type HorarioGrupoResponse,
  type SeveridadConflicto,
} from '../../../api'
import {
  filterConflicts,
  summarizeConflicts,
  type ValidationFilters as ValidationFilterValues,
} from '../../../content/validations'
import type { BadgeTone } from '../../atoms/Badge'
import { Text } from '../../atoms/Text'
import { ConflictDetailModal } from '../ConflictDetailModal'
import { ConflictsTable } from '../ConflictsTable'
import { ValidationReadyModal } from '../ValidationReadyModal'
import { ValidationFilters } from '../ValidationFilters'
import { ValidationMetrics } from '../ValidationMetrics'
import { ValidationScheduleCard } from '../ValidationScheduleCard'
import styles from './ValidationsWorkspace.module.css'

const tiposConflicto = [
  { value: '', label: 'Todos' },
  { value: 'teacher-busy', label: 'Docente ocupado' },
  { value: 'classroom-busy', label: 'Aula ocupada' },
  { value: 'capacity', label: 'Sobreaforo' },
]

const filtroDocente = [{ value: '', label: 'Todos' }]
const filtroAula = [{ value: '', label: 'Todas' }]

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback
}

function tonoEstado(estado: EstadoHorario | null | undefined): BadgeTone {
  if (estado === 'PUBLICADO') {
    return 'success'
  }

  if (estado === 'ARCHIVADO') {
    return 'neutral'
  }

  if (estado === 'BORRADOR') {
    return 'warning'
  }

  return 'muted'
}

function etiquetaEstado(estado: EstadoHorario | null | undefined) {
  if (estado === 'PUBLICADO') {
    return 'Publicado'
  }

  if (estado === 'ARCHIVADO') {
    return 'Archivado'
  }

  if (estado === 'BORRADOR') {
    return 'Borrador'
  }

  return 'Sin horario'
}

function consultaDe(filters: Pick<ValidationFilterValues, 'severity' | 'status'>) {
  if (filters.severity === 'ERROR' || filters.severity === 'ADVERTENCIA') {
    return 'severidad' as const
  }

  if (filters.status === 'PENDIENTE') {
    return 'pendientes' as const
  }

  return 'todos' as const
}

function revisar(items: Awaited<ReturnType<typeof listarConflictosPorHorario>>, yearId: string) {
  return items.flatMap((item) => {
    const conflict = aConflictoRevisado(item, yearId)
    return conflict ? [conflict] : []
  })
}

export function ValidationsWorkspace() {
  const navigate = useNavigate()
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos()
  const [yearIdState, setYearIdState] = useState('')
  const [groupIdState, setGroupIdState] = useState('')
  const [tablero, setTablero] = useState<HorarioGrupoResponse | null>(null)
  const [horarioCargando, setHorarioCargando] = useState(false)
  const [horarioError, setHorarioError] = useState('')
  const [conflictoError, setConflictoError] = useState('')
  const [cargando, setCargando] = useState(false)
  const [todos, setTodos] = useState<ConflictoVista[]>([])
  const [visibles, setVisibles] = useState<ConflictoVista[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [readyOpen, setReadyOpen] = useState(false)
  const [resolviendo, setResolviendo] = useState(false)
  const [resolverError, setResolverError] = useState('')
  const [revision, setRevision] = useState(0)
  const [filters, setFilters] = useState<Omit<ValidationFilterValues, 'groupId'>>({
    severity: '',
    status: 'PENDIENTE',
    kind: '',
    teacherName: '',
    classroom: '',
  })

  const yearId = anios?.some((year) => year.id === yearIdState)
    ? yearIdState
    : (anioEscolarVisible(anios ?? [])?.id ?? '')
  const year = anios?.find((item) => item.id === yearId) ?? null
  const gruposDelAnio = useMemo(
    () => (grupos ?? []).filter((group) => group.anioEscolarId === yearId && group.activo),
    [grupos, yearId],
  )
  const groupId = gruposDelAnio.some((group) => group.id === groupIdState)
    ? groupIdState
    : (gruposDelAnio[0]?.id ?? '')
  const grupo = gruposDelAnio.find((group) => group.id === groupId) ?? null
  const horarioId = tablero?.grupoId === groupId ? (tablero.horarioId ?? '') : ''
  const horarioClave = `${groupId}:${revision}`
  const [horarioVisto, setHorarioVisto] = useState(horarioClave)

  if (horarioVisto !== horarioClave) {
    setHorarioVisto(horarioClave)
    setHorarioError('')

    if (!groupId) {
      setTablero(null)
      setHorarioCargando(false)
    } else {
      setHorarioCargando(true)
    }
  }

  useEffect(() => {
    if (!groupId) {
      return
    }

    const controller = new AbortController()

    consultarHorarioGrupo(groupId, controller.signal)
      .then((siguiente) => {
        setTablero(siguiente)
        setHorarioError('')
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTablero(null)
        setHorarioError(mensajeError(cause, 'No se pudo cargar el horario de este grupo.'))
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setHorarioCargando(false)
        }
      })

    return () => controller.abort()
  }, [groupId, revision])

  const conflictoClave = `${horarioId}:${filters.severity}:${filters.status}:${revision}:${yearId}`
  const [conflictoVisto, setConflictoVisto] = useState(conflictoClave)

  if (conflictoVisto !== conflictoClave) {
    setConflictoVisto(conflictoClave)
    setConflictoError('')
    setVisibles([])

    if (!horarioId) {
      setTodos([])
      setCargando(false)
    } else {
      setCargando(true)
    }
  }

  const seleccionClave = `${groupId}:${yearId}`
  const [seleccionVista, setSeleccionVista] = useState(seleccionClave)

  if (seleccionVista !== seleccionClave) {
    setSeleccionVista(seleccionClave)
    setSelectedId(null)
    setResolverError('')
  }

  useEffect(() => {
    if (!horarioId) {
      return
    }

    const controller = new AbortController()
    const modo = consultaDe({ severity: filters.severity, status: filters.status })
    const especificos =
      modo === 'pendientes'
        ? listarConflictosPendientes(horarioId, controller.signal)
        : modo === 'severidad'
          ? listarConflictosPorSeveridad(
              horarioId,
              filters.severity as SeveridadConflicto,
              controller.signal,
            )
          : null

    Promise.all([
      listarConflictosPorHorario(horarioId, controller.signal),
      especificos ?? Promise.resolve(null),
    ])
      .then(([completos, filtrados]) => {
        setTodos(revisar(completos ?? [], yearId))
        setVisibles(revisar(filtrados ?? completos ?? [], yearId))
        setConflictoError('')
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTodos([])
        setVisibles([])
        setConflictoError(mensajeError(cause, 'No se pudieron cargar los conflictos de este horario.'))
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setCargando(false)
        }
      })

    return () => controller.abort()
  }, [filters.severity, filters.status, horarioId, revision, yearId])

  const summary = summarizeConflicts(todos)
  const rows = filterConflicts(visibles, {
    severity: '',
    status: consultaDe(filters) === 'pendientes' ? '' : filters.status,
    kind: filters.kind,
    groupId: '',
    teacherName: '',
    classroom: '',
  })
  const selected = todos.find((item) => item.id === selectedId) ?? null
  const scheduleLabel = tablero
    ? `Horario ${tablero.anioEscolar} - ${tablero.sedeNombre} · v${tablero.versionHorario || 1}`
    : 'Sin horario'

  const groupOptions = gruposLoading
    ? [{ value: '', label: 'Cargando grupos…' }]
    : gruposDelAnio.length === 0
      ? [{ value: '', label: 'Sin grupos activos' }]
      : gruposDelAnio.map((group) => ({
          value: group.id,
          label: `${group.codigo} · ${group.gradoNombre}`,
        }))

  const emptyMessage = horarioCargando
    ? 'Cargando el horario…'
    : cargando
    ? 'Cargando conflictos…'
    : !yearId
      ? 'No hay un año lectivo disponible para consultar conflictos.'
      : !groupId
        ? 'Este año lectivo no tiene grupos activos.'
        : horarioError
          ? 'No se pudo consultar el horario de este grupo.'
          : !horarioId
            ? 'Este grupo todavía no tiene un horario, así que no hay conflictos registrados.'
            : todos.length === 0
              ? 'Este horario no tiene conflictos registrados.'
              : 'No hay conflictos que coincidan con esos filtros.'

  const marcarResuelto = () => {
    if (!selected || selected.status === 'RESUELTO') {
      return
    }

    setResolviendo(true)
    setResolverError('')

    marcarConflictoResuelto(selected.id)
      .then(() => {
        setRevision((current) => current + 1)
      })
      .catch((cause) => {
        setResolverError(mensajeError(cause, 'No se pudo marcar el conflicto como resuelto.'))
      })
      .finally(() => {
        setResolviendo(false)
      })
  }

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

      <ValidationScheduleCard
        year={year}
        yearOptions={opcionesAniosEscolares(anios, aniosError)}
        yearDisabled={aniosLoading || Boolean(aniosError)}
        versionLabel={horarioId ? String(tablero?.versionHorario || 1) : '—'}
        scheduleName={
          tablero?.sedeNombre
            ? `Horario ${tablero.anioEscolar} - ${tablero.sedeNombre}`
            : ''
        }
        campusLabel={tablero?.sedeNombre || grupo?.sedeNombre || '—'}
        statusLabel={etiquetaEstado(horarioId ? tablero?.estadoHorario : null)}
        statusTone={tonoEstado(horarioId ? tablero?.estadoHorario : null)}
        onYearChange={setYearIdState}
      />
      <ValidationMetrics {...summary} />
      {horarioId ? (
        <PublishBanner
          readiness={summary.readiness}
          onOpenReady={() => setReadyOpen(true)}
        />
      ) : null}

      {aniosError ? (
        <p className={styles.alert} role="alert">
          {aniosError}
        </p>
      ) : null}
      {gruposError ? (
        <p className={styles.alert} role="alert">
          {gruposError}
        </p>
      ) : null}
      {horarioError ? (
        <p className={styles.alert} role="alert">
          {horarioError}
        </p>
      ) : null}
      {conflictoError ? (
        <p className={styles.alert} role="alert">
          {conflictoError}
        </p>
      ) : null}

      <ValidationFilters
        values={{ ...filters, groupId }}
        groupOptions={groupOptions}
        teacherOptions={filtroDocente}
        classroomOptions={filtroAula}
        kindOptions={tiposConflicto}
        validating={cargando || horarioCargando}
        onChange={(patch) => {
          if (patch.groupId !== undefined) {
            setGroupIdState(patch.groupId)
          }

          setFilters((current) => ({
            ...current,
            ...(patch.severity !== undefined ? { severity: patch.severity } : {}),
            ...(patch.status !== undefined ? { status: patch.status } : {}),
            ...(patch.kind !== undefined ? { kind: patch.kind } : {}),
            ...(patch.teacherName !== undefined ? { teacherName: patch.teacherName } : {}),
            ...(patch.classroom !== undefined ? { classroom: patch.classroom } : {}),
          }))
        }}
        onRevalidate={() => setRevision((current) => current + 1)}
      />

      <ConflictsTable
        rows={rows}
        emptyMessage={emptyMessage}
        onDetail={(conflict) => {
          setResolverError('')
          setSelectedId(conflict.id)
        }}
      />

      <ConflictDetailModal
        open={Boolean(selected)}
        conflict={selected}
        scheduleLabel={scheduleLabel}
        detectedAt={selected?.detectedAt ?? '—'}
        resolving={resolviendo}
        resolveError={resolverError}
        onClose={() => {
          setSelectedId(null)
          setResolverError('')
        }}
        onOpenSchedule={() => navigate('/dashboard/horario')}
        onResolve={marcarResuelto}
      />
      <ValidationReadyModal
        open={readyOpen}
        hasWarnings={summary.readiness === 'warnings'}
        onClose={() => setReadyOpen(false)}
        onOpenSchedule={() => navigate('/dashboard/horario')}
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
