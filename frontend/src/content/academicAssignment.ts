
export type AssignmentTab = 'curricular' | 'activity'

export type AssignmentKind = 'PRINCIPAL' | 'IDONEIDAD' | 'TRANSVERSAL' | 'ACTIVIDAD'

export type CurricularKind = Exclude<AssignmentKind, 'ACTIVIDAD'>

export type AssignmentStatus = 'COMPLETA' | 'PARCIAL' | 'SIN_ASIGNAR'

export type AssignmentPersistence = 'BORRADOR' | 'CONFIRMADA'

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
  assignmentId: string | null
  estado: AssignmentPersistence | null
}

export type CurricularAssignmentSource = {
  id: string
  asignaturaId: string
  asignaturaNombre: string
  docenteId: string
  docenteNombre: string
  horasSemanales: number
  observacion: string | null
  estado: AssignmentPersistence
}

export const assignmentTabs: { id: AssignmentTab; label: string }[] = [
  { id: 'curricular', label: 'Asignaciones curriculares' },
  { id: 'activity', label: 'Actividades y proyectos' },
]



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
  'No corresponde a una asignatura del Plan de Estudios. La justificación queda registrada con la asignación.'

export const institutionalActivities: InstitutionalActivity[] = [
  {
    id: '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e501',
    name: 'Proyecto de acompañamiento institucional',
    area: 'Institucional',
    weeklyHours: 2,
  },
  {
    id: '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e502',
    name: 'Proyecto de convivencia',
    area: 'Institucional',
    weeklyHours: 2,
  },
  {
    id: '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e503',
    name: 'Proyecto de emprendimiento',
    area: 'Institucional',
    weeklyHours: 2,
  },
]

export type ActivityAssignmentSource = {
  id: string
  docenteId: string
  docenteNombre: string
  actividadId: string
  actividadNombre: string
  tipoActividad: string
  horasSemanales: number
  justificacion: string
}

export const assignmentPrioritySteps =
  '1. Área principal → 2. Idoneidades válidas → 3. Transversales permitidas → 4. Actividades / proyectos institucionales'

export const assignmentPriorityNote =
  'El Rector conserva la decisión final. No se realizarán asignaciones automáticas.'









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



function filaCurricular(
  item: { key: string; area: string; subject: string; requiredHours: number },
  assignment: CurricularAssignmentSource | undefined,
): AssignmentRow {
  const assignedHours = assignment?.horasSemanales ?? 0
  const teacherName = assignment?.docenteNombre?.trim() ?? ''
  const assigned = Boolean(teacherName) && assignedHours > 0

  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours,
    teacherId: assignment?.docenteId ?? null,
    teacherName,
    kind: null,
    kindLabel: assignment ? 'Curricular' : '—',
    status: assignmentStatus(item.requiredHours, assignedHours, assigned),
    assigned,
    note: assignment?.observacion?.trim() ?? '',
    assignmentId: assignment?.id ?? null,
    estado: assignment?.estado ?? null,
  }
}

export function mergeCurricularAssignments(
  subjects: Array<{ key: string; area: string; subject: string; requiredHours: number }>,
  assignments: CurricularAssignmentSource[],
): AssignmentRow[] {
  const bySubject = new Map<string, CurricularAssignmentSource>()

  assignments.forEach((item) => {
    const current = bySubject.get(item.asignaturaId)

    if (!current || (current.estado !== 'BORRADOR' && item.estado === 'BORRADOR')) {
      bySubject.set(item.asignaturaId, item)
    }
  })

  const covered = new Set(subjects.map((item) => item.key))
  const rows = subjects.map((item) => filaCurricular(item, bySubject.get(item.key)))

  bySubject.forEach((assignment) => {
    if (covered.has(assignment.asignaturaId)) {
      return
    }

    rows.push(
      filaCurricular(
        {
          key: assignment.asignaturaId,
          area: '—',
          subject: assignment.asignaturaNombre?.trim() || 'Asignatura',
          requiredHours: assignment.horasSemanales,
        },
        assignment,
      ),
    )
  })

  return rows
}


export function activitySubjects() {
  return institutionalActivities.map((item) => ({
    key: item.id,
    area: item.area,
    subject: item.name,
    requiredHours: item.weeklyHours,
  }))
}

const tipoActividadLabel: Record<string, string> = {
  PROYECTO_PEDAGOGICO: 'Proyecto pedagógico',
  ACTIVIDAD_INSTITUCIONAL: 'Actividad institucional',
  DIRECCION_DE_GRUPO: 'Dirección de grupo',
  COMITE: 'Comité',
  OTRO: 'Otro',
}

function etiquetaTipoActividad(tipo: string) {
  return tipoActividadLabel[tipo] ?? (tipo || 'Actividad / proyecto')
}

function filaActividadVacia(item: {
  key: string
  area: string
  subject: string
  requiredHours: number
}): AssignmentRow {
  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours: 0,
    teacherId: null,
    teacherName: '',
    kind: null,
    kindLabel: '—',
    status: 'SIN_ASIGNAR',
    assigned: false,
    note: '',
    assignmentId: null,
    estado: null,
  }
}

function filaActividadAsignada(
  item: { key: string; area: string; subject: string; requiredHours: number },
  assignment: ActivityAssignmentSource,
): AssignmentRow {
  const assignedHours = assignment.horasSemanales
  const teacherName = assignment.docenteNombre?.trim() ?? ''

  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours,
    teacherId: assignment.docenteId,
    teacherName,
    kind: 'ACTIVIDAD',
    kindLabel: etiquetaTipoActividad(assignment.tipoActividad),
    status: assignmentStatus(item.requiredHours, assignedHours, Boolean(teacherName)),
    assigned: Boolean(teacherName) && assignedHours > 0,
    note: assignment.justificacion?.trim() ?? '',
    assignmentId: null,
    estado: null,
  }
}

export function filasActividadesInstitucionales(
  assignments: ActivityAssignmentSource[],
): AssignmentRow[] {
  const subjects = activitySubjects()
  const pending = new Map<string, ActivityAssignmentSource[]>()

  assignments.forEach((item) => {
    const list = pending.get(item.actividadId) ?? []
    list.push(item)
    pending.set(item.actividadId, list)
  })

  const rows: AssignmentRow[] = []
  const used = new Set<string>()

  subjects.forEach((subject) => {
    const matches = pending.get(subject.key) ?? []

    if (matches.length === 0) {
      rows.push(filaActividadVacia(subject))
      return
    }

    matches.forEach((match, index) => {
      used.add(match.id)
      rows.push(
        filaActividadAsignada(
          index === 0 ? subject : { ...subject, key: match.id },
          match,
        ),
      )
    })
  })

  assignments.forEach((assignment) => {
    if (used.has(assignment.id)) {
      return
    }

    rows.push(
      filaActividadAsignada(
        {
          key: assignment.id,
          area: 'Institucional',
          subject: assignment.actividadNombre?.trim() || 'Actividad institucional',
          requiredHours: assignment.horasSemanales,
        },
        assignment,
      ),
    )
  })

  return rows
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
