import type { AcademicYear } from './academicYear'
import type { WeekDay } from './teacherRestrictions'

export type ConflictSeverity = 'ERROR' | 'ADVERTENCIA'
export type ConflictReviewStatus = 'PENDIENTE' | 'RESUELTO'

export type ConflictKind =
  | 'teacher-busy'
  | 'group-busy'
  | 'classroom-busy'
  | 'teacher-unavailable'
  | 'incomplete-hours'
  | 'lab-required'
  | 'capacity'
  | 'preference'

export type ScheduleConflict = {
  id: string
  yearId: string
  severity: ConflictSeverity
  kind: ConflictKind
  message: string
  day: WeekDay | null
  bandId: string | null
  slotLabel: string
  affected: string
  subject: string
  teacherName: string
  classroom: string
  groupCode: string
  groupIds: string[]
  teacherNames: string[]
  classrooms: string[]
}

export type ReviewedConflict = ScheduleConflict & { status: ConflictReviewStatus }

export type ValidationFilters = {
  severity: string
  status: string
  kind: string
  groupId: string
  teacherName: string
  classroom: string
}

export type PublishReadiness = 'blocked' | 'warnings' | 'ready'

const kindLabels: Record<ConflictKind, string> = {
  'teacher-busy': 'Docente ocupado',
  'group-busy': 'Grupo ocupado',
  'classroom-busy': 'Aula ocupada',
  'teacher-unavailable': 'Restricción docente',
  'incomplete-hours': 'Horas incompletas',
  'lab-required': 'Aula no requerida',
  'capacity': 'Sobreaforo',
  'preference': 'Preferencia docente',
}

export const validationReadyCopy = {
  title: 'Sin errores bloqueantes',
  badge: 'Validación completada',
  message: 'No se detectaron errores bloqueantes en la validación actual.',
  warnings: 'Pueden existir advertencias. Revísalas antes de continuar.',
}


export const severityFilterOptions = [
  { value: '', label: 'Todos' },
  { value: 'ERROR', label: 'Error' },
  { value: 'ADVERTENCIA', label: 'Advertencia' },
]

export const statusFilterOptions = [
  { value: '', label: 'Todos' },
  { value: 'PENDIENTE', label: 'Pendientes' },
  { value: 'RESUELTO', label: 'Resueltos' },
]

export const kindFilterOptions = [
  { value: '', label: 'Todos' },
  ...Object.entries(kindLabels).map(([value, label]) => ({ value, label })),
]

export function labelConflictKind(kind: ConflictKind) {
  return kindLabels[kind]
}

export function labelConflictSeverity(severity: ConflictSeverity) {
  return severity === 'ERROR' ? 'Error' : 'Advertencia'
}

export function labelConflictStatus(status: ConflictReviewStatus) {
  return status === 'RESUELTO' ? 'Resuelto' : 'Pendiente'
}


export function scheduleNameForYear(year: AcademicYear) {
  return `Horario General ${year.year}`
}



export function conflictDetailFacts(
  conflict: ReviewedConflict,
  scheduleLabel: string,
  detectedAt: string,
) {
  const facts = [
    { label: 'Horario', value: scheduleLabel },
    { label: 'Día / Franja', value: momento(conflict) },
    { label: 'Docente', value: conflict.teacherName },
    { label: 'Grupo', value: conflict.groupCode },
    { label: 'Asignatura', value: conflict.subject },
    { label: 'Aula', value: conflict.classroom },
    { label: 'Fecha de detección', value: detectedAt },
  ]

  return facts.filter((fact) => tieneDato(fact.value))
}

function momento(conflict: ReviewedConflict) {
  const dia = conflict.slotLabel && conflict.slotLabel !== '—' ? conflict.slotLabel : ''
  return dia
}




export function conflictosPorId<T extends { id: string }>(conflicts: T[]) {
  const vistos = new Set<string>()

  return conflicts.filter((item) => {
    if (!item.id || vistos.has(item.id)) {
      return false
    }

    vistos.add(item.id)
    return true
  })
}

export function contarConflictosPendientes(conflicts: ReviewedConflict[]) {
  const pending = conflictosPorId(conflicts).filter((item) => item.status === 'PENDIENTE')
  const errors = pending.filter((item) => item.severity === 'ERROR').length
  const warnings = pending.filter((item) => item.severity === 'ADVERTENCIA').length

  return {
    pending,
    errors,
    warnings,
    readiness: publishReadiness(errors, warnings),
  }
}

export function summarizeConflicts(conflicts: ReviewedConflict[]) {
  const unicos = conflictosPorId(conflicts)
  const conteo = contarConflictosPendientes(unicos)
  const errors = unicos.filter((item) => item.severity === 'ERROR')
  const warnings = unicos.filter((item) => item.severity === 'ADVERTENCIA')
  const resolved = unicos.filter((item) => item.status === 'RESUELTO')

  return {
    total: unicos.length,
    errors: errors.length,
    warnings: warnings.length,
    resolved: resolved.length,
    pending: conteo.pending.length,
    readiness: conteo.readiness,
  }
}

export function publishReadiness(pendingErrors: number, pendingWarnings: number): PublishReadiness {
  if (pendingErrors > 0) {
    return 'blocked'
  }

  if (pendingWarnings > 0) {
    return 'warnings'
  }

  return 'ready'
}

export function filterConflicts(conflicts: ReviewedConflict[], filters: ValidationFilters) {
  return conflicts.filter((item) => {
    if (filters.severity && item.severity !== filters.severity) {
      return false
    }

    if (filters.status && item.status !== filters.status) {
      return false
    }

    if (filters.kind && item.kind !== filters.kind) {
      return false
    }

    if (filters.groupId && !item.groupIds.includes(filters.groupId)) {
      return false
    }

    if (filters.teacherName && !item.teacherNames.some((name) => normalize(name) === normalize(filters.teacherName))) {
      return false
    }

    if (filters.classroom && !item.classrooms.some((room) => normalize(room) === normalize(filters.classroom))) {
      return false
    }

    return true
  })
}






function tieneDato(value: string) {
  const texto = value?.trim()
  return Boolean(texto) && texto !== '—' && texto !== '-'
}

function normalize(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}
