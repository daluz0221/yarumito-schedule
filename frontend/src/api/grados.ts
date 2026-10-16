import { api } from './client'

export type GradoResponse = {
  id: string
  nivel: number
  nombre: string
  esMedia: boolean
  prioridadAsignacion: number
  horasSemanalesEsperadas: number
}

type PaginaGrados = {
  content: GradoResponse[]
  totalPages: number
}

export type OpcionGrado = {
  value: string
  label: string
}

const PAGE_SIZE = 100

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaGrados>(`/api/v1/grados?${search.toString()}`, { signal })
}

export async function listarCatalogoGrados(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const grados = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    grados.push(...(next.content ?? []))
  }

  return ordenarGrados(grados)
}

export function etiquetaGrado(nombre: string) {
  const limpio = nombre.trim().toLocaleLowerCase('es')

  if (!limpio) {
    return 'Grado'
  }

  return limpio.charAt(0).toLocaleUpperCase('es') + limpio.slice(1)
}

export function ordenarGrados(grados: GradoResponse[]) {
  return [...grados].sort(
    (left, right) =>
      left.nivel - right.nivel ||
      etiquetaGrado(left.nombre).localeCompare(etiquetaGrado(right.nombre), 'es'),
  )
}

export function planDesdeGrado(grado: GradoResponse) {
  return {
    typeLabel: grado.esMedia ? 'Media Técnica' : 'Bachillerato',
    weeklyHours: grado.horasSemanalesEsperadas,
  }
}

export function gradoPorId(grados: GradoResponse[] | null, id: string) {
  return grados?.find((grado) => grado.id === id) ?? null
}

export function gradoPorEtiqueta(grados: GradoResponse[] | null, etiqueta: string) {
  return grados?.find((grado) => etiquetaGrado(grado.nombre) === etiqueta) ?? null
}

function opcionesEstado(grados: GradoResponse[] | null, error: string): OpcionGrado[] | null {
  if (error) {
    return [{ value: '', label: 'Grados no disponibles' }]
  }

  if (grados === null) {
    return [{ value: '', label: 'Cargando grados…' }]
  }

  if (grados.length === 0) {
    return [{ value: '', label: 'Sin grados registrados' }]
  }

  return null
}

export function opcionesGradoRequerido(grados: GradoResponse[] | null, error: string) {
  return (
    opcionesEstado(grados, error) ?? [
      { value: '', label: 'Seleccione un grado' },
      ...ordenarGrados(grados ?? []).map((grado) => ({
        value: grado.id,
        label: etiquetaGrado(grado.nombre),
      })),
    ]
  )
}

export function opcionesFiltroGrados(grados: GradoResponse[] | null, error: string) {
  if (error) {
    return [{ value: '', label: 'Grados no disponibles' }]
  }

  if (grados === null) {
    return [{ value: '', label: 'Cargando grados…' }]
  }

  return [
    { value: '', label: 'Todos' },
    ...ordenarGrados(grados).map((grado) => ({
      value: etiquetaGrado(grado.nombre),
      label: etiquetaGrado(grado.nombre),
    })),
  ]
}

export function opcionesSelectorPlan(grados: GradoResponse[] | null, error: string) {
  return (
    opcionesEstado(grados, error) ??
    ordenarGrados(grados ?? []).map((grado) => ({
      value: grado.id,
      label: etiquetaGrado(grado.nombre),
    }))
  )
}
