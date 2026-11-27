import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import { idDisponible } from '../../../content/idDisponible'
import {
  actualizarClaseProgramada,
  anioEscolarVisible,
  ApiError,
  bandasDelTablero,
  clasesDelTablero,
  consultarHorarioGrupo,
  crearBorradorDeMejora,
  crearClaseProgramada,
  descartarBorradorHorario,
  diaSemanaApi,
  eliminarClaseProgramada,
  etiquetaEstadoHorario,
  etiquetaGrado,
  listarAulasPorSede,
  listarCatalogoTurnos,
  listarFranjasPorTurno,
  listarPlanesEstudio,
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
  type PlanEstudiosResponse,
  type ResumenHorarioResponse,
  type TurnoResponse,
} from '../../../api'
import { blockKey, type PendingPlacement } from '../../../content/scheduleConstruction'
import type { WeekDay } from '../../../content/teacherRestrictions'
import { Button } from '../../atoms/Button'
import { Text } from '../../atoms/Text'
import { ConfirmActionModal } from '../ConfirmActionModal'
import type { BadgeTone } from '../../atoms/Badge'
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

type TimeBands = ReturnType<typeof bandasDelTablero>

// Franjas de un turno adicional del plan (p. ej. Contrajornada en 10° y 11°).
// El tablero del backend solo trae las franjas del turno principal del grupo.
type JornadaAdicional = {
  turnoId: string
  nombre: string
  franjas: TimeBands
}

type ClassDialog =
  | { name: 'create'; day: WeekDay; bandId: string; subjectKey: string }
  | { name: 'edit'; placementId: string }

