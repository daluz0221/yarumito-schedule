import { api } from './client'

export type AreaResponse = {
  id: string
  nombre: string
  codigo: string
}

export function listarAreas(signal?: AbortSignal) {
  return api.get<AreaResponse[]>('/api/v1/areas', { signal })
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
