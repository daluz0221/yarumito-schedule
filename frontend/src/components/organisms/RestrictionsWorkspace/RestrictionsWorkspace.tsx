import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  anioEscolarVisible,
  ApiError,
  horaCorta,
  listarCatalogoAreas,
  listarCatalogoTurnos,
  listarDocentes,
  listarFranjasPorTurno,
  listarRestriccionesPorDocenteYAnio,
  useCatalogoAniosEscolares,
} from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { FranjaHorariaResponse } from '../../../api/franjasHorarias'
import type { RestriccionDocenteVista } from '../../../api/restriccionesDocentes'
import type { TurnoResponse } from '../../../api/turnos'
import { resolveAreaName } from '../../../content/teacherProfile'
import { formatTeacherName, labelEstado, type Teacher } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { RestrictionSelector } from '../RestrictionSelector'
import { TeacherAvailabilitySummary } from '../TeacherAvailabilitySummary'
import { RetiredRestrictionsSection } from '../RetiredRestrictionsSection'
import { WeeklyAvailability, type AvailabilityBand } from '../WeeklyAvailability'
import styles from './RestrictionsWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback
}

function horaVisible(value: string) {
  return horaCorta(value).replace(/^0(?=\d:)/, '')
}

function turnoInicial(turnos: TurnoResponse[]) {
  return (
    turnos.find((turno) => turno.nombre.trim().toLowerCase() === 'mañana') ?? turnos[0] ?? null
  )
}

function aBand(franja: FranjaHorariaResponse): AvailabilityBand {
  const horario = `${horaVisible(franja.horaInicio)} – ${horaVisible(franja.horaFin)}`
  return {
    id: franja.id,
    kind: franja.esDescanso ? 'break' : 'class',
    label: franja.esDescanso ? `Descanso · ${horario} · No editable` : horario,
  }
}

type Catalog = {
  teachers: Teacher[]
  areas: AreaResponse[]
  error: string
}

