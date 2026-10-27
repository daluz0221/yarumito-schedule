import {
  academicYearOptions,
  type AcademicYear,
  type AcademicYearStatus,
} from '../content/academicYear'
import { api } from './client'

export type AnioEscolarResponse = {
  id: string
  anio: number
  fechaInicio: string
  fechaFin: string
  estado: string
  esActual: boolean
}

type PaginaAniosEscolares = {
  content: AnioEscolarResponse[]
  totalPages: number
}

const PAGE_SIZE = 100

const ESTADOS: AcademicYearStatus[] = ['PLANEACION', 'ACTIVO', 'CERRADO']

function listarPagina(page: number, signal?: AbortSignal) {
  const search = new URLSearchParams({
    page: String(page),
    size: String(PAGE_SIZE),
  })

  return api.get<PaginaAniosEscolares>(`/api/v1/anios-escolares?${search.toString()}`, {
    signal,
  })
}

function esEstado(value: string): value is AcademicYearStatus {
  return ESTADOS.includes(value as AcademicYearStatus)
}

function formatFecha(iso: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso ?? '')

  if (!match) {
    return iso?.trim() || '—'
  }

  return `${match[3]}/${match[2]}/${match[1]}`
}

export function aAcademicYear(dto: AnioEscolarResponse): AcademicYear | null {
  if (!dto?.id || !esEstado(dto.estado) || !Number.isFinite(dto.anio)) {
    return null
  }

  return {
    id: dto.id,
    year: dto.anio,
    startDate: formatFecha(dto.fechaInicio),
    endDate: formatFecha(dto.fechaFin),
    status: dto.estado,
    current: Boolean(dto.esActual),
  }
}

export function ordenarAniosEscolares(anios: AcademicYear[]) {
  return [...anios].sort((left, right) => left.year - right.year)
}

export async function listarCatalogoAniosEscolares(signal?: AbortSignal) {
  const first = await listarPagina(0, signal)
  const pagina = [...(first.content ?? [])]
  const totalPages = first.totalPages ?? 1

  for (let page = 1; page < totalPages; page += 1) {
    const next = await listarPagina(page, signal)
    pagina.push(...(next.content ?? []))
  }

  return ordenarAniosEscolares(
    pagina.flatMap((item) => {
      const year = aAcademicYear(item)
      return year ? [year] : []
    }),
  )
}

export function anioEscolarVisible(anios: AcademicYear[]) {
  return anios.find((year) => year.current) ?? anios.at(-1) ?? null
}

export function opcionesAniosEscolares(anios: AcademicYear[] | null, error: string) {
  if (error) {
    return [{ value: '', label: 'Años no disponibles' }]
  }

  if (anios === null) {
    return [{ value: '', label: 'Cargando años lectivos…' }]
  }

  if (anios.length === 0) {
    return [{ value: '', label: 'Sin años escolares' }]
  }

  return academicYearOptions(ordenarAniosEscolares(anios))
}
