import { api } from './client'
import { horaCorta } from './turnos'

export type FranjaHorariaRequest = {
  turnoId: string
  numero: number
  horaInicio: string
  horaFin: string
  esDescanso: boolean
  horasAcademicasEquivalentes: number
  orden: number
}

export type FranjaHorariaResponse = {
  id: string
  turnoId: string
  turnoNombre: string
  numero: number
  horaInicio: string
  horaFin: string
  esDescanso: boolean
  horasAcademicasEquivalentes: number
  orden: number
}

export function minutosDelDia(value: string) {
  const match = /^(\d{2}):(\d{2})/.exec(horaCorta(value ?? ''))
  if (!match) {
    return null
  }

  return Number(match[1]) * 60 + Number(match[2])
}

export function horaDesdeMinutos(total: number) {
  if (!Number.isInteger(total) || total < 0 || total >= 24 * 60) {
    return ''
  }

  const hours = Math.floor(total / 60)
  const minutes = total % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
}

export function consultarFranjaHoraria(id: string, signal?: AbortSignal) {
  return api.get<FranjaHorariaResponse>(`/api/v1/franjas-horarias/${id}`, { signal })
}

export function registrarFranjaHoraria(payload: FranjaHorariaRequest) {
  return api.post<FranjaHorariaResponse, FranjaHorariaRequest>('/api/v1/franjas-horarias', payload)
}

export function actualizarFranjaHoraria(id: string, payload: FranjaHorariaRequest) {
  return api.put<FranjaHorariaResponse, FranjaHorariaRequest>(
    `/api/v1/franjas-horarias/${id}`,
    payload,
  )
}

export function listarFranjasPorTurno(turnoId: string, signal?: AbortSignal) {
  const search = new URLSearchParams({ turnoId })
  return api.get<FranjaHorariaResponse[]>(`/api/v1/franjas-horarias?${search.toString()}`, {
    signal,
  })
}
