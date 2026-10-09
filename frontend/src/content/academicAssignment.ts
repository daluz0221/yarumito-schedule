import { schoolGroups, type SchoolGroup } from './academicYear'
import { findStudyPlanGrade, studyPlanEntries, type StudyPlanEntry } from './studyPlan'
import { formatTeacherName, type Teacher } from './teachers'

export type AssignmentTab = 'curricular' | 'activity'

export type AssignmentKind = 'PRINCIPAL' | 'IDONEIDAD' | 'TRANSVERSAL' | 'ACTIVIDAD'

export type CurricularKind = Exclude<AssignmentKind, 'ACTIVIDAD'>

export type AssignmentStatus = 'COMPLETA' | 'PARCIAL' | 'SIN_ASIGNAR'

export type AssignmentRecord = {
  id: string
  yearId: string
  groupId: string
  subjectKey: string
  tab: AssignmentTab
  teacherId: string | null
  teacherName: string | null
  assignedHours: number
  kind: AssignmentKind
  note: string
}

export type InstitutionalActivity = {
  id: string
  name: string
  area: string
  weeklyHours: number
}

export type AssignmentRow = {
  key: string
  area: string
  subject: string
  requiredHours: number
  assignedHours: number
  teacherId: string | null
  teacherName: string
  kind: AssignmentKind | null
  kindLabel: string
  status: AssignmentStatus
  assigned: boolean
  note: string
}

export const assignmentTabs: { id: AssignmentTab; label: string }[] = [
  { id: 'curricular', label: 'Asignaciones curriculares' },
  { id: 'activity', label: 'Actividades y proyectos' },
]

export const assignmentKindOptions = [
  { value: 'PRINCIPAL', label: 'Principal' },
  { value: 'IDONEIDAD', label: 'Idoneidad' },
  { value: 'TRANSVERSAL', label: 'Transversal' },
  { value: 'ACTIVIDAD', label: 'Actividad / proyecto' },
]

export const curricularKindOptions = assignmentKindOptions.filter(
  (option) => option.value !== 'ACTIVIDAD',
)

export const assignmentKindHint =
  'Principal: área de nombramiento · Idoneidad: válida para la asignatura · Transversal: permitida por plan, idoneidad y reglas institucionales.'

export const assignmentKindDetails: {
  value: CurricularKind
  label: string
  description: string
}[] = [
  {
    value: 'PRINCIPAL',
    label: 'Principal',
    description: 'Corresponde al área principal de nombramiento del docente.',
  },
  {
    value: 'IDONEIDAD',
    label: 'Idoneidad',
    description:
      'Requiere una idoneidad válida para la asignatura. Un docente puede tener múltiples idoneidades.',
  },
  {
    value: 'TRANSVERSAL',
    label: 'Transversal · HU-06',
    description:
      'La asignatura debe pertenecer al Plan de Estudios y cumplir idoneidad + reglas institucionales. Ética, Educación Artística y Religión son ejemplos condicionados; no habilitada para cualquier docente.',
  },
]

export const assignmentKindModalNote =
  'Las filas actuales coinciden con el área principal. El Rector conserva la decisión final; el tipo no sustituye la verificación académica.'

export const assignmentKindModalHint =
  'Selección demostrativa. Debe comprobarse idoneidad y, para transversales, el Plan de Estudios y las reglas vigentes antes de formalizar la asignación.'

export const assignmentTeacherPriorityHint =
  'Prioridad: área principal y después idoneidades válidas. Sin límite de idoneidades.'

export const assignmentRestrictionWarning =
  'Este docente tiene restricciones de disponibilidad registradas. Se evaluarán al construir el horario; no se declara conflicto en esta asignación.'

export const activityAssignmentHint =
  'Actividad institucional demostrativa; no corresponde a una asignatura del Plan de Estudios.'

export const institutionalActivities: InstitutionalActivity[] = [
  {
    id: 'acompanamiento',
    name: 'Proyecto de acompañamiento institucional',
    area: 'Institucional',
    weeklyHours: 2,
  },
  {
    id: 'convivencia',
    name: 'Proyecto de convivencia',
    area: 'Institucional',
    weeklyHours: 2,
  },
  {
    id: 'emprendimiento',
    name: 'Proyecto de emprendimiento',
    area: 'Institucional',
    weeklyHours: 2,
  },
]

