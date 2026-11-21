import { availabilityDays, type WeekDay } from './teacherRestrictions'

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


export function scheduleDayOptions() {
  return availabilityDays.map((day) => ({ value: day, label: day }))
}

// Referencia local de la validación. La construcción de horario carga la
// grilla desde GET /api/horarios/grupos/{grupoId}.




export function blockKey(day: WeekDay, bandId: string) {
  return `${day}-${bandId}`
}






