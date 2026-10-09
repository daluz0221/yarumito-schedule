import { api } from './client'

export type AreaResponse = {
  id: string
  nombre: string
  codigo: string
  obligatoria?: boolean
  soloMedia?: boolean
  activa?: boolean
}

export function listarAreas(signal?: AbortSignal) {
  return api.get<AreaResponse[]>('/api/v1/areas', { signal })
}

export function listarCatalogoAreas(signal?: AbortSignal) {
  return api.get<AreaResponse[]>('/api/v1/areas?soloActivas=false', { signal })
}

export function toAreaSelectOptions(areas: AreaResponse[]) {
  return [
    { value: '', label: 'Seleccione un área' },
    ...areas.map((area) => ({
      value: area.id,
      label: area.nombre,
    })),
  ]
}