const gradeNameToId: Record<string, string> = {
  Sexto: '6',
  Séptimo: '7',
  Octavo: '8',
  Noveno: '9',
  Décimo: '10',
  Undécimo: '11',
}

export const assignmentPrioritySteps =
  '1. Área principal → 2. Idoneidades válidas → 3. Transversales permitidas → 4. Actividades / proyectos institucionales'

export const assignmentPriorityNote =
  'El Rector conserva la decisión final. No se realizarán asignaciones automáticas.'

export const assignmentFormHint =
  'Las horas requeridas vienen del plan de estudios. El docente se elige entre los registrados y el tipo guía la prioridad de carga, sin automatizar la decisión.'

export const seedAssignments: AssignmentRecord[] = [
  {
    id: '2026-601-mat',
    yearId: '2026',
    groupId: '601',
    subjectKey: '2026-6-mat',
    tab: 'curricular',
    teacherId: null,
    teacherName: 'Carlos Pérez',
    assignedHours: 5,
    kind: 'PRINCIPAL',
    note: 'Asignación curricular del grupo 601',
  },
  {
    id: '2026-601-bio',
    yearId: '2026',
    groupId: '601',
    subjectKey: '2026-6-bio',
    tab: 'curricular',
    teacherId: null,
    teacherName: 'Diana Marcela Ruiz',
    assignedHours: 2,
    kind: 'PRINCIPAL',
    note: 'Asignación curricular del grupo 601',
  },
]

export function groupsForYear(yearId: string) {
  return schoolGroups.filter((group) => group.yearId === yearId && group.status === 'ACTIVO')
}

export function assignmentGroupOptions(yearId: string) {
  return groupsForYear(yearId).map((group) => ({
    value: group.id,
    label: group.code,
  }))
}

export function gradeIdFromGroup(group: SchoolGroup) {
  return gradeNameToId[group.grade] ?? '6'
}

export function gradeLabelFromGroup(group: SchoolGroup) {
  return findStudyPlanGrade(gradeIdFromGroup(group)).label
}

export function planEntriesForGroup(yearId: string, group: SchoolGroup) {
  const gradeId = gradeIdFromGroup(group)
  return studyPlanEntries.filter(
    (entry) => entry.yearId === yearId && entry.gradeId === gradeId,
  )
}

export function labelAssignmentKind(kind: AssignmentKind | null) {
  if (!kind) {
    return '—'
  }

  return assignmentKindOptions.find((option) => option.value === kind)?.label ?? kind
}

export function assignmentStatus(
  requiredHours: number,
  assignedHours: number,
  hasTeacher: boolean,
): AssignmentStatus {
  if (!hasTeacher || assignedHours <= 0) {
    return 'SIN_ASIGNAR'
  }

  if (assignedHours >= requiredHours) {
    return 'COMPLETA'
  }

  return 'PARCIAL'
}

export function labelAssignmentStatus(status: AssignmentStatus) {
  const labels: Record<AssignmentStatus, string> = {
    COMPLETA: 'Completa',
    PARCIAL: 'Parcial',
    SIN_ASIGNAR: 'Sin asignar',
  }

  return labels[status]
}

export function resolveAssignmentTeacher(
  assignment: AssignmentRecord | undefined,
  teachers: Teacher[],
) {
  if (!assignment) {
    return { teacherId: null, teacherName: '' }
  }

  const byId = assignment.teacherId
    ? teachers.find((teacher) => teacher.id === assignment.teacherId)
    : undefined
  const byName = assignment.teacherName
    ? teachers.find(
        (teacher) =>
          formatTeacherName(teacher).toLowerCase() === assignment.teacherName?.toLowerCase(),
      )
    : undefined
  const teacher = byId ?? byName

  return {
    teacherId: teacher?.id ?? assignment.teacherId,
    teacherName: teacher ? formatTeacherName(teacher) : assignment.teacherName ?? '',
  }
}