export function ScheduleWorkspace() {
  const navigate = useNavigate()
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos()
  const { sedes, error: sedesError } = useCatalogoSedes()
  const [yearElegido, elegirAnio] = useAnioDeTrabajo()
  const [campusId, setCampusId] = useState('')
  const [groupElegido, setGroupElegido] = useState('')
  const [tableroGrupoId, setTableroGrupoId] = useState('')
  const [clases, setClases] = useState<ClaseVista[]>([])
  const [pending, setPending] = useState<PendingPlacement[]>([])
  const [resumen, setResumen] = useState<ResumenHorarioResponse | null>(null)
  const [estadoHorario, setEstadoHorario] = useState<EstadoHorario | null>(null)
  const [horarioId, setHorarioId] = useState<string | null>(null)
  const [versionHorario, setVersionHorario] = useState(0)
  const [mejoraAviso, setMejoraAviso] = useState('')
  const [mejoraError, setMejoraError] = useState('')
  const [creandoBorrador, setCreandoBorrador] = useState(false)
  const [descartarAbierto, setDescartarAbierto] = useState(false)
  const [descartandoBorrador, setDescartandoBorrador] = useState(false)
  const [turnoFiltro, setTurnoFiltro] = useState('')
  const [turnos, setTurnos] = useState<TurnoResponse[] | null>(null)
  const [planes, setPlanes] = useState<PlanEstudiosResponse[] | null>(null)
  const [turnosError, setTurnosError] = useState('')
  const [turnoNombre, setTurnoNombre] = useState('')
  const [turnoPrincipalId, setTurnoPrincipalId] = useState('')
  const [franjas, setFranjas] = useState<TimeBands>([])
  const [jornadasAdicionales, setJornadasAdicionales] = useState<JornadaAdicional[]>([])
  const [jornadasError, setJornadasError] = useState('')
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
    setHorarioId(tablero.horarioId)
    setVersionHorario(tablero.versionHorario || 0)
    setTurnoNombre(tablero.turnoNombre?.trim() || '')
    setTurnoPrincipalId(tablero.turnoId ?? '')
    setFranjas(bandasDelTablero(tablero.franjas))
    setContexto({
      codigo: tablero.grupoCodigo?.trim() || '',
      grado: tablero.gradoNombre?.trim() || '',
      anio: tablero.anioEscolar,
    })
    setHorarioError('')
  }, [])

  const yearId = idDisponible(
    yearElegido,
    anios,
    Boolean(anios?.length),
    anioEscolarVisible(anios ?? [])?.id ?? '',
  )

  const campusSeleccionada = sedes?.some((sede) => sede.id === campusId)
    ? campusId
    : (sedeVisible(sedes ?? [])?.id ?? '')

  const gruposDelAnio = useMemo(
    () => (grupos ?? []).filter((group) => group.anioEscolarId === yearId && group.activo),
    [grupos, yearId],
  )

  // Turnos que usa el plan de estudios de cada grado en el año, en orden de
  // aparición. El primero es el turno principal (el backend arma el tablero
  // con él); los demás son jornadas adicionales, como la contrajornada de la
  // media. El selector muestra el catálogo completo; al elegir un turno quedan
  // los grupos cuyo plan usa ese turno en alguna asignatura.
  const turnosPorGrado = useMemo(() => {
    const map = new Map<string, string[]>()

    ;(planes ?? []).forEach((plan) => {
      if (!plan.turnoId || plan.activo === false) {
        return
      }

      const key = `${plan.anioEscolarId}:${plan.gradoId}`
      const lista = map.get(key) ?? []

      if (!lista.includes(plan.turnoId)) {
        map.set(key, [...lista, plan.turnoId])
      }
    })

    return map
  }, [planes])

  const gruposDeSede = useMemo(
    () =>
      gruposDelAnio
        .filter((group) => !campusSeleccionada || group.sedeId === campusSeleccionada)
        .sort(
          (left, right) =>
            left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, 'es'),
        ),
    [campusSeleccionada, gruposDelAnio],
  )

  const turnosDe = (group: { anioEscolarId: string; gradoId: string }) =>
    turnosPorGrado.get(`${group.anioEscolarId}:${group.gradoId}`) ?? []

  const turnosDelCatalogo = turnos ?? []
  const turnoActivo = turnosDelCatalogo.some((turno) => turno.id === turnoFiltro)
    ? turnoFiltro
    : ''
  const yearGroups = turnoActivo
    ? gruposDeSede.filter((group) => turnosDe(group).includes(turnoActivo))
    : gruposDeSede

  const groupId = idDisponible(
    groupElegido,
    yearGroups,
    Boolean(yearId) && grupos !== null,
    yearGroups[0]?.id ?? '',
  )
  const [grupoVisto, setGrupoVisto] = useState(groupId)

  if (grupoVisto !== groupId) {
    setGrupoVisto(groupId)
    setClassDialog(null)
    setRemoveId(null)
    setSaveError('')
    setRemoveError('')
    setTableroGrupoId('')
    setClases([])
    setPending([])
    setResumen(null)
    setEstadoHorario(null)
    setHorarioId(null)
    setVersionHorario(0)
    setMejoraAviso('')
    setMejoraError('')
    setDescartarAbierto(false)
    setTurnoNombre('')
    setTurnoPrincipalId('')
    setFranjas([])
    setJornadasAdicionales([])
    setJornadasError('')
    setContexto({ codigo: '', grado: '', anio: 0 })
    setHorarioError('')
    setHorarioLoading(Boolean(groupId))
  }

  useEffect(() => {
    const controller = new AbortController()

    Promise.all([
      listarCatalogoTurnos(controller.signal),
      listarPlanesEstudio(controller.signal),
    ])
      .then(([listaTurnos, listaPlanes]) => {
        setTurnos(listaTurnos)
        setPlanes(listaPlanes)
        setTurnosError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setTurnos([])
        setPlanes([])
        setTurnosError(mensajeError(cause, 'No se pudieron cargar los turnos para filtrar.'))
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!groupId) {
      return
    }

    const solicitud = ++solicitudRef.current
    const controller = new AbortController()

    consultarHorarioGrupo(groupId, controller.signal, { trabajo: true })
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

  // Turnos del plan distintos al que ya trae el tablero (clave estable para el efecto).
  const turnosAdicionalesKey = grupo && turnoPrincipalId
    ? turnosDe(grupo)
        .filter((turnoId) => turnoId !== turnoPrincipalId)
        .join(',')
    : ''

  useEffect(() => {
    if (!turnosAdicionalesKey) {
      return
    }

    const controller = new AbortController()
    const ids = turnosAdicionalesKey.split(',')

    Promise.all(ids.map((turnoId) => listarFranjasPorTurno(turnoId, controller.signal)))
      .then((listas) => {
        if (controller.signal.aborted) {
          return
        }

        setJornadasAdicionales(
          ids.map((turnoId, index) => ({
            turnoId,
            nombre:
              (turnos ?? []).find((turno) => turno.id === turnoId)?.nombre?.trim() ||
              listas[index][0]?.turnoNombre?.trim() ||
              'Jornada adicional',
            franjas: bandasDelTablero(listas[index]),
          })),
        )
        setJornadasError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setJornadasAdicionales([])
        setJornadasError(
          mensajeError(cause, 'No se pudieron cargar las franjas de la contrajornada.'),
        )
      })

    return () => controller.abort()
  }, [turnosAdicionalesKey, turnos])

  const jornadasVisibles = turnosAdicionalesKey ? jornadasAdicionales : []

  const [sedeVista, setSedeVista] = useState(sedeDelGrupo)

  if (sedeVista !== sedeDelGrupo) {
    setSedeVista(sedeDelGrupo)
    setAulas(sedeDelGrupo ? null : [])
    setAulasError('')
  }

  useEffect(() => {
    if (!sedeDelGrupo) {
      return
    }

    const controller = new AbortController()

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
  // Con jornadas adicionales, cada franja lleva el nombre de su turno para
  // distinguir, p. ej., "Mañana · 06:25 – 07:20" de "Contrajornada · 13:30 – 14:25".
  const conPrefijo = jornadasVisibles.length > 0
  const bandOptions = [
    { nombre: turnoNombre, franjas },
    ...jornadasVisibles,
  ].flatMap((jornada) =>
    jornada.franjas
      .filter((band) => band.kind === 'class')
      .map((band) => ({
        value: band.id,
        label: conPrefijo && jornada.nombre ? `${jornada.nombre} · ${band.label}` : band.label,
      })),
  )
  const yearOptions = opcionesAniosEscolares(anios, aniosError)
  const groupOptions = gruposLoading
    ? [{ value: '', label: 'Cargando grupos…' }]
    : gruposError
      ? [{ value: '', label: 'Grupos no disponibles' }]
      : yearGroups.map((item) => ({ value: item.id, label: item.codigo }))
  const hayAulasActivas = (aulas ?? []).some((aula) => aula.activa)

  const selectYear = (nextYearId: string) => {
    elegirAnio(nextYearId)
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
        const tablero = await consultarHorarioGrupo(grupoSolicitado, undefined, { trabajo: true })

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
        const tablero = await consultarHorarioGrupo(grupoSolicitado, undefined, { trabajo: true })

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
  const esBorradorMejora = estadoHorario === 'BORRADOR' && versionHorario > 1
  const puedeCrearBorrador =
    Boolean(horarioId) && estadoHorario === 'PUBLICADO' && year?.status !== 'CERRADO'

  const crearBorrador = async () => {
    if (!horarioId || !grupo || creandoBorrador) {
      return
    }

    setCreandoBorrador(true)
    setMejoraError('')
    setMejoraAviso('')

    try {
      const resultado = await crearBorradorDeMejora(horarioId)
      const tablero = await consultarHorarioGrupo(grupo.id, undefined, { trabajo: true })
      mostrarTablero(grupo.id, tablero)
      setMejoraAviso(
        resultado.nuevo
          ? 'Se creó el borrador de mejora. El horario publicado sigue vigente.'
          : 'Ya hay un borrador de mejora. Puedes seguir editándolo.',
      )
    } catch (cause) {
      setMejoraError(mensajeError(cause, 'No se pudo crear el borrador de mejora.'))
    } finally {
      setCreandoBorrador(false)
    }
  }

  const descartarBorrador = async () => {
    if (!horarioId || !grupo || descartandoBorrador) {
      return
    }

    setDescartandoBorrador(true)
    setMejoraError('')

    try {
      await descartarBorradorHorario(horarioId)
      const tablero = await consultarHorarioGrupo(grupo.id, undefined, { trabajo: true })
      mostrarTablero(grupo.id, tablero)
      setDescartarAbierto(false)
      setMejoraAviso('Se descartó el borrador. El horario publicado sigue vigente.')
    } catch (cause) {
      setMejoraError(mensajeError(cause, 'No se pudo descartar el borrador.'))
    } finally {
      setDescartandoBorrador(false)
    }
  }

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
        shiftId={turnoActivo}
        groupId={grupo?.id ?? ''}
        gradeLabel={gradeLabel}
        year={yearNumber}
        groupCode={groupCode}
        groupShiftLabel={turnoNombre}
        yearOptions={yearOptions}
        yearDisabled={aniosLoading || Boolean(aniosError)}
        groupOptions={groupOptions}
        groupsDisabled={gruposLoading || Boolean(gruposError)}
        campusOptions={opcionesCatalogoSedes(sedes, sedesError)}
        campusDisabled={Boolean(sedesError) || !sedes?.length}
        shiftOptions={[
          {
            value: '',
            label: turnos === null && !turnosError ? 'Cargando turnos…' : 'Todos los turnos',
          },
          ...turnosDelCatalogo.map((turno) => ({ value: turno.id, label: turno.nombre })),
        ]}
        shiftDisabled={turnos === null || turnosDelCatalogo.length === 0}
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
        onShiftChange={setTurnoFiltro}
        onGroupChange={setGroupElegido}
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
      {turnosError ? (
        <p className={styles.alert} role="alert">
          {turnosError}
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
      {jornadasError ? (
        <p className={styles.alert} role="alert">
          {jornadasError}
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
            : gruposDeSede.length === 0
              ? 'Esta sede no tiene grupos activos en el año lectivo seleccionado.'
              : 'No hay grupos activos en el turno seleccionado.'}
        </p>
      ) : null}

      {grupo && horarioLoading ? (
        <p className={styles.notice}>Cargando horario del grupo...</p>
      ) : null}

      {grupo && tableroListo && !horarioError && year?.status !== 'CERRADO' && (puedeCrearBorrador || esBorradorMejora) ? (
        <div className={styles.draftBar}>
          <p className={styles.draftText}>
            {esBorradorMejora
              ? `Estás editando el borrador de mejora v${versionHorario}. El horario publicado sigue vigente. Si publicas este borrador, esa versión queda archivada.`
              : 'Este horario está publicado. Las mejoras se hacen en un borrador y el publicado sigue vigente hasta que publiques esa versión.'}
          </p>
          <div className={styles.draftActions}>
            {esBorradorMejora ? (
              <>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={descartandoBorrador || creandoBorrador}
                  onClick={() => setDescartarAbierto(true)}
                >
                  Descartar borrador
                </Button>
                <Button variant="primary" size="sm" onClick={() => navigate('/dashboard/publicacion')}>
                  Ir a publicación
                </Button>
              </>
            ) : (
              <Button
                variant="primary"
                size="sm"
                disabled={creandoBorrador}
                onClick={() => void crearBorrador()}
              >
                {creandoBorrador ? 'Creando borrador…' : 'Crear borrador de mejora'}
              </Button>
            )}
          </div>
        </div>
      ) : null}
      {mejoraAviso ? <p className={styles.success}>{mejoraAviso}</p> : null}
      {mejoraError ? (
        <p className={styles.alert} role="alert">
          {mejoraError}
        </p>
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
            {jornadasVisibles.map((jornada) =>
              jornada.franjas.length === 0 ? (
                <p key={jornada.turnoId} className={styles.notice}>
                  El turno {jornada.nombre} no tiene franjas horarias. Créalas en Franjas
                  horarias para programar sus clases.
                </p>
              ) : (
                <ScheduleGrid
                  key={jornada.turnoId}
                  groupCode={groupCode}
                  groupId={grupo.id}
                  shiftLabel={jornada.nombre}
                  readOnly={readOnly}
                  bands={jornada.franjas}
                  blocks={blocks}
                  onProgram={(day, bandId) => openCreate(day, bandId)}
                  onEdit={(placementId) => {
                    setSaveError('')
                    setRemoveId(null)
                    setClassDialog({ name: 'edit', placementId })
                  }}
                />
              ),
            )}
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
      <ConfirmActionModal
        open={descartarAbierto}
        title="Descartar borrador"
        description="Se elimina el borrador de mejora y vuelves a ver el horario publicado, que sigue vigente."
        confirmLabel="Descartar borrador"
        busyLabel="Descartando..."
        submitting={descartandoBorrador}
        error={mejoraError}
        onClose={() => {
          if (!descartandoBorrador) {
            setDescartarAbierto(false)
          }
        }}
        onConfirm={() => void descartarBorrador()}
      />
    </section>
  )
}
