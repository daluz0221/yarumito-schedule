import type { RegistroAuditoriaResponse } from '../api/registrosAuditoria'

// Entidades que el backend deja en el historial de auditoría. El nombre debe
// coincidir con el que registra cada servicio.
export const ENTIDADES_GRUPO = [
  { value: 'AsignacionAcademica', label: 'Asignación académica' },
  { value: 'ClaseProgramada', label: 'Clase programada' },
] as const

export const ENTIDADES_OTRAS = [
  { value: 'Aula', label: 'Aula' },
  { value: 'Docente', label: 'Docente' },
  { value: 'PlanEstudios', label: 'Plan de estudios' },
  { value: 'RestriccionDocente', label: 'Restricción docente' },
  { value: 'TituloProfesional', label: 'Título profesional' },
] as const

export type EntidadGrupo = (typeof ENTIDADES_GRUPO)[number]['value']
export type EntidadOtra = (typeof ENTIDADES_OTRAS)[number]['value']
export type EntidadAuditable = EntidadGrupo | EntidadOtra

export const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

export function esEntidadGrupo(valor: string): valor is EntidadGrupo {
  return ENTIDADES_GRUPO.some((item) => item.value === valor)
}

export function esEntidadOtra(valor: string): valor is EntidadOtra {
  return ENTIDADES_OTRAS.some((item) => item.value === valor)
}

export function esEntidad(valor: string): valor is EntidadAuditable {
  return esEntidadGrupo(valor) || esEntidadOtra(valor)
}

export function etiquetaEntidad(entidad: string) {
  return (
    [...ENTIDADES_GRUPO, ...ENTIDADES_OTRAS].find((item) => item.value === entidad)?.label ??
    entidad
  )
}

export type ConsultaEstado = 'inicial' | 'cargando' | 'listo' | 'error'

export type ConsultaAuditoria = {
  estado: ConsultaEstado
  registros: RegistroAuditoriaResponse[]
  error: string
}

export const consultaInicial: ConsultaAuditoria = {
  estado: 'inicial',
  registros: [],
  error: '',
}

export function marcaTiempo(value: RegistroAuditoriaResponse['fecha']) {
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value.map((part) => Number(part))

    if ([year, month, day, hour, minute].every((part) => Number.isFinite(part))) {
      return new Date(year, month - 1, day, hour, minute, second || 0).getTime()
    }
  }

  if (typeof value === 'string' && value.trim()) {
    const date = new Date(value)
    return Number.isNaN(date.getTime()) ? 0 : date.getTime()
  }

  return 0
}

export function ordenarRegistros(registros: RegistroAuditoriaResponse[]) {
  const unicos = new Map<string, RegistroAuditoriaResponse>()

  registros.forEach((registro) => {
    if (registro?.id) {
      unicos.set(registro.id, registro)
    }
  })

  return [...unicos.values()].sort((left, right) => marcaTiempo(right.fecha) - marcaTiempo(left.fecha))
}

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
