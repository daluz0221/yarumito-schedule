import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  actualizarClaseProgramada,
  anioEscolarVisible,
  ApiError,
  bandasDelTablero,
  clasesDelTablero,
  consultarHorarioGrupo,
  crearClaseProgramada,
  diaSemanaApi,
  eliminarClaseProgramada,
  etiquetaEstadoHorario,
  etiquetaGrado,
  listarAulasPorSede,
  metricasDelTablero,
  opcionesAniosEscolares,
  opcionesCatalogoSedes,
  pendientesDelTablero,
  sedeVisible,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
  useCatalogoSedes,
  type AulaResponse,
  type ClaseVista,
  type EstadoHorario,
  type HorarioGrupoResponse,
  type ResumenHorarioResponse,
} from '../../../api'
import { blockKey, type PendingPlacement } from '../../../content/scheduleConstruction'
import type { WeekDay } from '../../../content/teacherRestrictions'
import { Text } from '../../atoms/Text'
import type { BadgeTone } from '../../atoms/Badge'
import { PendingAssignments } from '../PendingAssignments'
import { RemoveScheduleClassModal } from '../RemoveScheduleClassModal'
import {
  ScheduleClassFormModal,
  type ScheduleClassFormValues,
} from '../ScheduleClassFormModal'
import { ScheduleGrid } from '../ScheduleGrid'
import { ScheduleMetrics } from '../ScheduleMetrics'
import { ScheduleSelector } from '../ScheduleSelector'
import styles from './ScheduleWorkspace.module.css'

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

  return 'warning'
}

function aulaInicial(
  aulas: AulaResponse[],
  aulaFijaId: string | null | undefined,
  note: string | null,
) {
  const activas = aulas.filter((aula) => aula.activa)

  if (note && /laborat/i.test(note)) {
    const laboratorio = activas.find(
      (aula) => aula.tipo === 'LABORATORIO' || /lab/i.test(aula.nombre),
    )

    if (laboratorio) {
      return laboratorio.id
    }
  }

  if (aulaFijaId && activas.some((aula) => aula.id === aulaFijaId)) {
    return aulaFijaId
  }

  return activas[0]?.id ?? ''
}

function opcionesAula(aulas: AulaResponse[], actualId: string) {
  const options = aulas
    .filter((aula) => aula.activa)
    .map((aula) => ({
      value: aula.id,
      label: aula.nombre?.trim() || 'Aula',
    }))

  if (actualId && !options.some((option) => option.value === actualId)) {
    const actual = aulas.find((aula) => aula.id === actualId)

    return [{ value: actualId, label: actual?.nombre?.trim() || 'Aula actual' }, ...options]
  }

  return options
}

type ClassDialog =
  | { name: 'create'; day: WeekDay; bandId: string; subjectKey: string }
  | { name: 'edit'; placementId: string }

