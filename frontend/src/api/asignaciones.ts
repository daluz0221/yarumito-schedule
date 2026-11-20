import { api } from './client'

export type EstadoAsignacionAcademica = 'BORRADOR' | 'CONFIRMADA'

export type CrearAsignacionAcademicaRequest = {
  anioEscolarId: string
  docenteId: string
  grupoId: string
  asignaturaId: string
  horasSemanales: number
  observacion?: string | null
}

export type AsignacionAcademicaResponse = {
  id: string
  anioEscolarId: string
  anioEscolar: number
  docenteId: string
  docenteNombre: string
  grupoId: string
  grupoCodigo: string
  asignaturaId: string
  asignaturaNombre: string
  horasSemanales: number
  horasExtras: number
  estado: EstadoAsignacionAcademica
  observacion: string | null
  asignadaPorId: string | null
  asignadaPorCorreo: string | null
}

export function registrarAsignacion(payload: CrearAsignacionAcademicaRequest) {
  return api.post<AsignacionAcademicaResponse, CrearAsignacionAcademicaRequest>(
    '/api/asignaciones',
    payload,
  )
}

export function listarBorradores(signal?: AbortSignal) {
  return api.get<AsignacionAcademicaResponse[]>('/api/asignaciones/borradores', { signal })
}

export function consultarAsignacion(id: string, signal?: AbortSignal) {
  return api.get<AsignacionAcademicaResponse>(`/api/asignaciones/${id}`, { signal })
}

export function listarAsignacionesPorDocente(docenteId: string, signal?: AbortSignal) {
  return api.get<AsignacionAcademicaResponse[]>(`/api/asignaciones/docente/${docenteId}`, {
    signal,
  })
}

export function listarAsignacionesPorGrupo(grupoId: string, signal?: AbortSignal) {
  return api.get<AsignacionAcademicaResponse[]>(`/api/asignaciones/grupo/${grupoId}`, { signal })
}

export function confirmarAsignacion(id: string) {
  return api.patch<AsignacionAcademicaResponse>(`/api/asignaciones/${id}/confirmar`)
}

export type ActualizarAsignacionRequest = {
  docenteId: string
  horasSemanales: number
  observacion?: string | null
}

// H-04: solo asignaciones en BORRADOR.
export function actualizarAsignacion(id: string, payload: ActualizarAsignacionRequest) {
  return api.put<AsignacionAcademicaResponse, ActualizarAsignacionRequest>(
    `/api/asignaciones/${id}`,
    payload,
  )
}

// H-04: retira una asignación en BORRADOR sin clases programadas.
export function retirarAsignacion(id: string) {
  return api.delete<void>(`/api/asignaciones/${id}`)
}
