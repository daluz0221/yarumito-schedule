import { api } from './client'

export type SedeResponse = {
  id: string
  nombre: string
  codigo: string
  direccion: string
  esPrincipal: boolean
}

type PaginaSedes = {
  content: SedeResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaSedes>(`/api/v1/sedes?${search.toString()}`, { signal })
}

export async function listarCatalogoSedes(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const sedes = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    sedes.push(...(next.content ?? []))
  }

  return sedes
}

export type SedeRequest = {
  nombre: string
  codigo: string
  direccion: string | null
  esPrincipal: boolean
}

export function consultarSede(id: string, signal?: AbortSignal) {
  return api.get<SedeResponse>(`/api/v1/sedes/${id}`, { signal })
}

export function registrarSede(payload: SedeRequest) {
  return api.post<SedeResponse, SedeRequest>('/api/v1/sedes', payload)
}

export function actualizarSede(id: string, payload: SedeRequest) {
  return api.put<SedeResponse, SedeRequest>(`/api/v1/sedes/${id}`, payload)
}

export function sedeVisible(sedes: SedeResponse[]) {
  return sedes.find((sede) => sede.esPrincipal) ?? sedes[0] ?? null
}

export function opcionesCatalogoSedes(sedes: SedeResponse[] | null, error: string) {
  if (error) {
    return [{ value: '', label: 'Sede no disponible' }]
  }

  if (sedes === null) {
    return [{ value: '', label: 'Cargando sedes…' }]
  }

  if (sedes.length === 0) {
    return [{ value: '', label: 'Sin sedes registradas' }]
  }

  return sedes.map((sede) => ({
    value: sede.id,
    label: sede.nombre,
  }))
}

export function etiquetaCatalogoSedes(sedes: SedeResponse[] | null, error: string) {
  if (error) {
    return 'Sede no disponible'
  }

  if (sedes === null) {
    return 'Cargando sedes…'
  }

  return sedeVisible(sedes)?.nombre ?? 'Sin sedes registradas'
}
