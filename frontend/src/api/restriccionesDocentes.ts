import type { RestrictionKind, WeekDay } from '../content/teacherRestrictions'
import { api } from './client'

export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES'

export type TipoRestriccionDocente = 'NO_DISPONIBLE' | 'PREFERENTE'

export type RestriccionDocenteResponse = {
  id: string
  docenteId: string
  anioEscolarId: string
  dia: DiaSemana
  franjaHorariaId: string
  tipo: TipoRestriccionDocente
  motivo: string | null
  documentoSoporte: string | null
  aprobadaPorId: string | null
}

export type CrearRestriccionDocenteRequest = {
  docenteId: string
  anioEscolarId: string
  dia: DiaSemana
  franjaHorariaId: string
  tipo: TipoRestriccionDocente
  motivo: string
  documentoSoporte: string | null
}

export type ActualizarRestriccionDocenteRequest = {
  tipo: TipoRestriccionDocente
  motivo: string
  documentoSoporte: string | null
}

export type RestriccionDocenteVista = {
  id: string
  docenteId: string
  anioEscolarId: string
  day: WeekDay
  bandId: string
  status: RestrictionKind
  motivo: string
  documentoSoporte: string | null
  aprobadaPorId: string | null
}

const DIA_VISTA: Record<DiaSemana, WeekDay> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
}

const DIA_API: Record<WeekDay, DiaSemana> = {
  Lunes: 'LUNES',
  Martes: 'MARTES',
  Miércoles: 'MIERCOLES',
  Jueves: 'JUEVES',
  Viernes: 'VIERNES',
}

export function diaApi(day: WeekDay) {
  return DIA_API[day]
}

export function tipoApi(status: RestrictionKind): TipoRestriccionDocente {
  return status === 'PREFERENCIA' ? 'PREFERENTE' : 'NO_DISPONIBLE'
}

function documento(value: unknown) {
  return typeof value === 'string' && value.trim() ? value.trim() : null
}

export function aRestriccionVista(dto: RestriccionDocenteResponse): RestriccionDocenteVista | null {
  const day = DIA_VISTA[dto?.dia]
  const status =
    dto?.tipo === 'PREFERENTE'
      ? 'PREFERENCIA'
      : dto?.tipo === 'NO_DISPONIBLE'
        ? 'NO_DISPONIBLE'
        : null

  if (!dto?.id || !day || !status || !dto.franjaHorariaId) {
    return null
  }

  return {
    id: dto.id,
    docenteId: dto.docenteId,
    anioEscolarId: dto.anioEscolarId,
    day,
    bandId: dto.franjaHorariaId,
    status,
    motivo: dto.motivo?.trim() ?? '',
    documentoSoporte: documento(dto.documentoSoporte),
    aprobadaPorId: dto.aprobadaPorId ?? null,
  }
}

function listar(path: string, signal?: AbortSignal) {
  return api.get<RestriccionDocenteResponse[]>(path, { signal }).then((items) =>
    (Array.isArray(items) ? items : []).flatMap((item) => {
      const vista = aRestriccionVista(item)
      return vista ? [vista] : []
    }),
  )
}

export function consultarRestriccion(id: string, signal?: AbortSignal) {
  return api.get<RestriccionDocenteResponse>(`/api/v1/restricciones-docentes/${id}`, { signal })
}

export function listarRestriccionesPorDocenteYAnio(
  docenteId: string,
  anioEscolarId: string,
  signal?: AbortSignal,
) {
  return listar(
    `/api/v1/restricciones-docentes/docente/${docenteId}/anio/${anioEscolarId}`,
    signal,
  )
}

export function listarRestriccionesPorAnio(anioEscolarId: string, signal?: AbortSignal) {
  return listar(`/api/v1/restricciones-docentes/anio/${anioEscolarId}`, signal)
}

export function registrarRestriccion(payload: CrearRestriccionDocenteRequest) {
  return api.post<RestriccionDocenteResponse, CrearRestriccionDocenteRequest>(
    '/api/v1/restricciones-docentes',
    payload,
  )
}

export function actualizarRestriccion(id: string, payload: ActualizarRestriccionDocenteRequest) {
  return api.put<RestriccionDocenteResponse, ActualizarRestriccionDocenteRequest>(
    `/api/v1/restricciones-docentes/${id}`,
    payload,
  )
}
