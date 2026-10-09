import { api } from './client'

export type AsignaturaResponse = {
  id: string
  areaId: string
  nombre: string
  codigo: string
  abreviatura?: string
  colorUi?: string | null
  exigeIdoneidadEstricta: boolean
  esMediaTecnica: boolean
  requiereDocenteExclusivo: boolean
  tipoAulaRequerida?: string | null
  maxClasesConsecutivas: number
  activa: boolean
}

export function listarAsignaturas(areaId: string, signal?: AbortSignal) {
  const params = new URLSearchParams({ areaId })
  return api.get<AsignaturaResponse[]>(`/api/v1/asignaturas?${params.toString()}`, {
    signal,
  })
}
