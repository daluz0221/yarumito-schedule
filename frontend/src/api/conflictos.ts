import { leerDetalleDelMensaje } from '../content/detalleConflicto'
import type { ConflictKind, ReviewedConflict } from '../content/validations'
import { api } from './client'

export type TipoConflicto = 'DOCENTE_OCUPADO' | 'AULA_OCUPADA' | 'CAPACIDAD_AULA'

export type SeveridadConflicto = 'ERROR' | 'ADVERTENCIA'

export type ConflictoResponse = {
  id: string
  horarioId: string
  tipo: TipoConflicto
  severidad: SeveridadConflicto
  mensaje: string
  resuelto: boolean
  fechaDeteccion: string
  dia?: string | null
  franja?: string | null
  elementoAfectado?: string | null
  asignatura?: string | null
  docente?: string | null
  aula?: string | null
  grupo?: string | null
}

export type ConflictoVista = ReviewedConflict & {
  detectedAt: string
}

const TIPOS: Record<TipoConflicto, ConflictKind> = {
  DOCENTE_OCUPADO: 'teacher-busy',
  AULA_OCUPADA: 'classroom-busy',
  CAPACIDAD_AULA: 'capacity',
}

export function listarConflictosPorHorario(horarioId: string, signal?: AbortSignal) {
  return api.get<ConflictoResponse[]>(`/api/v1/conflictos/horario/${horarioId}`, { signal })
}

export function listarConflictosPendientes(horarioId: string, signal?: AbortSignal) {
  return api.get<ConflictoResponse[]>(`/api/v1/conflictos/horario/${horarioId}/pendientes`, {
    signal,
  })
}

export function listarConflictosPorSeveridad(
  horarioId: string,
  severidad: SeveridadConflicto,
  signal?: AbortSignal,
) {
  const search = new URLSearchParams({ severidad })

  return api.get<ConflictoResponse[]>(
    `/api/v1/conflictos/horario/${horarioId}/severidad?${search.toString()}`,
    { signal },
  )
}

export function marcarConflictoResuelto(conflictoId: string) {
  return api.patch<ConflictoResponse>(`/api/v1/conflictos/${conflictoId}/resuelto`)
}

function formatoFechaDeteccion(value: unknown) {
  if (Array.isArray(value) && value.length >= 5) {
    const [year, month, day, hour, minute] = value.map((part) => Number(part))
    const pad = (part: number) => String(part).padStart(2, '0')

    if ([year, month, day, hour, minute].every((part) => Number.isFinite(part))) {
      return `${year}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}`
    }
  }

  if (typeof value === 'string') {
    const match = /^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})/.exec(value)
    return match ? `${match[1]} ${match[2]}` : value
  }

  return '—'
}

export function aConflictoRevisado(dto: ConflictoResponse, yearId: string): ConflictoVista | null {
  const kind = TIPOS[dto?.tipo]

  if (!dto?.id || !kind) {
    return null
  }

  const severity = dto.severidad === 'ADVERTENCIA' ? 'ADVERTENCIA' : 'ERROR'
  const leido = leerDetalleDelMensaje(dto.mensaje ?? '', dto.tipo)
  const dia = texto(dto.dia) || leido.dia
  const franja = texto(dto.franja) || leido.franja
  const docente = texto(dto.docente) || leido.docente
  const aula = texto(dto.aula) || leido.aula
  const grupo = texto(dto.grupo) || leido.grupo
  const asignatura = texto(dto.asignatura) || leido.asignatura
  const elemento = texto(dto.elementoAfectado) || leido.elementoAfectado

  return {
    id: dto.id,
    yearId,
    severity,
    kind,
    message: dto.mensaje?.trim() || 'Conflicto detectado',
    day: null,
    bandId: null,
    slotLabel: [dia, franja].filter(Boolean).join(', ') || '—',
    affected: elemento || docente || aula || grupo || '—',
    subject: asignatura,
    teacherName: docente,
    classroom: aula,
    groupCode: grupo,
    groupIds: [],
    teacherNames: docente ? [docente] : [],
    classrooms: aula ? [aula] : [],
    status: dto.resuelto ? 'RESUELTO' : 'PENDIENTE',
    detectedAt: formatoFechaDeteccion(dto.fechaDeteccion),
  }
}

function texto(value: string | null | undefined) {
  const limpio = value?.trim()
  return limpio || ''
}
