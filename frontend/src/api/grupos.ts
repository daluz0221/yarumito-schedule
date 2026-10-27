import { etiquetaGrado, gradoPorId, planDesdeGrado, type GradoResponse } from './grados'
import type { SchoolGroup, SchoolLevel } from '../content/academicYear'
import { api } from './client'

export type CrearGrupoRequest = {
  codigo: string
  gradoId: string
  anioEscolarId: string
  sedeId: string
  directorGrupoId?: string
  cantidadEstudiantes?: number
  aulaFijaId?: string
  activo: boolean
}

export type GrupoResponse = {
  id: string
  codigo: string
  gradoId: string
  gradoNivel: number
  gradoNombre: string
  anioEscolarId: string
  anioEscolar: number
  sedeId: string
  sedeNombre: string
  directorGrupoId: string | null
  directorGrupoNombre: string | null
  cantidadEstudiantes: number | null
  aulaFijaId: string | null
  activo: boolean
}

type PaginaGrupos = {
  content: GrupoResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaGrupos>(`/api/v1/grupos?${search.toString()}`, { signal })
}

function esMedia(dto: GrupoResponse, grado: GradoResponse | null): SchoolLevel {
  if (grado) {
    return grado.esMedia ? 'MEDIA' : 'BACHILLERATO'
  }

  return dto.gradoNivel >= 10 ? 'MEDIA' : 'BACHILLERATO'
}

export function aSchoolGroup(
  dto: GrupoResponse,
  grados: GradoResponse[] | null,
): SchoolGroup | null {
  if (!dto?.id || !dto.anioEscolarId || !dto.codigo?.trim()) {
    return null
  }

  const grado = grados ? gradoPorId(grados, dto.gradoId) : null
  const plan = grado ? planDesdeGrado(grado) : null

  return {
    id: dto.id,
    yearId: dto.anioEscolarId,
    schoolYear: Number.isFinite(dto.anioEscolar) ? dto.anioEscolar : undefined,
    code: dto.codigo.trim(),
    grade: etiquetaGrado(dto.gradoNombre || grado?.nombre || ''),
    level: esMedia(dto, grado),
    weeklyHours: plan?.weeklyHours ?? null,
    students: dto.cantidadEstudiantes ?? 0,
    director: dto.directorGrupoNombre?.trim() || null,
    status: dto.activo ? 'ACTIVO' : 'INACTIVO',
    sedeId: dto.sedeId || undefined,
    sedeNombre: dto.sedeNombre?.trim() || undefined,
  }
}

export function gruposVisibles(
  grupos: GrupoResponse[] | null,
  grados: GradoResponse[] | null,
) {
  return (grupos ?? []).flatMap((dto) => {
    const group = aSchoolGroup(dto, grados)
    return group ? [group] : []
  })
}

export function registrarGrupo(payload: CrearGrupoRequest) {
  return api.post<GrupoResponse, CrearGrupoRequest>('/api/v1/grupos', payload)
}

export async function listarCatalogoGrupos(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const grupos = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    grupos.push(...(next.content ?? []))
  }

  return grupos.sort(
    (left, right) =>
      (left.gradoNivel ?? 0) - (right.gradoNivel ?? 0) ||
      (left.codigo ?? '').localeCompare(right.codigo ?? '', 'es'),
  )
}
