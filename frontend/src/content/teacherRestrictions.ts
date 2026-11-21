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

export function restrictionDayOptions() {
  return [
    { value: '', label: 'Seleccione un día' },
    ...availabilityDays.map((day) => ({ value: day, label: day })),
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

export function restrictionEffect(status: RestrictionKind) {
  if (status === 'NO_DISPONIBLE') {
    return 'Esta franja no podrá utilizarse al programar clases para el docente.'
  }

  return 'Esta condición será tenida en cuenta durante la planificación y generará una advertencia.'
}




