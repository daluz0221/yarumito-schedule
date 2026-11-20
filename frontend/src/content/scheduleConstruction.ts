import {
  availabilityDays,
  morningBands,
  type WeekDay,
} from './teacherRestrictions'

export type PlacementTone = 'ok' | 'conflict' | 'warning'

export type SchedulePlacement = {
  id: string
  yearId: string
  groupId: string
  day: WeekDay
  bandId: string
  subjectKey: string
  teacherName: string
  classroom: string
}

export type PlacementAssessment = {
  tone: PlacementTone
  reason: string
}

export type ScheduleBlock = SchedulePlacement & {
  subject: string
  tone: PlacementTone
  reason: string
  locked?: boolean
}

export type PendingPlacement = {
  key: string
  subject: string
  teacherName: string
  pendingHours: number
  note: string | null
}

export type ScheduleAlertKind =
  | 'teacher-busy'
  | 'classroom-busy'
  | 'teacher-unavailable'
  | 'slot-taken'
  | 'capacity'

export type ScheduleAlert = {
  kind: ScheduleAlertKind
  teacherName?: string
  classroom?: string
}

export type ScheduleDraft = {
  id?: string
  yearId: string
  groupId: string
  day: WeekDay
  bandId: string
  teacherName: string
  classroom: string
}


export const scheduleViewOptions = [{ value: 'grupo', label: 'Por grupo' }]



export const scheduleClassrooms = [
  { value: 'Aula 201', label: 'Aula 201', capacity: 30 },
  { value: 'Aula 202', label: 'Aula 202', capacity: 32 },
  { value: 'Laboratorio', label: 'Laboratorio', capacity: 40 },
  { value: 'Lab. 1', label: 'Lab. 1', capacity: 24 },
]

export function scheduleDayOptions() {
  return availabilityDays.map((day) => ({ value: day, label: day }))
}

export function scheduleBandOptions() {
  return classBands().map((band) => ({
    value: band.id,
    label: band.label.replace(/\b(\d):/g, '0$1:'),
  }))
}

export function scheduleClassroomOptions() {
  return scheduleClassrooms.map((room) => ({ value: room.value, label: room.label }))
}


// Referencia local de la validación. La construcción de horario carga la
// grilla desde GET /api/horarios/grupos/{grupoId}.

export function classBands() {
  return morningBands.filter((band) => band.kind === 'class')
}




export function blockKey(day: WeekDay, bandId: string) {
  return `${day}-${bandId}`
}






