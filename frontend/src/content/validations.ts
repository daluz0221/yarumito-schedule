import {
  buildAssignmentRows,
  groupsForYear,
  seedAssignments,
} from './academicAssignment'
import { academicYears, type AcademicYear } from './academicYear'
import {
  assessPlacement,
  curricularRowsSource,
  pendingPlacements,
  scheduleCampusOptions,
  scheduleClassrooms,
  scheduleStatusLabel,
  scheduleSubject,
  seedPlacements,
  type SchedulePlacement,
} from './scheduleConstruction'
import { morningBands, type WeekDay } from './teacherRestrictions'
import type { Teacher } from './teachers'
import { formatTeacherName } from './teachers'

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

export const validationCampusLabel = scheduleCampusOptions[0]?.label ?? 'Sede Principal'
export const validationScheduleStatus = scheduleStatusLabel

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

export function conflictGuidance(kind: ConflictKind) {
  const messages: Record<ConflictKind, string> = {
    'teacher-busy':
      'Ajusta una de las clases en Construcción de horario para que el docente no quede en dos grupos a la misma hora.',
    'group-busy':
      'Un grupo no puede tener dos clases simultáneas. Reubica una de las franjas en Construcción de horario.',
    'classroom-busy':
      'El aula ya está ocupada. Cambia el aula o mueve una de las clases a otra franja.',
    'teacher-unavailable':
      'La franja está bloqueada por una restricción docente. Cambia el horario o revisa la disponibilidad del docente.',
    'incomplete-hours':
      'Faltan horas por programar según la asignación académica. Completa las franjas pendientes en Construcción de horario.',
    'lab-required':
      'Asigna un laboratorio o un aula compatible con el requerimiento de la asignatura.',
    'capacity':
      'Elige un aula con mayor capacidad o revisa el número de estudiantes del grupo.',
    'preference':
      'Es una advertencia: el docente prefiere no usar esta franja, pero no bloquea la publicación por sí sola.',
  }

  return messages[kind]
}

export function scheduleNameForYear(year: AcademicYear) {
  return `Horario General ${year.year}`
}

export function scheduleVersionLabel(year: AcademicYear, version: number) {
  return `${scheduleNameForYear(year)} · v${version}`
}