export function buildAssignmentRows(
  subjects: Array<{ key: string; area: string; subject: string; requiredHours: number }>,
  assignments: AssignmentRecord[],
  teachers: Teacher[],
): AssignmentRow[] {
  return subjects.map((item) => {
    const assignment = assignments.find((record) => record.subjectKey === item.key)
    const teacher = resolveAssignmentTeacher(assignment, teachers)
    const assignedHours = assignment?.assignedHours ?? 0
    const assigned = Boolean(teacher.teacherName) && assignedHours > 0
    const status = assignmentStatus(item.requiredHours, assignedHours, assigned)

    return {
      key: item.key,
      area: item.area,
      subject: item.subject,
      requiredHours: item.requiredHours,
      assignedHours,
      teacherId: teacher.teacherId,
      teacherName: teacher.teacherName,
      kind: assignment?.kind ?? null,
      kindLabel: labelAssignmentKind(assignment?.kind ?? null),
      status,
      assigned,
      note: assignment?.note ?? '',
    }
  })
}

export function curricularSubjects(entries: StudyPlanEntry[]) {
  return entries.map((entry) => ({
    key: entry.id,
    area: entry.area,
    subject: entry.subject,
    requiredHours: entry.weeklyHours,
  }))
}

export function activitySubjects() {
  return institutionalActivities.map((item) => ({
    key: item.id,
    area: item.area,
    subject: item.name,
    requiredHours: item.weeklyHours,
  }))
}

export function summarizeRows(rows: AssignmentRow[]) {
  const requiredHours = rows.reduce((total, row) => total + row.requiredHours, 0)
  const assignedHours = rows.reduce((total, row) => total + row.assignedHours, 0)
  const hasAny = rows.some((row) => row.assigned)
  const allComplete =
    rows.length > 0 && rows.every((row) => row.status === 'COMPLETA')
  const status: AssignmentStatus = allComplete
    ? 'COMPLETA'
    : hasAny
      ? 'PARCIAL'
      : 'SIN_ASIGNAR'

  return { requiredHours, assignedHours, status }
}

export function nextAssignmentId(
  yearId: string,
  groupId: string,
  subjectKey: string,
) {
  return `${yearId}-${groupId}-${subjectKey}`
}

export function subjectPlanHint(gradeLabel: string, row?: AssignmentRow | null) {
  if (!row) {
    return `Plan de estudios de ${gradeLabel}.`
  }

  const pending = Math.max(row.requiredHours - row.assignedHours, 0)
  return `Plan de estudios de ${gradeLabel} · Requeridas: ${row.requiredHours} h · Asignadas: ${row.assignedHours} h · Pendientes: ${pending} h`
}

export function hoursStatusCopy(requiredHours: number, assignedHours: number) {
  if (assignedHours > requiredHours) {
    const extra = assignedHours - requiredHours
    return {
      tone: 'warning' as const,
      text: `Excede · ${assignedHours} h asignadas frente a ${requiredHours} h requeridas. Revisa la asignación: supera el plan en ${extra} h.`,
    }
  }

  if (assignedHours === requiredHours && assignedHours > 0) {
    return {
      tone: 'success' as const,
      text: `Completa · ${assignedHours} h asignadas de ${requiredHours} h requeridas.`,
    }
  }

  const pending = Math.max(requiredHours - assignedHours, 0)
  return {
    tone: 'pending' as const,
    text: `Pendiente · ${assignedHours} h asignadas de ${requiredHours} h requeridas. Faltan ${pending} h.`,
  }
}

function normalizeLabel(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

export function teacherMatchesArea(teacherArea: string, subjectArea: string) {
  return Boolean(teacherArea) && normalizeLabel(teacherArea) === normalizeLabel(subjectArea)
}

export function teacherCompatibilityHint(
  teacherStatus: string,
  teacherArea: string,
  subjectArea: string,
  hasValidIdoneidad: boolean,
) {
  if (teacherMatchesArea(teacherArea, subjectArea)) {
    return `${teacherStatus} · Área principal: ${teacherArea} · Coincidencia por área principal.`
  }

  if (hasValidIdoneidad) {
    return `${teacherStatus} · Área principal: ${teacherArea || 'sin registrar'} · Idoneidad válida para la asignatura.`
  }

  return `${teacherStatus} · Área principal: ${teacherArea || 'sin registrar'} · Sin coincidencia automática; el Rector decide.`
}

export function namedTeacherValue(name: string) {
  return `name:${name}`
}

export function parseTeacherSelection(value: string) {
  if (value.startsWith('name:')) {
    return { teacherId: null, teacherName: value.slice(5) }
  }

  return { teacherId: value, teacherName: '' }
}
