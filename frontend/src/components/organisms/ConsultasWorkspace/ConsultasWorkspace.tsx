import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { useAuth } from '../../../auth'
import {
  anioEscolarVisible,
  ApiError,
  consultarHorarioGrupo,
  consultarInformeHorario,
  descargarInformeHorario,
  etiquetaGrado,
  horaCorta,
  listarDocentes,
  minutosDelDia,
  opcionesAniosEscolares,
  opcionesCatalogoSedes,
  sedeVisible,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
  useCatalogoSedes,
  type FormatoInformeHorario,
  type FranjaHorariaResponse,
  type InformeHorarioClaseDetalle,
  type InformeHorariosFiltroRequest,
  type InformeHorariosResponse,
  type VersionInformeHorario,
} from '../../../api'
import { blockKey, type ScheduleBlock } from '../../../content/scheduleConstruction'
import type { TimeBand, WeekDay } from '../../../content/teacherRestrictions'
import { idDisponible } from '../../../content/idDisponible'
import type { Teacher } from '../../../content/teachers'
import { Badge, type BadgeTone } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { StatCard } from '../../molecules/StatCard'
import { ScheduleGrid } from '../ScheduleGrid'
import styles from './ConsultasWorkspace.module.css'

const DIAS = [
  { value: '', label: 'Todos los días' },
  { value: 'LUNES', label: 'Lunes' },
  { value: 'MARTES', label: 'Martes' },
  { value: 'MIERCOLES', label: 'Miércoles' },
  { value: 'JUEVES', label: 'Jueves' },
  { value: 'VIERNES', label: 'Viernes' },
]

const JORNADAS = [
  { value: '', label: 'Todas las jornadas' },
  { value: 'REGULAR', label: 'Regular' },
  { value: 'CONTRAJORNADA', label: 'Contrajornada' },
]

const ESTADOS = [
  { value: '', label: 'Cualquier estado' },
  { value: 'BORRADOR', label: 'Borrador' },
  { value: 'PUBLICADO', label: 'Publicado' },
  { value: 'ARCHIVADO', label: 'Archivado' },
]

const VERSIONES = [
  { value: 'institucional', label: 'Horario institucional' },
  { value: 'publicado', label: 'Horario publicado' },
]

const ETIQUETA_DIA: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  'MIÉRCOLES': 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
}

type Consulta =
  | { estado: 'inicial' }
  | { estado: 'cargando' }
  | {
      estado: 'listo'
      informe: InformeHorariosResponse
      version: VersionInformeHorario
      grupoId: string
      grupoCodigo: string
      turnoNombre: string
      franjas: FranjaHorariaResponse[]
    }
  | { estado: 'error'; mensaje: string }

function esAbort(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  if (
    cause instanceof ApiError &&
    cause.message &&
    cause.message !== 'No se pudo completar la solicitud'
  ) {
    return cause.message
  }

  return fallback
}

function texto(value: string | null | undefined) {
  const limpio = value?.trim()
  return limpio ? limpio : '—'
}

function numero(value: number | null | undefined) {
  return value ?? 0
}

function etiquetaDia(dia: string | null) {
  if (!dia) {
    return '—'
  }

  return ETIQUETA_DIA[dia.trim().toUpperCase()] ?? dia
}

function etiquetaJornada(jornada: string | null) {
  if (!jornada) {
    return '—'
  }

  if (jornada.toUpperCase() === 'CONTRAJORNADA') {
    return 'Contrajornada'
  }

  if (jornada.toUpperCase() === 'REGULAR') {
    return 'Regular'
  }

  return jornada
}

function etiquetaEstado(estado: string | null) {
  if (estado === 'PUBLICADO') {
    return 'Publicado'
  }

  if (estado === 'ARCHIVADO') {
    return 'Archivado'
  }

  if (estado === 'BORRADOR') {
    return 'Borrador'
  }

  return estado?.trim() || '—'
}

function tonoEstado(estado: string | null): BadgeTone {
  if (estado === 'PUBLICADO') {
    return 'success'
  }

  if (estado === 'ARCHIVADO') {
    return 'neutral'
  }

  return 'warning'
}

