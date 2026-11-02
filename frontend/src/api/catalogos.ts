import { api } from './client'

export type OpcionCatalogo = {
  codigo: string
  nombre: string
}

export type OpcionAsignaturaPredefinida = OpcionCatalogo & {
  abreviatura: string
  codigoArea: string
}

export function listarAreasPredefinidas(signal?: AbortSignal) {
  return api.get<OpcionCatalogo[]>('/api/v1/catalogos/areas', { signal })
}

export function listarAsignaturasPredefinidas(area?: string, signal?: AbortSignal) {
  const search = new URLSearchParams()
  const codigo = area?.trim()

  if (codigo) {
    search.set('area', codigo)
  }

  const query = search.toString()
  return api.get<OpcionAsignaturaPredefinida[]>(
    `/api/v1/catalogos/asignaturas${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function listarRegimenesDocentes(signal?: AbortSignal) {
  return api.get<OpcionCatalogo[]>('/api/v1/catalogos/regimenes-docentes', { signal })
}

export function listarEscalafones(regimen: string, signal?: AbortSignal) {
  const search = new URLSearchParams({ regimen })
  return api.get<OpcionCatalogo[]>(`/api/v1/catalogos/escalafones?${search.toString()}`, {
    signal,
  })
}

export function regimenDesdeEscalafon(codigo: string) {
  const valor = codigo.trim().toUpperCase()

  if (/^(?:[1-9]|1[0-4])$/.test(valor)) {
    return '2277'
  }

  if (/^[1-3][ABCD]$/.test(valor)) {
    return '1278'
  }

  if (/^(?:I|II|III|IV|V|VI)$/.test(valor)) {
    return 'SEIP'
  }

  return ''
}
