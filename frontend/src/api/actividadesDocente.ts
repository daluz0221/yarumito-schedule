import { api } from './client'

export type TipoActividadInstitucional =
  | 'PROYECTO_PEDAGOGICO'
  | 'ACTIVIDAD_INSTITUCIONAL'
  | 'DIRECCION_DE_GRUPO'
  | 'COMITE'
  | 'OTRO'

export type CrearAsignacionActividadRequest = {
  actividadId: string
  horasSemanales: number
  justificacion: string
}

export type AsignacionActividadResponse = {
  id: string
  docenteId: string
  docenteNombre: string
  actividadId: string
  actividadNombre: string
  tipoActividad: TipoActividadInstitucional
  horasSemanales: number
  justificacion: string
}

export function listarActividadesDocente(docenteId: string, signal?: AbortSignal) {
  return api.get<AsignacionActividadResponse[]>(`/api/docentes/${docenteId}/actividades`, {
    signal,
  })
}

export function asignarActividadDocente(
  docenteId: string,
  payload: CrearAsignacionActividadRequest,
) {
  return api.post<AsignacionActividadResponse, CrearAsignacionActividadRequest>(
    `/api/docentes/${docenteId}/actividades`,
    payload,
  )
}
