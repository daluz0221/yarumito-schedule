import type { AreaResponse } from '../api/areas'

export type AreaStatusFilter = '' | 'activa' | 'inactiva'

export const areaStatusOptions = [
  { value: '', label: 'Todas' },
  { value: 'activa', label: 'Activas' },
  { value: 'inactiva', label: 'Inactivas' },
]

export function labelClasificacion(obligatoria?: boolean) {
  if (obligatoria === undefined) {
    return null
  }

  return obligatoria ? 'Obligatoria' : 'Opcional'
}

export function labelAplicacion(soloMedia?: boolean) {
  if (soloMedia === undefined) {
    return null
  }

  return soloMedia ? 'Solo media' : 'Todos los grados'
}

export function labelEstadoArea(activa?: boolean) {
  if (activa === undefined) {
    return null
  }

  return activa ? 'Activa' : 'Inactiva'
}

export const areaYesNoOptions = [
  { value: '', label: 'Seleccione' },
  { value: 'si', label: 'Sí' },
  { value: 'no', label: 'No' },
]

export const areaFormStatusOptions = [
  { value: '', label: 'Seleccione un estado' },
  { value: 'activa', label: 'Activa' },
  { value: 'inactiva', label: 'Inactiva' },
]

export function isAreaCodeTaken(
  areas: AreaResponse[],
  code: string,
  exceptId?: string,
) {
  const normalized = code.trim().toLowerCase()

  if (!normalized) {
    return false
  }

  return areas.some(
    (area) =>
      area.id !== exceptId && area.codigo.trim().toLowerCase() === normalized,
  )
}

export function filterAreas(
  areas: AreaResponse[],
  filters: { query: string; status: AreaStatusFilter },
) {
  const query = filters.query.trim().toLowerCase()

  return areas.filter((area) => {
    if (filters.status === 'activa' && area.activa !== true) {
      return false
    }

    if (filters.status === 'inactiva' && area.activa !== false) {
      return false
    }

    if (!query) {
      return true
    }

    return (
      area.codigo.toLowerCase().includes(query) ||
      area.nombre.toLowerCase().includes(query)
    )
  })
}