export function ScheduleWorkspace() {
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos()
  const { sedes, error: sedesError } = useCatalogoSedes()
  const [yearId, setYearId] = useState('')
  const [campusId, setCampusId] = useState('')
  const [groupId, setGroupId] = useState('')
  const [tableroGrupoId, setTableroGrupoId] = useState('')
  const [clases, setClases] = useState<ClaseVista[]>([])
  const [pending, setPending] = useState<PendingPlacement[]>([])
  const [resumen, setResumen] = useState<ResumenHorarioResponse | null>(null)
  const [estadoHorario, setEstadoHorario] = useState<EstadoHorario | null>(null)
  const [turnoId, setTurnoId] = useState('')
  const [turnoNombre, setTurnoNombre] = useState('')
  const [franjas, setFranjas] = useState<ReturnType<typeof bandasDelTablero>>([])
  const [contexto, setContexto] = useState({ codigo: '', grado: '', anio: 0 })
  const [horarioError, setHorarioError] = useState('')
  const [horarioLoading, setHorarioLoading] = useState(false)
  const [aulas, setAulas] = useState<AulaResponse[] | null>(null)
  const [aulasError, setAulasError] = useState('')
  const [classDialog, setClassDialog] = useState<ClassDialog | null>(null)
  const [removeId, setRemoveId] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [removing, setRemoving] = useState(false)
  const [removeError, setRemoveError] = useState('')
  const solicitudRef = useRef(0)

  const mostrarTablero = useCallback((grupoSolicitado: string, tablero: HorarioGrupoResponse) => {
    setTableroGrupoId(grupoSolicitado)
    setClases(clasesDelTablero(tablero))
    setPending(pendientesDelTablero(tablero))
    setResumen(tablero.resumen ?? null)
    setEstadoHorario(tablero.estadoHorario ?? null)
    setTurnoId(tablero.turnoId ?? '')
    setTurnoNombre(tablero.turnoNombre?.trim() || '')
    setFranjas(bandasDelTablero(tablero.franjas))
    setContexto({
      codigo: tablero.grupoCodigo?.trim() || '',
      grado: tablero.gradoNombre?.trim() || '',
      anio: tablero.anioEscolar,
    })
    setHorarioError('')
  }, [])

  useEffect(() => {
    if (!anios?.length) {
      return
    }

    setYearId((current) =>
      anios.some((year) => year.id === current) ? current : (anioEscolarVisible(anios)?.id ?? ''),
    )
  }, [anios])

  const campusSeleccionada = sedes?.some((sede) => sede.id === campusId)
    ? campusId
    : (sedeVisible(sedes ?? [])?.id ?? '')

  const gruposDelAnio = useMemo(
    () => (grupos ?? []).filter((group) => group.anioEscolarId === yearId && group.activo),
    [grupos, yearId],
  )

  const yearGroups = useMemo(() => {
    return gruposDelAnio
      .filter((group) => !campusSeleccionada || group.sedeId === campusSeleccionada)
      .sort(
        (left, right) =>
          left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, 'es'),
      )
  }, [campusSeleccionada, gruposDelAnio])

  useEffect(() => {
    if (!yearId || grupos === null) {
      return
    }

    setGroupId((current) =>
      yearGroups.some((group) => group.id === current) ? current : (yearGroups[0]?.id ?? ''),
    )
  }, [grupos, yearGroups, yearId])

  useEffect(() => {
    setClassDialog(null)
    setRemoveId(null)
    setSaveError('')
    setRemoveError('')
  }, [groupId])

  useEffect(() => {
    const solicitud = ++solicitudRef.current

    if (!groupId) {
      setTableroGrupoId('')
      setClases([])
      setPending([])
      setResumen(null)
      setEstadoHorario(null)
      setTurnoId('')
      setTurnoNombre('')
      setFranjas([])
      setContexto({ codigo: '', grado: '', anio: 0 })
      setHorarioError('')
      setHorarioLoading(false)
      return
    }

    const controller = new AbortController()
    setHorarioLoading(true)
    setHorarioError('')
    setTableroGrupoId('')
    setClases([])
    setPending([])
    setResumen(null)
    setEstadoHorario(null)
    setTurnoId('')
    setTurnoNombre('')
    setFranjas([])
    setContexto({ codigo: '', grado: '', anio: 0 })

    consultarHorarioGrupo(groupId, controller.signal)
      .then((tablero) => {
        if (controller.signal.aborted || solicitudRef.current !== solicitud) {
          return
        }

        mostrarTablero(groupId, tablero)
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted || solicitudRef.current !== solicitud) {
          return
        }

        setTableroGrupoId('')
        setClases([])
        setPending([])
        setResumen(null)
        setEstadoHorario(null)
        setTurnoId('')
        setTurnoNombre('')
        setFranjas([])
        setContexto({ codigo: '', grado: '', anio: 0 })
        setHorarioError(mensajeError(cause, 'No se pudo cargar el horario de este grupo.'))
      })
      .finally(() => {
        if (!controller.signal.aborted && solicitudRef.current === solicitud) {
          setHorarioLoading(false)
        }
      })

    return () => controller.abort()
  }, [groupId, mostrarTablero])

  const year = anios?.find((item) => item.id === yearId) ?? null
  const grupo = yearGroups.find((item) => item.id === groupId) ?? null
  const sedeDelGrupo = grupo?.sedeId ?? ''

  useEffect(() => {
    if (!sedeDelGrupo) {
      setAulas([])
      setAulasError('')
      return
    }

    const controller = new AbortController()
    setAulas(null)
    setAulasError('')

    listarAulasPorSede(sedeDelGrupo, controller.signal)
      .then((lista) => {
        if (!controller.signal.aborted) {
          setAulas(lista)
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setAulas([])
        setAulasError(mensajeError(cause, 'No se pudieron cargar las aulas de la sede.'))
      })

    return () => controller.abort()
  }, [sedeDelGrupo])

  const tableroListo = tableroGrupoId === groupId && !horarioLoading
  const readOnly =
    year?.status === 'CERRADO' ||
    estadoHorario === 'PUBLICADO' ||
    estadoHorario === 'ARCHIVADO'
  const gradeLabel = etiquetaGrado(contexto.grado || grupo?.gradoNombre || '')
  const groupCode = contexto.codigo || grupo?.codigo || ''
  const yearNumber = contexto.anio || year?.year || 0
  const shiftLabel = turnoNombre || (horarioLoading ? 'Cargando turno…' : 'Sin turno')
  const metrics = metricasDelTablero(resumen)
  const blocks = useMemo(() => {
    const map = new Map<string, ClaseVista>()
    clases.forEach((clase) => map.set(blockKey(clase.day, clase.bandId), clase))
    return map
  }, [clases])
  const bandOptions = franjas
    .filter((band) => band.kind === 'class')
    .map((band) => ({ value: band.id, label: band.label }))
  const yearOptions = opcionesAniosEscolares(anios, aniosError)
  const groupOptions = gruposLoading
    ? [{ value: '', label: 'Cargando grupos…' }]
    : gruposError
      ? [{ value: '', label: 'Grupos no disponibles' }]
      : yearGroups.map((item) => ({ value: item.id, label: item.codigo }))
  const hayAulasActivas = (aulas ?? []).some((aula) => aula.activa)

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
  }

  const openCreate = (day: WeekDay, bandId: string, subjectKey = pending[0]?.key ?? '') => {
    if (readOnly || !grupo || bandOptions.length === 0 || !hayAulasActivas) {
      return
    }

    const item = pending.find((entry) => entry.key === subjectKey) ?? pending[0]
    setSaveError('')
    setRemoveId(null)
    setClassDialog({
      name: 'create',
      day,
      bandId,
      subjectKey: item?.key ?? subjectKey,
    })
  }

  const saveClass = async (values: ScheduleClassFormValues) => {
    if (!grupo || !classDialog || saving || readOnly) {
      return
    }

    const solicitud = solicitudRef.current
    const grupoSolicitado = grupo.id
    const current =
      classDialog.name === 'edit'
        ? clases.find((item) => item.id === classDialog.placementId)
        : undefined

    if (current?.locked) {
      return
    }

    const asignacionId = current?.subjectKey ?? values.subjectKey

    if (!asignacionId || !values.bandId || !values.classroom) {
      setSaveError('Selecciona la asignación, la franja y el aula.')
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      if (current) {
        await actualizarClaseProgramada(current.id, {
          dia: diaSemanaApi(values.day),
          franjaHorariaId: values.bandId,
          aulaId: values.classroom,
        })
      } else {
        await crearClaseProgramada({
          asignacionAcademicaId: asignacionId,
          dia: diaSemanaApi(values.day),
          franjaHorariaId: values.bandId,
          aulaId: values.classroom,
        })
      }

      try {
        const tablero = await consultarHorarioGrupo(grupoSolicitado)

        if (solicitudRef.current !== solicitud) {
          return
        }

        mostrarTablero(grupoSolicitado, tablero)
      } catch (cause) {
        if (solicitudRef.current !== solicitud) {
          return
        }

        setHorarioError(
          mensajeError(cause, 'El cambio se guardó, pero no se pudo actualizar el horario.'),
        )
      }

      if (solicitudRef.current !== solicitud) {
        return
      }

      setClassDialog(null)
      setSaveError('')
    } catch (cause) {
      if (solicitudRef.current !== solicitud) {
        return
      }

      setSaveError(
        mensajeError(
          cause,
          current ? 'No se pudo actualizar la clase.' : 'No se pudo programar la clase.',
        ),
      )
    } finally {
      setSaving(false)
    }
  }

  const confirmRemove = async () => {
    const clase = clases.find((item) => item.id === removeId)

    if (!clase || clase.locked || removing || readOnly) {
      if (!clase || clase?.locked) {
        setRemoveId(null)
      }
      return
    }

    const solicitud = solicitudRef.current
    const grupoSolicitado = groupId
    setRemoving(true)
    setRemoveError('')

    try {
      await eliminarClaseProgramada(clase.id)

      try {
        const tablero = await consultarHorarioGrupo(grupoSolicitado)

        if (solicitudRef.current !== solicitud) {
          return
        }

        mostrarTablero(grupoSolicitado, tablero)
      } catch (cause) {
        if (solicitudRef.current !== solicitud) {
          return
        }

        setHorarioError(
          mensajeError(cause, 'La clase se quitó, pero no se pudo actualizar el horario.'),
        )
      }

      if (solicitudRef.current !== solicitud) {
        return
      }

      setRemoveId(null)
      setClassDialog(null)
      setRemoveError('')
    } catch (cause) {
      if (solicitudRef.current !== solicitud) {
        return
      }

      setRemoveError(mensajeError(cause, 'No se pudo quitar la clase del horario.'))
    } finally {
      setRemoving(false)
    }
  }

  const editing =
    classDialog?.name === 'edit'
      ? clases.find((item) => item.id === classDialog.placementId)
      : undefined
  const creatingItem =
    classDialog?.name === 'create'
      ? pending.find((item) => item.key === classDialog.subjectKey) ?? pending[0]
      : undefined
  const formOpen = Boolean(classDialog) && !removeId
  const classroomValue =
    editing?.aulaId ||
    aulaInicial(aulas ?? [], grupo?.aulaFijaId, creatingItem?.note ?? null)
  const primeraFranja = bandOptions[0]?.value ?? ''
  const classroomOptions = opcionesAula(aulas ?? [], classroomValue)

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Construcción de horario
        </Text>
        <Text variant="body">
          Programa las asignaciones académicas por día, franja horaria y aula.
        </Text>
      </div>

      <ScheduleSelector
        yearId={year?.id ?? ''}
        campusId={campusSeleccionada}
        shiftId={turnoId}
        groupId={grupo?.id ?? ''}
        gradeLabel={gradeLabel}
        year={yearNumber}
        groupCode={groupCode}
        yearOptions={yearOptions}
        yearDisabled={aniosLoading || Boolean(aniosError)}
        groupOptions={groupOptions}
        groupsDisabled={gruposLoading || Boolean(gruposError)}
        campusOptions={opcionesCatalogoSedes(sedes, sedesError)}
        campusDisabled={Boolean(sedesError) || !sedes?.length}
        shiftOptions={[{ value: turnoId, label: shiftLabel }]}
        shiftDisabled
        statusLabel={
          horarioLoading
            ? 'Cargando'
            : horarioError
              ? 'No disponible'
              : grupo && tableroListo
                ? etiquetaEstadoHorario(estadoHorario)
                : 'Sin horario'
        }
        statusTone={
          grupo && tableroListo && !horarioLoading && !horarioError
            ? tonoEstado(estadoHorario)
            : 'neutral'
        }
        onYearChange={selectYear}
        onCampusChange={setCampusId}
        onShiftChange={() => undefined}
        onGroupChange={setGroupId}
      />

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
      {sedesError ? (
        <p className={styles.alert} role="alert">
          {sedesError}
        </p>
      ) : null}
      {horarioError ? (
        <p className={styles.alert} role="alert">
          {horarioError}
        </p>
      ) : null}
      {aulasError ? (
        <p className={styles.alert} role="alert">
          {aulasError}
        </p>
      ) : null}

      {!year && aniosLoading ? (
        <p className={styles.notice}>Cargando año lectivo...</p>
      ) : null}
      {year && gruposLoading ? <p className={styles.notice}>Cargando grupos...</p> : null}

      {year && !gruposLoading && !grupo ? (
        <p className={styles.notice}>
          {gruposDelAnio.length === 0
            ? 'Este año lectivo aún no tiene grupos activos para construir el horario.'
            : 'Esta sede no tiene grupos activos en el año lectivo seleccionado.'}
        </p>
      ) : null}

      {grupo && horarioLoading ? (
        <p className={styles.notice}>Cargando horario del grupo...</p>
      ) : null}

      {grupo && tableroListo && !horarioError ? (
        <>
          <ScheduleMetrics {...metrics} />
          {aulas && !hayAulasActivas ? (
            <p className={styles.notice}>
              Esta sede no tiene aulas activas para programar clases.
            </p>
          ) : null}
          <div className={styles.board}>
            {franjas.length === 0 ? (
              <p className={styles.notice}>
                Este grupo no tiene franjas horarias para armar la rejilla.
              </p>
            ) : (
              <ScheduleGrid
                groupCode={groupCode}
                groupId={grupo.id}
                shiftLabel={shiftLabel}
                readOnly={readOnly}
                bands={franjas}
                blocks={blocks}
                onProgram={(day, bandId) => openCreate(day, bandId)}
                onEdit={(placementId) => {
                  setSaveError('')
                  setRemoveId(null)
                  setClassDialog({ name: 'edit', placementId })
                }}
              />
            )}
            <PendingAssignments
              items={pending}
              readOnly={readOnly || bandOptions.length === 0 || !hayAulasActivas}
              onProgram={(subjectKey) => openCreate('Lunes', primeraFranja, subjectKey)}
            />
          </div>
        </>
      ) : null}

      {classDialog && grupo && (classDialog.name === 'create' || editing) ? (
        <ScheduleClassFormModal
          key={
            classDialog.name === 'edit'
              ? `edit-${classDialog.placementId}`
              : `create-${classDialog.day}-${classDialog.bandId}-${classDialog.subjectKey}`
          }
          open={formOpen}
          mode={classDialog.name === 'edit' ? 'edit' : 'create'}
          groupCode={groupCode}
          subject={editing?.subject ?? creatingItem?.subject ?? ''}
          teacherName={editing?.teacherName ?? creatingItem?.teacherName ?? ''}
          pendingHours={creatingItem?.pendingHours ?? 0}
          assignments={pending}
          day={classDialog.name === 'edit' ? editing?.day ?? 'Lunes' : classDialog.day}
          bandId={
            classDialog.name === 'edit' ? editing?.bandId ?? primeraFranja : classDialog.bandId
          }
          classroom={classroomValue}
          subjectKey={editing?.subjectKey ?? creatingItem?.key ?? ''}
          bandOptions={bandOptions}
          classroomOptions={classroomOptions}
          submitting={saving}
          error={saveError}
          onClose={() => {
            if (!saving) {
              setClassDialog(null)
              setSaveError('')
            }
          }}
          onRemove={() => {
            if (editing && !editing.locked && !saving) {
              setRemoveError('')
              setRemoveId(editing.id)
            }
          }}
          onSubmit={saveClass}
        />
      ) : null}

      <RemoveScheduleClassModal
        open={Boolean(removeId)}
        submitting={removing}
        error={removeError}
        onClose={() => {
          if (!removing) {
            setRemoveId(null)
            setRemoveError('')
          }
        }}
        onConfirm={confirmRemove}
      />
    </section>
  )
}
