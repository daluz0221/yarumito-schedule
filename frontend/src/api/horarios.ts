import type { PendingPlacement, ScheduleBlock } from '../content/scheduleConstruction'
import type { TimeBand, WeekDay } from '../content/teacherRestrictions'
import { api } from './client'
import type { FranjaHorariaResponse } from './franjasHorarias'
import { horaCorta } from './turnos'

export type EstadoHorario = 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO'

export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES'

export type ResumenHorarioResponse = {
  asignacionesProgramadas: number
  totalAsignaciones: number
  horasProgramadas: number
  totalHoras: number
  horasPendientes: number
  conflictos: number
  advertencias: number
}

export type ClaseProgramadaResponse = {
  id: string
  asignacionAcademicaId: string
  asignaturaId: string
  asignaturaNombre: string
  asignaturaAbreviatura: string
  asignaturaColorUi: string
  docenteId: string
  docenteNombre: string
  aulaId: string
  aulaNombre: string
  dia: DiaSemana
  franjaHorariaId: string
  franjaNumero: number
  franjaOrden: number
  horaInicio: string
  horaFin: string
  esHoraExtra: boolean
  bloqueada: boolean
  warnings: string[]
}

export type AsignacionPendienteResponse = {
  asignacionAcademicaId: string
  asignaturaId: string
  asignaturaNombre: string
  asignaturaAbreviatura: string
  asignaturaColorUi: string
  docenteId: string
  docenteNombre: string
  horasSemanales: number
  horasProgramadas: number
  horasPendientes: number
}

export type HorarioGrupoResponse = {
  grupoId: string
  grupoCodigo: string
  gradoId: string
  gradoNombre: string
  anioEscolarId: string
  anioEscolar: number
  sedeId: string
  sedeNombre: string
  horarioId: string | null
  versionHorario: number
  turnoId: string | null
  turnoNombre: string | null
  estadoHorario: EstadoHorario
  resumen: ResumenHorarioResponse
  franjas: FranjaHorariaResponse[]
  clasesProgramadas: ClaseProgramadaResponse[]
  asignacionesPendientes: AsignacionPendienteResponse[]
}

export type ClaseVista = ScheduleBlock & {
  locked: boolean
  aulaId: string
}

const DIAS: Record<DiaSemana, WeekDay> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
}

function horaVisible(value: string) {
  return horaCorta(value ?? '').replace(/^0(?=\d:)/, '')
}

export type CrearClaseProgramadaRequest = {
  asignacionAcademicaId: string
  dia: DiaSemana
  franjaHorariaId: string
  aulaId: string
}

export type ActualizarClaseProgramadaRequest = {
  dia: DiaSemana
  franjaHorariaId: string
  aulaId: string
}

const DIA_API: Record<WeekDay, DiaSemana> = {
  Lunes: 'LUNES',
  Martes: 'MARTES',
  Miércoles: 'MIERCOLES',
  Jueves: 'JUEVES',
  Viernes: 'VIERNES',
}

export function diaSemanaApi(day: WeekDay) {
  return DIA_API[day]
}

export function consultarHorarioGrupo(grupoId: string, signal?: AbortSignal) {
  return api.get<HorarioGrupoResponse>(`/api/horarios/grupos/${grupoId}`, { signal })
}

export function crearClaseProgramada(payload: CrearClaseProgramadaRequest) {
  return api.post<ClaseProgramadaResponse, CrearClaseProgramadaRequest>(
    '/api/clases-programadas',
    payload,
  )
}

export function actualizarClaseProgramada(id: string, payload: ActualizarClaseProgramadaRequest) {
  return api.put<ClaseProgramadaResponse, ActualizarClaseProgramadaRequest>(
    `/api/clases-programadas/${id}`,
    payload,
  )
}

export function eliminarClaseProgramada(id: string) {
  return api.delete<null>(`/api/clases-programadas/${id}`)
}

export function etiquetaEstadoHorario(estado: EstadoHorario | null | undefined) {
  if (estado === 'PUBLICADO' || estado === 'ARCHIVADO' || estado === 'BORRADOR') {
    return estado
  }

  return 'BORRADOR'
}

export function bandasDelTablero(franjas: FranjaHorariaResponse[] | null | undefined): TimeBand[] {
  return [...(franjas ?? [])]
    .sort((left, right) => left.orden - right.orden || left.numero - right.numero)
    .map((franja) => {
      const horario = `${horaVisible(franja.horaInicio)} – ${horaVisible(franja.horaFin)}`

      return {
        id: franja.id,
        kind: franja.esDescanso ? 'break' : 'class',
        label: franja.esDescanso ? `DESCANSO · ${horario} · No programable` : horario,
      }
    })
}

export function clasesDelTablero(tablero: HorarioGrupoResponse): ClaseVista[] {
  return (tablero.clasesProgramadas ?? []).flatMap((clase) => {
    const day = DIAS[clase.dia]

    if (!day || !clase.id || !clase.franjaHorariaId) {
      return []
    }

    const warnings = (clase.warnings ?? []).map((item) => item.trim()).filter(Boolean)
    const extra = clase.esHoraExtra ? 'Hora extra.' : ''
    const reason = [...warnings, extra].filter(Boolean).join(' ')

    return [
      {
        id: clase.id,
        yearId: tablero.anioEscolarId,
        groupId: tablero.grupoId,
        day,
        bandId: clase.franjaHorariaId,
        subjectKey: clase.asignacionAcademicaId,
        subject: clase.asignaturaNombre?.trim() || 'Asignatura',
        teacherName: clase.docenteNombre?.trim() || 'Docente',
        classroom: clase.aulaNombre?.trim() || 'Sin aula',
        aulaId: clase.aulaId ?? '',
        tone: warnings.length > 0 ? 'warning' : 'ok',
        reason,
        locked: Boolean(clase.bloqueada),
      },
    ]
  })
}

export function pendientesDelTablero(tablero: HorarioGrupoResponse): PendingPlacement[] {
  return (tablero.asignacionesPendientes ?? []).map((item) => ({
    key: item.asignacionAcademicaId,
    subject: item.asignaturaNombre?.trim() || 'Asignatura',
    teacherName: item.docenteNombre?.trim() || 'Docente',
    pendingHours: item.horasPendientes,
    note: null,
  }))
}

export function metricasDelTablero(resumen: ResumenHorarioResponse | null | undefined) {
  return {
    programmed: resumen?.asignacionesProgramadas ?? 0,
    assignedCount: resumen?.totalAsignaciones ?? 0,
    placedHours: resumen?.horasProgramadas ?? 0,
    assignedHours: resumen?.totalHoras ?? 0,
    pendingHours: resumen?.horasPendientes ?? 0,
    conflicts: resumen?.conflictos ?? 0,
    warnings: resumen?.advertencias ?? 0,
  }
}
