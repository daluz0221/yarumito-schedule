import { api } from './client'
import type {
  ActualizarTituloProfesionalRequest,
  CrearTituloProfesionalRequest,
  ProfessionalTitle,
} from '../content/teacherProfile'

export type TituloProfesionalResponse = {
  id: string
  docenteId: string
  nivel: string
  nombreTitulo: string
  institucion: string
  anioGraduacion: number | null
  archivoSoporte: string | null
  activo?: boolean
}

function textOrEmpty(value: string | null | undefined) {
  return value ?? ''
}

export function toProfessionalTitle(
  dto: TituloProfesionalResponse,
): ProfessionalTitle {
  return {
    id: textOrEmpty(dto.id),
    level: textOrEmpty(dto.nivel),
    name: textOrEmpty(dto.nombreTitulo),
    institution: textOrEmpty(dto.institucion),
    year: dto.anioGraduacion == null ? '' : String(dto.anioGraduacion),
    supportFile: textOrEmpty(dto.archivoSoporte),
  }
}

export function listarTitulosPorDocente(
  docenteId: string,
  signal?: AbortSignal,
) {
  return api
    .get<TituloProfesionalResponse[]>(
      `/api/v1/docentes/${docenteId}/titulos-profesionales`,
      { signal },
    )
    .then((items) => (Array.isArray(items) ? items : []).map(toProfessionalTitle))
}

export function consultarTituloProfesional(id: string, signal?: AbortSignal) {
  return api
    .get<TituloProfesionalResponse>(`/api/v1/titulos-profesionales/${id}`, {
      signal,
    })
    .then(toProfessionalTitle)
}

export function registrarTituloProfesional(
  payload: CrearTituloProfesionalRequest,
) {
  return api
    .post<TituloProfesionalResponse, CrearTituloProfesionalRequest>(
      '/api/v1/titulos-profesionales',
      payload,
    )
    .then(toProfessionalTitle)
}

export function actualizarTituloProfesional(
  id: string,
  payload: ActualizarTituloProfesionalRequest,
) {
  return api
    .put<TituloProfesionalResponse, ActualizarTituloProfesionalRequest>(
      `/api/v1/titulos-profesionales/${id}`,
      payload,
    )
    .then(toProfessionalTitle)
}

export function listarTitulosActivosPorDocente(
  docenteId: string,
  signal?: AbortSignal,
) {
  return api
    .get<TituloProfesionalResponse[]>(
      `/api/v1/docentes/${docenteId}/titulos-profesionales/activos`,
      { signal },
    )
    .then((items) => (Array.isArray(items) ? items : []).map(toProfessionalTitle))
}

export function listarTitulosRetiradosPorDocente(
  docenteId: string,
  signal?: AbortSignal,
) {
  return api
    .get<TituloProfesionalResponse[]>(
      `/api/v1/docentes/${docenteId}/titulos-profesionales/retirados`,
      { signal },
    )
    .then((items) => (Array.isArray(items) ? items : []).map(toProfessionalTitle))
}

// H-04: baja lógica. El backend responde 409 si respalda idoneidades vigentes.
export function retirarTituloProfesional(id: string) {
  return api
    .patch<TituloProfesionalResponse>(`/api/v1/titulos-profesionales/${id}/retirar`)
    .then(toProfessionalTitle)
}

export function reactivarTituloProfesional(id: string) {
  return api
    .patch<TituloProfesionalResponse>(`/api/v1/titulos-profesionales/${id}/reactivar`)
    .then(toProfessionalTitle)
}
