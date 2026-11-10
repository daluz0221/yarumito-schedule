import { api } from './client'

export type TurnoRequest = {
  nombre: string
  horaInicio: string
  horaFin: string
  duracionClaseMinutos: number
  clasesPorDia: number
  clasesAntesDeDescanso: number
  duracionDescansoMinutos: number
}

export type TurnoResponse = {
  id: string
  nombre: string
  horaInicio: string
  horaFin: string
  duracionClaseMinutos: number
  clasesPorDia: number
  clasesAntesDeDescanso: number
  duracionDescansoMinutos: number
}

type PaginaTurnos = {
  content: TurnoResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaTurnos>(`/api/v1/turnos?${search.toString()}`, { signal })
}

export function horaCorta(value: string) {
  const match = /^(\d{2}:\d{2})/.exec(value ?? '')
  return match?.[1] ?? value
}

export function consultarTurno(id: string, signal?: AbortSignal) {
  return api.get<TurnoResponse>(`/api/v1/turnos/${id}`, { signal })
}

export function registrarTurno(payload: TurnoRequest) {
  return api.post<TurnoResponse, TurnoRequest>('/api/v1/turnos', payload)
}

export function actualizarTurno(id: string, payload: TurnoRequest) {
  return api.put<TurnoResponse, TurnoRequest>(`/api/v1/turnos/${id}`, payload)
}

export async function listarCatalogoTurnos(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const turnos = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    turnos.push(...(next.content ?? []))
  }

  return turnos.sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'))
}
