import { api } from './client'

export type TipoAula = 'AULA' | 'LABORATORIO' | 'SALA_SISTEMAS' | 'CANCHA'

export type AulaRequest = {
  sedeId: string
  nombre: string
  tipo: TipoAula
  capacidad: number | null
}

export type ActualizarAulaRequest = AulaRequest & {
  activa: boolean
}

export type AulaResponse = {
  id: string
  sedeId: string
  sedeNombre: string
  nombre: string
  tipo: TipoAula
  capacidad: number | null
  activa: boolean
}

export const tiposAula: { value: TipoAula; label: string }[] = [
  { value: 'AULA', label: 'Aula' },
  { value: 'LABORATORIO', label: 'Laboratorio' },
  { value: 'SALA_SISTEMAS', label: 'Sala de sistemas' },
  { value: 'CANCHA', label: 'Cancha' },
]

export function etiquetaTipoAula(tipo: string) {
  return tiposAula.find((item) => item.value === tipo)?.label ?? tipo
}

export function listarAulasPorSede(sedeId: string, signal?: AbortSignal) {
  const search = new URLSearchParams({ sedeId })
  return api.get<AulaResponse[]>(`/api/v1/aulas?${search.toString()}`, { signal })
}

export function consultarAula(id: string, signal?: AbortSignal) {
  return api.get<AulaResponse>(`/api/v1/aulas/${id}`, { signal })
}

export function registrarAula(payload: AulaRequest) {
  return api.post<AulaResponse, AulaRequest>('/api/v1/aulas', payload)
}

export function actualizarAula(id: string, payload: ActualizarAulaRequest) {
  return api.put<AulaResponse, ActualizarAulaRequest>(`/api/v1/aulas/${id}`, payload)
}

export function registrarNuevaVersionAula(id: string, payload: AulaRequest) {
  return api.post<AulaResponse, AulaRequest>(`/api/v1/aulas/${id}/nueva-version`, payload)
}

export function eliminarAula(id: string) {
  return api.delete<null>(`/api/v1/aulas/${id}`)
}
