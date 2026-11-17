import { api } from './client'

export type EntidadAuditable = 'AsignacionAcademica' | 'ClaseProgramada'

export type RegistroAuditoriaResponse = {
  id: string
  usuarioId: string
  accion: string
  entidad: string
  idEntidad: string
  datosAntes: unknown
  datosDespues: unknown
  fecha: string | number[] | null
  ip: string | null
}

export function consultarAuditoriaPorEntidad(
  entidad: string,
  idEntidad: string,
  signal?: AbortSignal,
) {
  const ruta = `/api/v1/registros-auditoria/entidad/${encodeURIComponent(entidad)}/${encodeURIComponent(idEntidad)}`

  return api.get<RegistroAuditoriaResponse[]>(ruta, { signal })
}

export function consultarAuditoriaPorUsuario(usuarioId: string, signal?: AbortSignal) {
  return api.get<RegistroAuditoriaResponse[]>(
    `/api/v1/registros-auditoria/usuario/${encodeURIComponent(usuarioId)}`,
    { signal },
  )
}
