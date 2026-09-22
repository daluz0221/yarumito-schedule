import { api } from './client'
import type {
  ActualizarIdoneidadRequest,
  CrearIdoneidadRequest,
  FinalizarVigenciaIdoneidadRequest,
  Idoneidad,
  TipoIdoneidad,
} from '../content/teacherProfile'

export type IdoneidadResponse = {
  id: string
  docenteId: string
  areaId: string
  asignaturaId: string | null
  tipo: TipoIdoneidad
  tituloSoporteId: string | null
  justificacion: string | null
  vigenteDesde: string
  vigenteHasta: string | null
  aprobadaPorId: string | null
}

function textOrEmpty(value: string | null | undefined) {
  return value ?? ''
}

export function toIdoneidad(dto: IdoneidadResponse): Idoneidad {
  return {
    id: dto.id,
    docenteId: dto.docenteId,
    areaId: textOrEmpty(dto.areaId),
    asignaturaId: textOrEmpty(dto.asignaturaId),
    tipo: dto.tipo,
    tituloSoporteId: textOrEmpty(dto.tituloSoporteId),
    justificacion: textOrEmpty(dto.justificacion),
    vigenteDesde: textOrEmpty(dto.vigenteDesde),
    vigenteHasta: textOrEmpty(dto.vigenteHasta),
    aprobadaPorId: textOrEmpty(dto.aprobadaPorId),
  }
}

export function listarIdoneidadesPorDocente(
  docenteId: string,
  signal?: AbortSignal,
) {
  return api
    .get<IdoneidadResponse[]>(`/api/v1/idoneidades/docente/${docenteId}`, {
      signal,
    })
    .then((items) => (items ?? []).map(toIdoneidad))
}

export function registrarIdoneidad(payload: CrearIdoneidadRequest) {
  return api
    .post<IdoneidadResponse, CrearIdoneidadRequest>(
      '/api/v1/idoneidades',
      payload,
    )
    .then(toIdoneidad)
}

export function actualizarIdoneidad(
  id: string,
  payload: ActualizarIdoneidadRequest,
) {
  return api
    .put<IdoneidadResponse, ActualizarIdoneidadRequest>(
      `/api/v1/idoneidades/${id}`,
      payload,
    )
    .then(toIdoneidad)
}

export function finalizarVigenciaIdoneidad(
  id: string,
  payload: FinalizarVigenciaIdoneidadRequest,
) {
  return api
    .patch<IdoneidadResponse, FinalizarVigenciaIdoneidadRequest>(
      `/api/v1/idoneidades/${id}/vigencia`,
      payload,
    )
    .then(toIdoneidad)
}
