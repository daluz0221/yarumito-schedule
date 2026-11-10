import { api } from './client'

export type AreaResponse = {
  id: string
  nombre: string
  codigo: string
  obligatoria?: boolean
  soloMedia?: boolean
  activa?: boolean
}

export type ListarAreasQuery = {
  texto?: string
  activa?: boolean
}

export type CrearAreaRequest = {
  nombre: string
  codigo: string
  obligatoria: boolean
  soloMedia: boolean
  activa: boolean
}

type PaginaAreas = {
  content: AreaResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function buildAreasPath(query: ListarAreasQuery & { page: number; size: number }) {
  const search = new URLSearchParams()
  const texto = query.texto?.trim()

  if (texto) {
    search.set('texto', texto)
  }

  if (typeof query.activa === 'boolean') {
    search.set('activa', String(query.activa))
  }

  search.set('page', String(query.page))
  search.set('size', String(query.size))

  return `/api/v1/areas?${search.toString()}`
}

function listarPagina(query: ListarAreasQuery, page: number, signal?: AbortSignal) {
  return api.get<PaginaAreas>(
    buildAreasPath({ ...query, page, size: PAGE_SIZE }),
    { signal },
  )
}

async function listarTodas(query: ListarAreasQuery, signal?: AbortSignal) {
  const first = await listarPagina(query, 0, signal)
  const areas = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(query, page, signal)
    areas.push(...(next.content ?? []))
  }

  return areas
}

export function listarAreas(signal?: AbortSignal, query: ListarAreasQuery = {}) {
  return listarTodas(query, signal)
}

export function listarCatalogoAreas(signal?: AbortSignal) {
  return listarTodas({}, signal)
}

export type ActualizarAreaRequest = CrearAreaRequest

export function registrarArea(payload: CrearAreaRequest) {
  return api.post<AreaResponse, CrearAreaRequest>('/api/v1/areas', payload)
}

export function consultarArea(id: string, signal?: AbortSignal) {
  return api.get<AreaResponse>(`/api/v1/areas/${id}`, { signal })
}

export function actualizarArea(id: string, payload: ActualizarAreaRequest) {
  return api.put<AreaResponse, ActualizarAreaRequest>(
    `/api/v1/areas/${id}`,
    payload,
  )
}

export function cambiarEstadoArea(id: string, activa: boolean) {
  const search = new URLSearchParams({ activa: String(activa) })
  return api.patch<AreaResponse>(`/api/v1/areas/${id}/estado?${search.toString()}`)
}

export function toAreaSelectOptions(areas: AreaResponse[]) {
  return [
    { value: '', label: 'Seleccione un área' },
    ...areas.map((area) => ({
      value: area.id,
      label: area.nombre,
    })),
  ]
}