export function formatValidationTimestamp(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function conflictDetailFacts(
  conflict: ReviewedConflict,
  scheduleLabel: string,
  detectedAt: string,
) {
  const extra =
    conflict.kind === 'teacher-busy' || conflict.kind === 'lab-required'
      ? [
          { label: 'Asignatura', value: display(conflict.subject) },
          { label: 'Aula', value: display(conflict.classroom) },
        ]
      : conflict.kind === 'classroom-busy'
        ? [
            { label: 'Docente', value: display(conflict.teacherName) },
            { label: 'Grupo', value: display(conflict.groupCode) },
          ]
        : conflict.kind === 'group-busy'
          ? [
              { label: 'Docente', value: display(conflict.teacherName) },
              { label: 'Asignatura', value: display(conflict.subject) },
            ]
          : [
              { label: 'Grupo', value: display(conflict.groupCode) },
              { label: 'Asignatura', value: display(conflict.subject) },
            ]

  return [
    { label: 'Horario', value: scheduleLabel },
    { label: 'Día / Franja', value: conflict.slotLabel },
    { label: 'Elemento afectado', value: display(conflict.affected) },
    ...extra,
    { label: 'Fecha de detección', value: detectedAt },
  ]
}

export function formatConflictSlot(day: WeekDay | null, bandId: string | null) {
  if (!day || !bandId) {
    return '—'
  }

  const raw = morningBands.find((band) => band.id === bandId)?.label ?? bandId
  const compact = raw
    .replace(/\s*–\s*|\s*-\s*/g, '-')
    .replace(/\b(\d):/g, '0$1:')
    .replace(/\s·.+$/, '')

  return `${day} · ${compact}`
}

export function detectScheduleConflicts(
  yearId: string,
  placements = seedPlacements,
  teachers: Teacher[] = [],
): ScheduleConflict[] {
  const yearPlacements = placements.filter((item) => item.yearId === yearId)
  const groups = groupsForYear(yearId)
  const conflicts: ScheduleConflict[] = []
  const slots = new Map<string, SchedulePlacement[]>()

  yearPlacements.forEach((item) => {
    const key = `${item.day}-${item.bandId}`
    const current = slots.get(key) ?? []
    current.push(item)
    slots.set(key, current)
  })

  slots.forEach((items) => {
    conflicts.push(
      ...duplicates(items, (item) => normalize(item.teacherName)).map((peers) =>
        conflict({
          yearId,
          kind: 'teacher-busy',
          severity: 'ERROR',
          message: `${peers[0].teacherName} tiene dos clases programadas en la misma franja.`,
          day: peers[0].day,
          bandId: peers[0].bandId,
          affected: `${peers[0].teacherName} / Grupo ${groupCode(yearId, peers[0].groupId)}`,
          subject: subjectName(peers[0].subjectKey),
          teacherName: peers[0].teacherName,
          classroom: peers[0].classroom,
          groupCode: groupCode(yearId, peers[0].groupId),
          groupIds: unique(peers.map((peer) => peer.groupId)),
          teacherNames: [peers[0].teacherName],
          classrooms: unique(peers.map((peer) => peer.classroom)),
        }),
      ),
    )

    conflicts.push(
      ...duplicates(items, (item) => item.groupId).map((peers) =>
        conflict({
          yearId,
          kind: 'group-busy',
          severity: 'ERROR',
          message: `El grupo ${groupCode(yearId, peers[0].groupId)} tiene dos clases programadas simultáneamente.`,
          day: peers[0].day,
          bandId: peers[0].bandId,
          affected: `Grupo ${groupCode(yearId, peers[0].groupId)}`,
          subject: subjectName(peers[0].subjectKey),
          teacherName: peers[0].teacherName,
          classroom: peers[0].classroom,
          groupCode: groupCode(yearId, peers[0].groupId),
          groupIds: [peers[0].groupId],
          teacherNames: unique(peers.map((peer) => peer.teacherName)),
          classrooms: unique(peers.map((peer) => peer.classroom)),
        }),
      ),
    )

    conflicts.push(
      ...duplicates(items, (item) => normalize(item.classroom)).map((peers) =>
        conflict({
          yearId,
          kind: 'classroom-busy',
          severity: 'ERROR',
          message: `${peers[0].classroom} está asignada a dos clases en la misma franja.`,
          day: peers[0].day,
          bandId: peers[0].bandId,
          affected: peers[0].classroom,
          subject: subjectName(peers[0].subjectKey),
          teacherName: peers[0].teacherName,
          classroom: peers[0].classroom,
          groupCode: groupCode(yearId, peers[0].groupId),
          groupIds: unique(peers.map((peer) => peer.groupId)),
          teacherNames: unique(peers.map((peer) => peer.teacherName)),
          classrooms: [peers[0].classroom],
        }),
      ),
    )
  })

  yearPlacements.forEach((item) => {
    const group = groups.find((entry) => entry.id === item.groupId)
    const subject = scheduleSubject(item.subjectKey)
    const assessment = assessPlacement(
      item,
      yearPlacements,
      Boolean(subject?.note && /laborat/i.test(subject.note)),
      group?.students ?? 0,
    )

    if (assessment.tone === 'conflict' && /no está disponible/i.test(assessment.reason)) {
      conflicts.push(
        conflict({
          yearId,
          kind: 'teacher-unavailable',
          severity: 'ERROR',
          message: 'La clase está programada en una franja NO DISPONIBLE para el docente.',
          day: item.day,
          bandId: item.bandId,
          affected: item.teacherName,
          subject: subjectName(item.subjectKey),
          teacherName: item.teacherName,
          classroom: item.classroom,
          groupCode: groupCode(yearId, item.groupId),
          groupIds: [item.groupId],
          teacherNames: [item.teacherName],
          classrooms: [item.classroom],
        }),
      )
    }

    if (assessment.tone === 'warning') {
      if (/laboratorio/i.test(assessment.reason)) {
        conflicts.push(
          warningFromPlacement(yearId, item, 'lab-required', 'La asignatura requiere laboratorio y el aula asignada no lo es.'),
        )
      }

      if (/capacidad/i.test(assessment.reason)) {
        conflicts.push(
          warningFromPlacement(
            yearId,
            item,
            'capacity',
            'La cantidad de estudiantes del grupo supera la capacidad registrada del aula.',
          ),
        )
      }

      if (/preferencia/i.test(assessment.reason)) {
        conflicts.push(
          warningFromPlacement(
            yearId,
            item,
            'preference',
            `${item.teacherName} registró esta franja como preferencia.`,
          ),
        )
      }
    }
  })

  groups.forEach((group) => {
    const rows = buildAssignmentRows(
      curricularRowsSource(yearId, group),
      seedAssignments.filter(
        (item) => item.yearId === yearId && item.groupId === group.id && item.tab === 'curricular',
      ),
      teachers,
    )
    const pending = pendingPlacements(rows, yearPlacements, yearId, group.id)

    pending.forEach((item) => {
      conflicts.push(
        conflict({
          yearId,
          kind: 'incomplete-hours',
          severity: 'ADVERTENCIA',
          message: `${item.subject} del grupo ${group.code} tiene ${item.pendingHours} h pendientes de programar.`,
          day: null,
          bandId: null,
          affected: `Grupo ${group.code}`,
          subject: item.subject,
          teacherName: item.teacherName,
          classroom: '',
          groupCode: group.code,
          groupIds: [group.id],
          teacherNames: item.teacherName ? [item.teacherName] : [],
          classrooms: [],
        }),
      )
    })
  })

  return conflicts
}

export function applyConflictStatus(
  conflicts: ScheduleConflict[],
  resolvedIds: string[],
): ReviewedConflict[] {
  const resolved = new Set(resolvedIds)

  return conflicts.map((item) => ({
    ...item,
    status: resolved.has(item.id) ? 'RESUELTO' : 'PENDIENTE',
  }))
}

export function summarizeConflicts(conflicts: ReviewedConflict[]) {
  const errors = conflicts.filter((item) => item.severity === 'ERROR')
  const warnings = conflicts.filter((item) => item.severity === 'ADVERTENCIA')
  const resolved = conflicts.filter((item) => item.status === 'RESUELTO')
  const pending = conflicts.filter((item) => item.status === 'PENDIENTE')
  const pendingErrors = errors.filter((item) => item.status === 'PENDIENTE').length

  return {
    total: conflicts.length,
    errors: errors.length,
    warnings: warnings.length,
    resolved: resolved.length,
    pending: pending.length,
    readiness: publishReadiness(pendingErrors, pending.filter((item) => item.severity === 'ADVERTENCIA').length),
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

export function validationGroupOptions(yearId: string) {
  return [
    { value: '', label: 'Todos' },
    ...groupsForYear(yearId).map((group) => ({ value: group.id, label: group.code })),
  ]
}

export function validationTeacherOptions(
  conflicts: ScheduleConflict[],
  teachers: Teacher[],
) {
  const names = unique([
    ...conflicts.flatMap((item) => item.teacherNames),
    ...teachers.map(formatTeacherName),
  ]).sort((left, right) => left.localeCompare(right, 'es'))

  return [{ value: '', label: 'Todos' }, ...names.map((name) => ({ value: name, label: name }))]
}

export function validationClassroomOptions(conflicts: ScheduleConflict[]) {
  const rooms = unique([
    ...scheduleClassrooms.map((room) => room.value),
    ...conflicts.flatMap((item) => item.classrooms),
  ])

  return [{ value: '', label: 'Todas' }, ...rooms.map((room) => ({ value: room, label: room }))]
}

export function defaultValidationYearId() {
  return academicYears.find((year) => year.current)?.id ?? academicYears[0]?.id ?? ''
}

export function defaultValidationGroupId(yearId: string) {
  return groupsForYear(yearId)[0]?.id ?? ''
}

function warningFromPlacement(
  yearId: string,
  item: SchedulePlacement,
  kind: ConflictKind,
  message: string,
): ScheduleConflict {
  return conflict({
    yearId: item.yearId,
    kind,
    severity: 'ADVERTENCIA',
    message,
    day: item.day,
    bandId: item.bandId,
    affected: kind === 'capacity' ? item.classroom : `Grupo ${groupCode(yearId, item.groupId)}`,
    subject: subjectName(item.subjectKey),
    teacherName: item.teacherName,
    classroom: item.classroom,
    groupCode: groupCode(yearId, item.groupId),
    groupIds: [item.groupId],
    teacherNames: [item.teacherName],
    classrooms: [item.classroom],
  })
}

function duplicates(
  items: SchedulePlacement[],
  keyOf: (item: SchedulePlacement) => string,
) {
  const groups = new Map<string, SchedulePlacement[]>()

  items.forEach((item) => {
    const key = keyOf(item)
    const current = groups.get(key) ?? []
    current.push(item)
    groups.set(key, current)
  })

  return [...groups.values()].filter((peers) => peers.length > 1)
}

function conflict(
  values: Omit<ScheduleConflict, 'id' | 'slotLabel'> & { day: WeekDay | null; bandId: string | null },
): ScheduleConflict {
  const slotLabel = formatConflictSlot(values.day, values.bandId)

  return {
    ...values,
    slotLabel,
    id: [
      values.yearId,
      values.kind,
      values.day ?? 'none',
      values.bandId ?? 'none',
      values.groupIds.join(','),
      values.teacherNames.join(','),
      values.classrooms.join(','),
      values.message,
    ].join('|'),
  }
}

function groupCode(yearId: string, groupId: string) {
  return groupsForYear(yearId).find((group) => group.id === groupId)?.code ?? groupId
}

function subjectName(subjectKey: string) {
  return scheduleSubject(subjectKey)?.subject ?? 'Asignatura'
}

function display(value: string) {
  return value || '—'
}

function unique(values: string[]) {
  return [...new Set(values.filter(Boolean))]
}

function normalize(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}
