export type AvailabilityStatus = 'DISPONIBLE' | 'NO_DISPONIBLE' | 'PREFERENCIA'

export type WeekDay = 'Lunes' | 'Martes' | 'Miércoles' | 'Jueves' | 'Viernes'

export type TimeBand = {
  id: string
  label: string
  kind: 'class' | 'break'
}

export const availabilityDays: WeekDay[] = [
  'Lunes',
  'Martes',
  'Miércoles',
  'Jueves',
  'Viernes',
]

export const morningShiftLabel = 'Mañana'

// Jornada de la mañana según el diseño. El descanso no se edita.
export const morningBands: TimeBand[] = [
  { id: '0630', label: '6:30 – 7:25', kind: 'class' },
  { id: '0725', label: '7:25 – 8:20', kind: 'class' },
  { id: '0820', label: '8:20 – 9:15', kind: 'class' },
  { id: 'break', label: 'Descanso · 9:15 – 9:45 · No editable', kind: 'break' },
  { id: '0945', label: '9:45 – 10:40', kind: 'class' },
  { id: '1040', label: '10:40 – 11:35', kind: 'class' },
]

export type RestrictionKind = Exclude<AvailabilityStatus, 'DISPONIBLE'>

export type TeacherRestriction = {
  yearId: string
  bandId: string
  day: WeekDay
  status: RestrictionKind
  motivo: string
  fileName: string | null
  approvedBy: string | null
}

// El backend aún no expone restricciones. 2026 reproduce la grilla del diseño
// para Carlos Pérez (en el listado real el nombre llega completo). 2025, año
// cerrado, solo conserva el lunes 6:30 del detalle de solo lectura.
const designMarks: TeacherRestriction[] = [
  {
    yearId: '2025',
    bandId: '0630',
    day: 'Lunes',
    status: 'NO_DISPONIBLE',
    motivo: 'Estudios de especialización',
    fileName: 'soporte_estudios.pdf',
    approvedBy: 'Rector / usuario autenticado',
  },
  {
    yearId: '2026',
    bandId: '0630',
    day: 'Lunes',
    status: 'NO_DISPONIBLE',
    motivo: 'Estudios de especialización',
    fileName: 'soporte_estudios.pdf',
    approvedBy: 'Rector / usuario autenticado',
  },
  {
    yearId: '2026',
    bandId: '0725',
    day: 'Martes',
    status: 'NO_DISPONIBLE',
    motivo: 'Compromiso institucional',
    fileName: null,
    approvedBy: null,
  },
  {
    yearId: '2026',
    bandId: '1040',
    day: 'Miércoles',
    status: 'PREFERENCIA',
    motivo: '',
    fileName: null,
    approvedBy: null,
  },
]

const statusLabels: Record<AvailabilityStatus, string> = {
  DISPONIBLE: 'Disponible',
  NO_DISPONIBLE: 'No disponible',
  PREFERENCIA: 'Preferencia',
}

export function labelAvailability(status: AvailabilityStatus) {
  return statusLabels[status]
}

export function restrictionCountLabel(count: number) {
  return count === 1 ? '1 restricción registrada' : `${count} restricciones registradas`
}

function normalizeName(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

function isDesignTeacher(name: string) {
  const normalized = normalizeName(name)
  return normalized.includes('carlos') && normalized.includes('perez')
}

function matchesContext(mark: TeacherRestriction, yearId: string, teacherName: string) {
  return mark.yearId === yearId && isDesignTeacher(teacherName)
}

export function restrictionDayOptions() {
  return [
    { value: '', label: 'Seleccione un día' },
    ...availabilityDays.map((day) => ({ value: day, label: day })),
  ]
}

export function restrictionBandOptions() {
  return [
    { value: '', label: 'Seleccione una franja' },
    ...morningBands
      .filter((band) => band.kind === 'class')
      .map((band) => ({ value: band.id, label: band.label })),
  ]
}

export const restrictionTypeOptions = [
  { value: '', label: 'Seleccione un tipo' },
  { value: 'NO_DISPONIBLE', label: 'No disponible' },
  { value: 'PREFERENCIA', label: 'Preferencia' },
]

export const restrictionTypeHint = 'Tipos permitidos: No disponible · Preferencia'

export const restrictionEditTypeHint = 'Opciones: No disponible · Preferencia'

export const restrictionApprovalHint =
  'Aprobación: el Rector autenticado se registra cuando corresponde.'

export const restrictionEditApprovalHint =
  'Aprobación: gestionada por el Rector autenticado cuando corresponda.'

export function timeBandLabel(bandId: string) {
  return morningBands.find((band) => band.id === bandId)?.label ?? ''
}

export function restrictionEffect(status: RestrictionKind) {
  if (status === 'NO_DISPONIBLE') {
    return 'Esta franja no podrá utilizarse al programar clases para el docente.'
  }

  return 'Esta condición será tenida en cuenta durante la planificación y generará una advertencia.'
}

export function findTeacherRestriction(
  yearId: string,
  teacherName: string,
  bandId: string,
  day: WeekDay,
) {
  return designMarks.find(
    (mark) =>
      matchesContext(mark, yearId, teacherName) && mark.bandId === bandId && mark.day === day,
  )
}

export function availabilityStatus(
  yearId: string,
  teacherName: string,
  bandId: string,
  day: WeekDay,
): AvailabilityStatus {
  return (
    designMarks.find(
      (mark) => matchesContext(mark, yearId, teacherName) && mark.bandId === bandId && mark.day === day,
    )?.status ?? 'DISPONIBLE'
  )
}

export function countRestrictions(yearId: string, teacherName: string) {
  return designMarks.filter((mark) => matchesContext(mark, yearId, teacherName)).length
}

export function preferredRestrictionTeacher(names: string[]) {
  return names.find((name) => isDesignTeacher(name))
}
