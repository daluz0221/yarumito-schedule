import { api } from './client'
import type { TipoAulaRequerida } from '../content/subjects'

export type AsignaturaResponse = {
  id: string
  areaId: string
  nombre: string
  codigo: string
  abreviatura?: string
  colorUi?: string | null
  exigeIdoneidadEstricta: boolean
  esMediaTecnica: boolean
  requiereDocenteExclusivo: boolean
  tipoAulaRequerida?: TipoAulaRequerida | null
  maxClasesConsecutivas: number
  activa: boolean
}

export type ListarAsignaturasQuery = {
  texto?: string
  activa?: boolean
  areaId?: string
}

export type CrearAsignaturaRequest = {
  areaId: string
  nombre: string
  codigo: string
  abreviatura: string | null
  colorUi: string | null
  exigeIdoneidadEstricta: boolean
  esMediaTecnica: boolean
  requiereDocenteExclusivo: boolean
  tipoAulaRequerida: TipoAulaRequerida | null
  maxClasesConsecutivas: number
  activa: boolean
}

type PaginaAsignaturas = {
  content: AsignaturaResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function buildAsignaturasPath(
  query: ListarAsignaturasQuery & { page: number; size: number },
) {
  const search = new URLSearchParams()
  const texto = query.texto?.trim()

  if (texto) {
    search.set('texto', texto)
  }

  if (typeof query.activa === 'boolean') {
    search.set('activa', String(query.activa))
  }

  if (query.areaId) {
    search.set('areaId', query.areaId)
  }

  search.set('page', String(query.page))
  search.set('size', String(query.size))

  return `/api/v1/asignaturas?${search.toString()}`
}

function listarPagina(
  query: ListarAsignaturasQuery,
  page: number,
  signal?: AbortSignal,
) {
  return api.get<PaginaAsignaturas>(
    buildAsignaturasPath({ ...query, page, size: PAGE_SIZE }),
    { signal },
  )
}

async function listarTodas(query: ListarAsignaturasQuery, signal?: AbortSignal) {
  const first = await listarPagina(query, 0, signal)
  const asignaturas = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(query, page, signal)
    asignaturas.push(...(next.content ?? []))
  }

  return asignaturas
}

export function listarAsignaturas(
  areaId: string,
  signal?: AbortSignal,
  query: Omit<ListarAsignaturasQuery, 'areaId'> = {},
) {
  return listarTodas({ ...query, areaId }, signal)
}

export function consultarAsignatura(id: string, signal?: AbortSignal) {
  return api.get<AsignaturaResponse>(`/api/v1/asignaturas/${id}`, { signal })
}

export function registrarAsignatura(payload: CrearAsignaturaRequest) {
  return api.post<AsignaturaResponse, CrearAsignaturaRequest>(
    '/api/v1/asignaturas',
    payload,
  )
}

export type ActualizarAsignaturaRequest = CrearAsignaturaRequest

export function actualizarAsignatura(id: string, payload: ActualizarAsignaturaRequest) {
  return api.put<AsignaturaResponse, ActualizarAsignaturaRequest>(
    `/api/v1/asignaturas/${id}`,
    payload,
  )
}

export function cambiarEstadoAsignatura(id: string, activa: boolean) {
  const search = new URLSearchParams({ activa: String(activa) })
  return api.patch<AsignaturaResponse>(
    `/api/v1/asignaturas/${id}/estado?${search.toString()}`,
  )
}
