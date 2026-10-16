import { api } from './client'

export type PlanEstudiosResponse = {
  id: string
  anioEscolarId: string
  anioEscolar: number
  gradoId: string
  gradoNivel: number
  gradoNombre: string
  asignaturaId: string
  asignaturaNombre: string
  asignaturaCodigo: string
  horasSemanales: number
  turnoId: string | null
  observacion: string | null
}

type PaginaPlanesEstudio = {
  content: PlanEstudiosResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaPlanesEstudio>(`/api/v1/planes-estudio?${search.toString()}`, {
    signal,
  })
}

export async function listarPlanesEstudio(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const planes = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    planes.push(...(next.content ?? []))
  }

  return planes
}
