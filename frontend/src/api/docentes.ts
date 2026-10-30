import { api } from './client'
import {
  teacherStatsFromCounts,
  type ActualizarDocenteRequest,
  type CrearDocenteRequest,
  type Teacher,
  type TeacherContract,
  type TeacherStatus,
} from '../content/teachers'
import type { StatItemData } from '../content/dashboard'

export type DocenteResponse = {
  id: string
  nombres: string
  apellidos: string
  tipoDocumento: string
  numeroDocumento: string
  telefono: string | null
  correoInstitucional: string | null
  tipoVinculacion: TeacherContract
  areaNombramientoId: string | null
  numeroDecreto: string | null
  fechaDecreto: string | null
  escalafon: string | null
  horasSemanalesContratadas: number
  maxHorasExtra: number
  esExclusivoMediaTecnica: boolean
  estado: TeacherStatus
  fechaVinculacion: string | null
}

export type SpringPage<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export type ListarDocentesQuery = {
  texto?: string
  estado?: string
  tipoVinculacion?: string
  page: number
  size: number
}

export type DocentesPage = {
  content: Teacher[]
  total: number
}

function textOrEmpty(value: string | null | undefined) {
  return value ?? ''
}

export function toTeacher(dto: DocenteResponse): Teacher {
  return {
    id: dto.id,
    nombres: textOrEmpty(dto.nombres),
    apellidos: textOrEmpty(dto.apellidos),
    tipoDocumento: textOrEmpty(dto.tipoDocumento),
    numeroDocumento: textOrEmpty(dto.numeroDocumento),
    telefono: textOrEmpty(dto.telefono),
    correoInstitucional: textOrEmpty(dto.correoInstitucional),
    tipoVinculacion: dto.tipoVinculacion,
    areaNombramientoId: textOrEmpty(dto.areaNombramientoId),
    numeroDecreto: textOrEmpty(dto.numeroDecreto),
    fechaDecreto: textOrEmpty(dto.fechaDecreto),
    escalafon: textOrEmpty(dto.escalafon),
    horasSemanalesContratadas: dto.horasSemanalesContratadas ?? 0,
    maxHorasExtra: dto.maxHorasExtra ?? 0,
    esExclusivoMediaTecnica: Boolean(dto.esExclusivoMediaTecnica),
    estado: dto.estado,
    fechaVinculacion: textOrEmpty(dto.fechaVinculacion),
  }
}

function buildQuery(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === '') {
      return
    }

    search.set(key, String(value))
  })

  const query = search.toString()
  return query ? `/api/v1/docentes?${query}` : '/api/v1/docentes'
}

export function registrarDocente(payload: CrearDocenteRequest) {
  return api.post<DocenteResponse, CrearDocenteRequest>(
    '/api/v1/docentes',
    payload,
  )
}

export function consultarDocente(id: string, signal?: AbortSignal) {
  return api
    .get<DocenteResponse>(`/api/v1/docentes/${id}`, { signal })
    .then(toTeacher)
}

export function actualizarDocente(
  id: string,
  payload: ActualizarDocenteRequest,
) {
  return api.put<DocenteResponse, ActualizarDocenteRequest>(
    `/api/v1/docentes/${id}`,
    payload,
  )
}

export function listarDocentes(
  query: ListarDocentesQuery,
  signal?: AbortSignal,
) {
  return api
    .get<SpringPage<DocenteResponse>>(
      buildQuery({
        texto: query.texto?.trim(),
        estado: query.estado,
        tipoVinculacion: query.tipoVinculacion,
        page: query.page,
        size: query.size,
      }),
      { signal },
    )
    .then((page) => ({
      content: (page.content ?? []).map(toTeacher),
      total: page.totalElements ?? 0,
    }))
}

async function countDocentes(
  estado: TeacherStatus | undefined,
  signal?: AbortSignal,
) {
  const page = await api.get<SpringPage<DocenteResponse>>(
    buildQuery({
      estado,
      page: 0,
      size: 1,
    }),
    { signal },
  )

  return page.totalElements ?? 0
}

export async function consultarResumenDocentes(
  signal?: AbortSignal,
): Promise<StatItemData[]> {
  const [total, active, license, retired] = await Promise.all([
    countDocentes(undefined, signal),
    countDocentes('ACTIVO', signal),
    countDocentes('LICENCIA', signal),
    countDocentes('RETIRADO', signal),
  ])

  return teacherStatsFromCounts({ total, active, license, retired })
}