export function RestrictionsWorkspace() {
  const [searchParams] = useSearchParams()
  const requestedTeacherId = searchParams.get('docente') ?? ''
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const [yearId, setYearId] = useState('')
  const [teacherId, setTeacherId] = useState(requestedTeacherId)
  const [catalog, setCatalog] = useState<Catalog | null>(null)
  const [turnos, setTurnos] = useState<TurnoResponse[]>([])
  const [turnosError, setTurnosError] = useState('')
  const [turnoId, setTurnoId] = useState('')
  const [franjas, setFranjas] = useState<FranjaHorariaResponse[] | null>(null)
  const [franjasError, setFranjasError] = useState('')
  const [restrictions, setRestrictions] = useState<RestriccionDocenteVista[]>([])
  const [restrictionsLoading, setRestrictionsLoading] = useState(false)
  const [restrictionsError, setRestrictionsError] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    if (!anios?.length) {
      return
    }

    setYearId((current) =>
      anios.some((year) => year.id === current) ? current : (anioEscolarVisible(anios)?.id ?? ''),
    )
  }, [anios])

  useEffect(() => {
    const controller = new AbortController()

    Promise.all([
      listarDocentes({ page: 0, size: 100 }, controller.signal),
      listarCatalogoAreas(controller.signal),
    ])
      .then(([teachersPage, areas]) => {
        const teachers = [...teachersPage.content].sort((left, right) =>
          formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
        )
        setCatalog({ teachers, areas, error: '' })
        setTeacherId((current) => {
          if (requestedTeacherId && teachers.some((teacher) => teacher.id === requestedTeacherId)) {
            return requestedTeacherId
          }

          if (current && teachers.some((teacher) => teacher.id === current)) {
            return current
          }

          return teachers[0]?.id ?? ''
        })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setCatalog({
          teachers: [],
          areas: [],
          error: mensajeError(cause, 'No se pudo cargar la disponibilidad docente.'),
        })
      })

    return () => controller.abort()
  }, [requestedTeacherId])

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoTurnos(controller.signal)
      .then((items) => {
        if (controller.signal.aborted) {
          return
        }

        setTurnos(items)
        setTurnosError('')
        setTurnoId((current) =>
          items.some((turno) => turno.id === current) ? current : (turnoInicial(items)?.id ?? ''),
        )
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setTurnos([])
        setTurnosError(mensajeError(cause, 'No se pudo cargar el catálogo de turnos.'))
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!turnoId) {
      setFranjas([])
      return
    }

    const controller = new AbortController()
    setFranjas(null)
    setFranjasError('')

    listarFranjasPorTurno(turnoId, controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setFranjas(
            [...(items ?? [])].sort(
              (left, right) => left.orden - right.orden || left.numero - right.numero,
            ),
          )
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setFranjas([])
        setFranjasError(mensajeError(cause, 'No se pudieron cargar las franjas del turno.'))
      })

    return () => controller.abort()
  }, [turnoId])

  useEffect(() => {
    if (!teacherId || !yearId) {
      setRestrictions([])
      setRestrictionsLoading(false)
      return
    }

    const controller = new AbortController()
    setRestrictionsLoading(true)
    setRestrictionsError('')

    listarRestriccionesPorDocenteYAnio(teacherId, yearId, controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setRestrictions(items)
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setRestrictions([])
        setRestrictionsError(mensajeError(cause, 'No se pudieron cargar las restricciones.'))
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setRestrictionsLoading(false)
        }
      })

    return () => controller.abort()
  }, [teacherId, yearId, revision])

  const year = anios?.find((item) => item.id === yearId) ?? null
  const teachers = catalog?.teachers ?? []
  const teacher = teachers.find((item) => item.id === teacherId)
  const teacherName = teacher ? formatTeacherName(teacher) : ''
  const turno = turnos.find((item) => item.id === turnoId) ?? null
  const readOnly = !year || year.status === 'CERRADO' || teacher?.estado !== 'ACTIVO'
  const bands = (franjas ?? []).map(aBand)

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Restricciones y disponibilidad docente
        </Text>
        <Text variant="body">
          Registra las franjas en las que cada docente presenta restricciones o
          condiciones de disponibilidad durante el año lectivo.
        </Text>
      </div>

      {aniosLoading || catalog === null ? (
        <p className={styles.message}>Cargando docentes...</p>
      ) : null}
      {aniosError ? <p className={styles.message}>{aniosError}</p> : null}
      {catalog?.error ? <p className={styles.message}>{catalog.error}</p> : null}
      {turnosError ? <p className={styles.message}>{turnosError}</p> : null}

      {catalog && !catalog.error && year ? (
        <>
          <RestrictionSelector
            status={year.status}
            year={year.year}
            years={anios ?? []}
            teacherName={teacherName}
            teachers={teachers.map((item) => ({
              id: item.id,
              name: formatTeacherName(item),
              areaName: resolveAreaName(item.areaNombramientoId, catalog.areas),
              active: item.estado === 'ACTIVO',
            }))}
            onYearChange={setYearId}
            onTeacherChange={setTeacherId}
          />

          {teachers.length === 0 ? (
            <p className={styles.message}>No hay docentes registrados.</p>
          ) : null}

          {teacher ? (
            <>
              <TeacherAvailabilitySummary
                name={teacherName}
                areaName={resolveAreaName(teacher.areaNombramientoId, catalog.areas)}
                statusLabel={labelEstado(teacher.estado)}
                year={year.year}
                restrictionCount={restrictions.length}
              />
              {teacher.estado !== 'ACTIVO' ? (
                <p className={styles.message}>
                  Solo un docente activo puede registrar o editar restricciones.
                </p>
              ) : null}
              <WeeklyAvailability
                yearId={year.id}
                year={year.year}
                teacherId={teacher.id}
                teacherName={teacherName}
                readOnly={readOnly}
                yearClosed={year.status === 'CERRADO'}
                shiftLabel={turno?.nombre ?? 'Sin turno'}
                turnos={turnos.map((item) => ({ id: item.id, nombre: item.nombre }))}
                turnoId={turnoId}
                onTurnoChange={setTurnoId}
                bands={bands}
                bandsLoading={franjas === null && !franjasError}
                bandsError={franjasError}
                restrictions={restrictions}
                loading={restrictionsLoading}
                error={restrictionsError}
                onSaved={() => setRevision((current) => current + 1)}
              />
              <RetiredRestrictionsSection
                teacherId={teacher.id}
                yearId={year.id}
                bands={bands}
                readOnly={readOnly}
                revision={revision}
                onRestored={() => setRevision((current) => current + 1)}
              />
            </>
          ) : null}
        </>
      ) : null}

      {catalog && !catalog.error && !aniosLoading && !aniosError && !year ? (
        <p className={styles.message}>No hay años lectivos registrados.</p>
      ) : null}
    </section>
  )
}