function hora(value: string | null) {
  return value ? horaCorta(value) : '—'
}

function horaVisible(value: string | null) {
  return hora(value).replace(/^0(?=\d:)/, '')
}

const DIA_SEMANA: Record<string, WeekDay> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  MIÉRCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
}

function rangoHorario(inicio: string | null, fin: string | null) {
  const desde = minutosDelDia(inicio ?? '')
  const hasta = minutosDelDia(fin ?? '')

  if (desde === null || hasta === null) {
    return null
  }

  return { desde, hasta, clave: `${desde}-${hasta}` }
}

function parrillaDeConsulta(
  clases: InformeHorarioClaseDetalle[],
  franjas: FranjaHorariaResponse[],
  groupId: string,
) {
  const oficiales = [...franjas].sort(
    (left, right) => left.orden - right.orden || left.numero - right.numero,
  )
  const bandaPorRango = new Map<string, string>()
  const bandas: Array<{ banda: TimeBand; desde: number }> = oficiales.map((franja) => {
    const rango = rangoHorario(franja.horaInicio, franja.horaFin)
    const horario = `${horaVisible(franja.horaInicio)} – ${horaVisible(franja.horaFin)}`

    if (rango && !franja.esDescanso) {
      bandaPorRango.set(rango.clave, franja.id)
    }

    return {
      desde: rango?.desde ?? Number.MAX_SAFE_INTEGER,
      banda: {
        id: franja.id,
        kind: franja.esDescanso ? 'break' : 'class',
        label: franja.esDescanso ? `DESCANSO · ${horario} · No programable` : horario,
      },
    }
  })
  const blocks = new Map<string, ScheduleBlock[]>()

  clases.forEach((clase, index) => {
    const day = DIA_SEMANA[(clase.diaSemana ?? '').trim().toUpperCase()]
    const rango = rangoHorario(clase.horaInicio, clase.horaFin)

    if (!day || !rango) {
      return
    }

    let bandId = bandaPorRango.get(rango.clave)

    if (!bandId) {
      bandId = `consulta-${rango.clave}`

      if (!bandas.some((item) => item.banda.id === bandId)) {
        bandas.push({
          desde: rango.desde,
          banda: {
            id: bandId,
            kind: 'class',
            label: `${horaVisible(clase.horaInicio)} – ${horaVisible(clase.horaFin)}`,
          },
        })
      }
    }

    const key = blockKey(day, bandId)
    const actuales = blocks.get(key) ?? []
    actuales.push({
      id: `${clase.horarioId ?? 'clase'}-${clase.asignaturaId ?? index}-${rango.clave}-${index}`,
      yearId: '',
      groupId: clase.grupoId ?? groupId,
      day,
      bandId,
      subjectKey: clase.asignaturaId ?? '',
      subject: clase.nombreAsignatura?.trim() || 'Asignatura',
      teacherName: clase.nombreDocente?.trim() || 'Docente',
      classroom: clase.nombreAula?.trim() || 'Sin aula',
      tone: 'ok',
      reason: clase.esHoraExtra ? 'Hora extra' : '',
    })
    blocks.set(key, actuales)
  })

  bandas.sort((left, right) => left.desde - right.desde)

  return {
    bands: bandas.map((item) => item.banda),
    blocks,
  }
}

function filtrosDe(valores: {
  anioEscolarId: string
  grupoId: string
  docenteId: string
  dia: string
  jornada: string
  estado: string
}): InformeHorariosFiltroRequest {
  const filtros: InformeHorariosFiltroRequest = {}

  if (valores.anioEscolarId) {
    filtros.anioEscolarId = valores.anioEscolarId
  }

  if (valores.grupoId) {
    filtros.grupoId = valores.grupoId
  }

  if (valores.docenteId) {
    filtros.docenteId = valores.docenteId
  }

  if (valores.dia) {
    filtros.dia = valores.dia
  }

  if (valores.jornada) {
    filtros.jornada = valores.jornada
  }

  if (valores.estado) {
    filtros.estado = valores.estado
  }

  return filtros
}

function nombreDocente(docente: Teacher) {
  return `${docente.apellidos} ${docente.nombres}`.trim()
}

