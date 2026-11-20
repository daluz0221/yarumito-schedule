import type { DatosPublicacion } from '../api/publicacionHorario'
import type { AcademicYear } from './academicYear'
import {
  publishReadiness,
  scheduleNameForYear,
  type ConflictKind,
  type PublishReadiness,
  type ReviewedConflict,
} from './validations'

export type PublicationStatus = 'BORRADOR' | 'PUBLICADO'

export type PublicationCheck = {
  id: 'assignments' | 'teacher' | 'group' | 'classroom'
  label: string
  passed: boolean
}

export type PublicationView = {
  scheduleName: string
  yearLabel: string
  campus: string
  campusFull: string
  version: number
  status: PublicationStatus
  groups: number
  classes: number
  errors: number
  warnings: number
  pendingHours: number
  publicationLabel: 'Pendiente' | 'Publicado'
  readiness: PublishReadiness
  checks: PublicationCheck[]
  versionCaption: string
  hasSchedule: boolean
}

export type PublicationWarningItem = {
  id: string
  title: string
}

export type PublicationModal = 'none' | 'warnings' | 'blocked'

export const publicationCopy = {
  pageTitle: 'Publicación del horario',
  pageSubtitle: 'Revisa el resultado final y publica la versión del horario académico.',
  summaryTitle: 'Resumen final',
  checksTitle: 'Comprobaciones antes de publicar',
  versionsTitle: 'Versiones del horario',
  previousTitle: 'Versiones anteriores',
  previousEmpty:
    'El servidor aún no expone el historial de versiones. Las versiones anteriores deben preservarse; el archivado de una versión previa queda pendiente de regla de negocio explícita.',
  logTitle: 'Registro de publicación',
  logEmpty: 'El servidor aún no entrega el registro de publicaciones de este horario.',
  logPublished: 'Este horario ya figura como publicado en el servidor.',
  logNote: 'La publicación desde esta pantalla se habilitará cuando el servidor la soporte.',
  publish: 'Publicar horario',
  published: 'Publicado',
  seeWarnings: 'Ver advertencias',
  seeValidations: 'Ver validaciones',
  noBlocking: 'Sin errores bloqueantes',
  blocking: 'Hay errores bloqueantes',
  warningsNote: 'Existen advertencias pendientes. Revisa su impacto antes de publicar.',
  blockingNote: 'Existen errores pendientes. Corrige el horario y vuelve a validar antes de publicar.',
  readyNote: 'No hay errores ni advertencias pendientes. Puedes publicar esta versión.',
  noScheduleNote: 'Este año aún no tiene horarios construidos para sus grupos activos.',
  warningsTitle: 'Advertencias pendientes',
  warningsLead: (count: number) =>
    `Este horario no contiene errores bloqueantes, pero mantiene ${count} ${count === 1 ? 'advertencia' : 'advertencias'}.`,
  warningsDisclaimer: 'Las advertencias no bloquean por sí mismas este flujo de publicación.',
  warningsReview: 'Volver a revisar',
  blockedTitle: 'Publicación bloqueada',
  blockedLead: 'No es posible publicar mientras existan errores pendientes.',
  blockedHint: 'Vuelve a Validaciones y conflictos, corrige el horario y ejecuta una nueva validación.',
  blockedAction: 'Volver a validaciones',
}

export function buildPublicationView({
  year,
  datos,
  campusName,
}: {
  year: AcademicYear
  datos: DatosPublicacion
  campusName: string
}): PublicationView {
  const { tableros, conflictos } = datos
  const scheduleName = scheduleNameForYear(year)
  const conRegistro = tableros.filter((item) => item.horarioId)
  const pending = conflictos.filter((item) => item.status === 'PENDIENTE')
  const errors = pending.filter((item) => item.severity === 'ERROR').length
  const warnings = pending.filter((item) => item.severity === 'ADVERTENCIA').length
  const pendingHours = tableros.reduce((total, item) => total + (item.resumen?.horasPendientes ?? 0), 0)
  const published =
    conRegistro.length > 0 && conRegistro.every((item) => item.estadoHorario === 'PUBLICADO')
  const campusFull = campusName.trim() || 'Sede no disponible'

  return {
    scheduleName,
    yearLabel: String(year.year),
    campus: campusFull.replace(/^Sede\s+/i, ''),
    campusFull,
    version: conRegistro.reduce((max, item) => Math.max(max, item.versionHorario || 1), 0),
    status: published ? 'PUBLICADO' : 'BORRADOR',
    groups: tableros.filter((item) => (item.clasesProgramadas ?? []).length > 0).length,
    classes: tableros.reduce((total, item) => total + (item.clasesProgramadas ?? []).length, 0),
    errors,
    warnings,
    pendingHours,
    publicationLabel: published ? 'Publicado' : 'Pendiente',
    readiness: publishReadiness(errors, warnings),
    checks: publicationChecks(pending, pendingHours),
    versionCaption: published
      ? `${scheduleName} · publicado`
      : `${scheduleName} · pendiente de publicación`,
    hasSchedule: conRegistro.length > 0,
  }
}

export function pendingPublicationWarnings(conflicts: ReviewedConflict[]): PublicationWarningItem[] {
  return conflicts
    .filter((item) => item.status === 'PENDIENTE' && item.severity === 'ADVERTENCIA')
    .map((item) => ({
      id: item.id,
      title: publicationWarningTitle(item),
    }))
}

export function publicationWarningTitle(conflict: ReviewedConflict) {
  if (conflict.kind === 'capacity') {
    return conflict.classroom ? `Sobreaforo — ${conflict.classroom}` : conflict.message || 'Sobreaforo'
  }

  if (conflict.kind === 'preference') {
    return 'Preferencia docente no satisfecha'
  }

  if (conflict.kind === 'lab-required') {
    return conflict.subject ? `Aula no requerida — ${conflict.subject}` : 'Aula no requerida'
  }

  return conflict.message
}

export function publicationChecks(pending: ReviewedConflict[], pendingHours: number): PublicationCheck[] {
  const hasError = (...kinds: ConflictKind[]) =>
    pending.some((item) => item.severity === 'ERROR' && kinds.includes(item.kind))

  return [
    {
      id: 'assignments',
      label: 'Asignaciones académicas programadas',
      passed: pendingHours === 0,
    },
    {
      id: 'teacher',
      label: 'Sin conflictos de docente',
      passed: !hasError('teacher-busy', 'teacher-unavailable'),
    },
    {
      id: 'group',
      label: 'Sin conflictos de grupo',
      passed: !hasError('group-busy'),
    },
    {
      id: 'classroom',
      label: 'Sin conflictos de aula',
      passed: !hasError('classroom-busy'),
    },
  ]
}
