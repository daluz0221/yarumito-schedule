export type EntidadAuditable = 'AsignacionAcademica' | 'ClaseProgramada'

export function rutaHistorialEntidad(
  entidad: EntidadAuditable,
  id: string,
  grupoId?: string,
) {
  const params = new URLSearchParams({ entidad, id })

  if (grupoId) {
    params.set('grupo', grupoId)
  }

  return `/dashboard/auditoria?${params.toString()}`
}