async function docentesParaFiltro(signal: AbortSignal) {
  const size = 100
  const primera = await listarDocentes({ page: 0, size }, signal)
  const docentes = [...primera.content]
  const paginas = Math.ceil(primera.total / size)

  for (let page = 1; page < paginas; page += 1) {
    const siguiente = await listarDocentes({ page, size }, signal)
    docentes.push(...siguiente.content)
  }

  return docentes.sort((left, right) =>
    nombreDocente(left).localeCompare(nombreDocente(right), 'es'),
  )
}

export function ConsultasWorkspace() {
  const { user } = useAuth()
  const esRector = user?.rol === 'RECTOR'
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos()
  const { sedes, error: sedesError } = useCatalogoSedes()
  const [yearElegido, setYearElegido] = useState('')
  const [campusId, setCampusId] = useState('')
  const [groupElegido, setGroupElegido] = useState('')
  const [version, setVersion] = useState<VersionInformeHorario>('institucional')
  const [dia, setDia] = useState('')
  const [jornada, setJornada] = useState('')
  const [docenteId, setDocenteId] = useState('')
  const [estado, setEstado] = useState('')
  const [docentes, setDocentes] = useState<Teacher[]>([])
  const [docentesError, setDocentesError] = useState('')
  const [consulta, setConsulta] = useState<Consulta>({ estado: 'inicial' })
  const [descarga, setDescarga] = useState<FormatoInformeHorario | null>(null)
  const [descargaError, setDescargaError] = useState('')
  const consultaAbort = useRef<AbortController | null>(null)

  const yearId = idDisponible(
    yearElegido,
    anios,
    Boolean(anios?.length),
    anioEscolarVisible(anios ?? [])?.id ?? '',
  )

  const campusSeleccionada = sedes?.some((sede) => sede.id === campusId)
    ? campusId
    : (sedeVisible(sedes ?? [])?.id ?? '')

  const gruposListos = grupos !== null

  const gruposDelAnio = useMemo(
    () => (grupos ?? []).filter((group) => group.anioEscolarId === yearId && group.activo),
    [grupos, yearId],
  )

  const yearGroups = useMemo(
    () =>
      gruposDelAnio
        .filter((group) => !campusSeleccionada || group.sedeId === campusSeleccionada)
        .sort(
          (left, right) =>
            left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, 'es'),
        ),
    [campusSeleccionada, gruposDelAnio],
  )

  const groupId = idDisponible(
    groupElegido,
    yearGroups,
    Boolean(yearId) && grupos !== null,
    yearGroups[0]?.id ?? '',
  )

  useEffect(() => {
    if (!esRector) {
      return
    }

    const controller = new AbortController()

    docentesParaFiltro(controller.signal)
      .then((items) => {
        setDocentes(items)
        setDocentesError('')
      })
      .catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setDocentes([])
        setDocentesError(mensajeError(cause, 'No se pudo cargar el listado de docentes.'))
      })

    return () => controller.abort()
  }, [esRector])

  const docenteActivo =
    docenteId && docentes.some((docente) => docente.id === docenteId) ? docenteId : ''
  const consultaClave = `${groupId}|${version}`
  const [consultaVista, setConsultaVista] = useState(consultaClave)

  if (consultaVista !== consultaClave) {
    setConsultaVista(consultaClave)
    setConsulta({ estado: 'inicial' })
    setDescargaError('')
  }

  useEffect(() => () => consultaAbort.current?.abort(), [])

  useEffect(() => {
    consultaAbort.current?.abort()
  }, [groupId, version])

  const grupo = yearGroups.find((item) => item.id === groupId) ?? null
  const year = anios?.find((item) => item.id === yearId) ?? null
  const sinGrupos = gruposListos && yearGroups.length === 0
  const ocupado = consulta.estado === 'cargando' || descarga !== null
  const puedeConsultar = esRector && Boolean(groupId) && !ocupado

  const valoresFiltro = useMemo(
    () => ({
      anioEscolarId: yearId,
      grupoId: groupId,
      docenteId: docenteActivo,
      dia,
      jornada,
      estado,
    }),
    [dia, docenteActivo, estado, groupId, jornada, yearId],
  )

  function consultar(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()

    if (!groupId || !esRector) {
      return
    }

    consultaAbort.current?.abort()

    const grupoSolicitado = groupId
    const versionSolicitada = version
    const controller = new AbortController()
    consultaAbort.current = controller

    setConsulta({ estado: 'cargando' })
    setDescargaError('')

    const codigoSolicitado = grupo?.codigo ?? ''

    Promise.all([
      consultarInformeHorario(
        grupoSolicitado,
        versionSolicitada,
        filtrosDe(valoresFiltro),
        controller.signal,
      ),
      consultarHorarioGrupo(grupoSolicitado, controller.signal).catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          throw cause
        }

        return null
      }),
    ])
      .then(([informe, tablero]) => {
        if (controller.signal.aborted) {
          return
        }

        setConsulta({
          estado: 'listo',
          informe,
          version: versionSolicitada,
          grupoId: grupoSolicitado,
          grupoCodigo: tablero?.grupoCodigo?.trim() || codigoSolicitado,
          turnoNombre: tablero?.turnoNombre?.trim() || tablero?.franjas?.[0]?.turnoNombre?.trim() || '',
          franjas: tablero?.franjas ?? [],
        })
      })
      .catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setConsulta({
          estado: 'error',
          mensaje: mensajeError(
            cause,
            versionSolicitada === 'publicado'
              ? 'No se pudo consultar el horario publicado. El grupo debe tener un horario en estado publicado.'
              : 'No se pudo consultar el horario institucional de este grupo.',
          ),
        })
      })
  }

  async function descargar(formato: FormatoInformeHorario) {
    if (!groupId || !esRector || ocupado) {
      return
    }

    setDescarga(formato)
    setDescargaError('')

    try {
      await descargarInformeHorario(groupId, version, formato, filtrosDe(valoresFiltro))
    } catch (cause) {
      if (!esAbort(cause)) {
        setDescargaError(
          mensajeError(
            cause,
          version === 'publicado'
            ? `No se pudo descargar el ${formato === 'pdf' ? 'PDF' : 'Excel'}. El grupo debe tener un horario publicado.`
            : `No se pudo descargar el ${formato === 'pdf' ? 'PDF' : 'Excel'} de este grupo.`,
          ),
        )
      }
    } finally {
      setDescarga(null)
    }
  }

  const informe = consulta.estado === 'listo' ? consulta.informe : null
  const clases = informe?.clasesProgramadas ?? []
  const cargas = informe?.resumenCargaHoraria ?? []
  const tituloVersion =
    consulta.estado === 'listo' && consulta.version === 'publicado'
      ? 'Horario publicado'
      : 'Horario institucional'
  const parrilla =
    consulta.estado === 'listo'
      ? parrillaDeConsulta(clases, consulta.franjas, consulta.grupoId)
      : null
  const hayFiltroDeClase = Boolean(dia || jornada || docenteActivo || estado)

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Consultas de horario
        </Text>
        <Text variant="body">
          Consulta el horario institucional o el publicado de un grupo en la rejilla semanal, y
          descárgalo en PDF o Excel.
        </Text>
      </div>

      {!esRector ? (
        <p className={styles.alert} role="alert">
          Estos informes son exclusivos del rector. Inicia sesión con esa cuenta para consultarlos.
        </p>
      ) : null}

      <Card>
        <form className={styles.filters} onSubmit={consultar}>
          <SelectField
            id="consulta-year"
            label="Año lectivo"
            name="year"
            value={year?.id ?? ''}
            options={opcionesAniosEscolares(anios, aniosError)}
            disabled={!esRector || aniosLoading || Boolean(aniosError) || !anios?.length}
            onChange={setYearElegido}
          />
          <SelectField
            id="consulta-campus"
            label="Sede"
            name="campus"
            value={campusSeleccionada}
            options={opcionesCatalogoSedes(sedes, sedesError)}
            disabled={!esRector || Boolean(sedesError) || !sedes?.length}
            onChange={setCampusId}
          />
          <SelectField
            id="consulta-group"
            label="Grupo"
            name="group"
            value={sinGrupos || !gruposListos ? '' : groupId}
            options={
              !gruposListos
                ? [{ value: '', label: 'Cargando grupos...' }]
                : sinGrupos
                  ? [{ value: '', label: 'Sin grupos activos' }]
                  : yearGroups.map((item) => ({
                      value: item.id,
                      label: `${item.codigo} · ${etiquetaGrado(item.gradoNombre)}`,
                    }))
            }
            disabled={!esRector || gruposLoading || Boolean(gruposError) || sinGrupos}
            onChange={setGroupElegido}
          />
          <SelectField
            id="consulta-version"
            label="Versión"
            name="version"
            value={version}
            options={VERSIONES}
            disabled={!esRector}
            onChange={(value) => setVersion(value as VersionInformeHorario)}
          />
          <SelectField
            id="consulta-day"
            label="Día"
            name="day"
            value={dia}
            options={DIAS}
            disabled={!esRector}
            onChange={setDia}
          />
          <SelectField
            id="consulta-shift"
            label="Jornada"
            name="shift"
            value={jornada}
            options={JORNADAS}
            disabled={!esRector}
            onChange={setJornada}
          />
          <SelectField
            id="consulta-teacher"
            label="Docente"
            name="teacher"
            value={docenteActivo}
            options={[
              { value: '', label: 'Todos los docentes' },
              ...docentes.map((docente) => ({
                value: docente.id,
                label: nombreDocente(docente),
              })),
            ]}
            disabled={!esRector || Boolean(docentesError)}
            onChange={setDocenteId}
          />
          <SelectField
            id="consulta-status"
            label="Estado del horario"
            name="status"
            value={estado}
            options={ESTADOS}
            disabled={!esRector}
            onChange={setEstado}
          />
          <div className={styles.actions}>
            <Button type="submit" disabled={!puedeConsultar}>
              {consulta.estado === 'cargando' ? 'Consultando...' : 'Consultar informe'}
            </Button>
            <Button
              type="button"
              variant="outline"
              disabled={!puedeConsultar}
              onClick={() => descargar('pdf')}
            >
              {descarga === 'pdf' ? 'Descargando PDF...' : 'Descargar PDF'}
            </Button>
            <Button
              type="button"
              variant="outline"
              disabled={!puedeConsultar}
              onClick={() => descargar('excel')}
            >
              {descarga === 'excel' ? 'Descargando Excel...' : 'Descargar Excel'}
            </Button>
          </div>
        </form>
      </Card>

      <p className={styles.hint}>
        {version === 'publicado'
          ? 'Con «Horario publicado», los tres botones usan el horario que ya fue publicado.'
          : 'Con «Horario institucional», los tres botones usan el horario actual del grupo, en el estado que tenga.'}
        {grupo
          ? ` Grupo ${grupo.codigo} · ${etiquetaGrado(grupo.gradoNombre)}.`
          : ''}
      </p>

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
      {docentesError ? (
        <p className={styles.alert} role="alert">
          {docentesError} Puedes consultar sin filtrar por docente.
        </p>
      ) : null}
      {consulta.estado === 'error' ? (
        <p className={styles.alert} role="alert">
          {consulta.mensaje}
        </p>
      ) : null}
      {descargaError ? (
        <p className={styles.alert} role="alert">
          {descargaError}
        </p>
      ) : null}
      {consulta.estado === 'inicial' ? (
        <p className={styles.notice}>
          Elige el grupo y pulsa Consultar informe para ver el horario en la rejilla.
        </p>
      ) : null}
      {consulta.estado === 'cargando' ? (
        <p className={styles.notice}>Generando el informe del grupo...</p>
      ) : null}

      {informe && consulta.estado === 'listo' ? (
        <>
          <div className={styles.metrics} aria-label="Resumen del informe">
            <StatCard value={String(numero(informe.totalClasesProgramadas))} label="Clases" wash="total" />
            <StatCard value={String(numero(informe.totalDocentes))} label="Docentes" wash="active" />
            <StatCard
              value={String(numero(informe.totalAsignaturas))}
              label="Asignaturas"
              wash="neutral"
            />
            <StatCard
              value={String(numero(informe.horasAcademicasProgramadas))}
              label="Horas académicas"
              wash="success"
            />
            <StatCard
              value={String(numero(informe.horasAcademicasJornadaRegular))}
              label="Jornada regular"
              wash="warning"
            />
            <StatCard
              value={String(numero(informe.horasAcademicasContrajornada))}
              label="Contrajornada"
              wash="retired"
            />
          </div>

          {parrilla && consulta.estado === 'listo' && parrilla.bands.length > 0 ? (
            <>
              <ScheduleGrid
                groupCode={consulta.grupoCodigo || grupo?.codigo || '—'}
                groupId={consulta.grupoId}
                shiftLabel={consulta.turnoNombre || 'Sin turno'}
                readOnly
                lectura
                bands={parrilla.bands}
                blocks={parrilla.blocks}
              />
              {hayFiltroDeClase ? (
                <p className={styles.hint}>
                  La rejilla muestra solo las clases que cumplen los filtros de día, jornada, docente
                  o estado.
                </p>
              ) : null}
            </>
          ) : null}

          <Card className={styles.panel}>
            <SectionHeader
              title={tituloVersion}
              description={`${texto(informe.nombreInstitucion)} · ${texto(informe.nombreSede)} · Año ${texto(informe.nombreAnioEscolar)}`}
              action={<Badge tone={tonoEstado(clases[0]?.estadoHorario ?? null)}>{etiquetaEstado(clases[0]?.estadoHorario ?? null)}</Badge>}
            />
            {clases.length === 0 ? (
              <p className={styles.notice}>No hay clases para los filtros seleccionados.</p>
            ) : (
              <div className={styles.tableHost}>
                <Table minWidth={860}>
                  <thead>
                    <tr>
                      <th>Día</th>
                      <th>Hora</th>
                      <th>Asignatura</th>
                      <th>Docente</th>
                      <th>Aula</th>
                      <th>Jornada</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {clases.map((clase, index) => (
                      <tr key={`${clase.asignaturaId ?? 'clase'}-${clase.diaSemana ?? ''}-${clase.horaInicio ?? index}`}>
                        <td>{etiquetaDia(clase.diaSemana)}</td>
                        <td className={styles.hour}>
                          {hora(clase.horaInicio)} – {hora(clase.horaFin)}
                          {clase.esHoraExtra ? <span className={styles.extra}>Hora extra</span> : null}
                        </td>
                        <td>{texto(clase.nombreAsignatura)}</td>
                        <td>{texto(clase.nombreDocente)}</td>
                        <td>{texto(clase.nombreAula)}</td>
                        <td>{etiquetaJornada(clase.jornada)}</td>
                        <td>
                          <Badge tone={tonoEstado(clase.estadoHorario)}>
                            {etiquetaEstado(clase.estadoHorario)}
                          </Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </div>
            )}
          </Card>

          <Card className={styles.panel}>
            <SectionHeader
              title="Carga horaria"
              description="Horas académicas por docente, grupo y asignatura."
            />
            {cargas.length === 0 ? (
              <p className={styles.notice}>No hay carga horaria para esta consulta.</p>
            ) : (
              <div className={styles.tableHost}>
                <Table minWidth={720}>
                  <thead>
                    <tr>
                      <th>Docente</th>
                      <th>Asignatura</th>
                      <th>Grupo</th>
                      <th>Ordinarias</th>
                      <th>Extras</th>
                      <th>Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cargas.map((carga, index) => (
                      <tr key={`${carga.docenteId ?? 'docente'}-${carga.asignaturaId ?? index}`}>
                        <td>{texto(carga.nombreDocente)}</td>
                        <td>{texto(carga.nombreAsignatura)}</td>
                        <td>{texto(carga.nombreGrupo)}</td>
                        <td>{numero(carga.horasOrdinarias)}</td>
                        <td>{numero(carga.horasExtras)}</td>
                        <td>{numero(carga.totalHorasAcademicas)}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </div>
            )}
          </Card>
        </>
      ) : null}
    </section>
  )
}
