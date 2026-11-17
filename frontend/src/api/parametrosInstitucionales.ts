import { api } from './client'

export type ParametroInstitucionalResponse = {
  id: string
  anioEscolarId: string
  clave: string
  valor: string
  tipoDato: string
  descripcion: string | null
}

export function listarParametrosInstitucionalesPorAnio(
  anioEscolarId: string,
  signal?: AbortSignal,
) {
  return api.get<ParametroInstitucionalResponse[]>(
    `/api/v1/parametros-institucionales/anio/${encodeURIComponent(anioEscolarId)}`,
    { signal },
  )
}

export function consultarParametroInstitucionalPorClave(
  anioEscolarId: string,
  clave: string,
  signal?: AbortSignal,
) {
  return api.get<ParametroInstitucionalResponse>(
    `/api/v1/parametros-institucionales/anio/${encodeURIComponent(anioEscolarId)}/clave/${encodeURIComponent(clave)}`,
    { signal },
  )
}
