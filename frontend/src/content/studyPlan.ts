import {
  academicYears,
  labelYearStatus,
  type AcademicYear,
} from './academicYear'

export type StudyPlanGrade = {
  id: string
  label: string
  level: string
  weeklyHours: number
}

export type StudyPlanEntry = {
  id: string
  yearId: string
  gradeId: string
  area: string
  subject: string
  weeklyHours: number
  shift: string
  note: string | null
}

export const studyPlanGrades: StudyPlanGrade[] = [
  { id: '6', label: '6°', level: 'Bachillerato', weeklyHours: 30 },
  { id: '7', label: '7°', level: 'Bachillerato', weeklyHours: 30 },
  { id: '8', label: '8°', level: 'Bachillerato', weeklyHours: 30 },
  { id: '9', label: '9°', level: 'Bachillerato', weeklyHours: 30 },
  { id: '10', label: '10°', level: 'Media Técnica', weeklyHours: 37 },
  { id: '11', label: '11°', level: 'Media Técnica', weeklyHours: 37 },
]

export const studyPlanGradeOptions = studyPlanGrades.map((grade) => ({
  value: grade.id,
  label: grade.label,
}))

// El backend aún no expone el plan. Estas filas reproducen el diseño de 2026 · 6°.
export const studyPlanEntries: StudyPlanEntry[] = [
  {
    id: '2026-6-mat',
    yearId: '2026',
    gradeId: '6',
    area: 'Matemáticas',
    subject: 'Matemáticas',
    weeklyHours: 5,
    shift: 'Mañana',
    note: null,
  },
  {
    id: '2026-6-bio',
    yearId: '2026',
    gradeId: '6',
    area: 'Ciencias Naturales',
    subject: 'Biología',
    weeklyHours: 4,
    shift: 'Mañana',
    note: 'Requiere laboratorio',
  },
  {
    id: '2026-6-ing',
    yearId: '2026',
    gradeId: '6',
    area: 'Inglés',
    subject: 'Inglés',
    weeklyHours: 3,
    shift: 'Mañana',
    note: null,
  },
]

export function studyPlanYearOptions() {
  return academicYears.map((year) => ({
    value: year.id,
    label: String(year.year),
  }))
}

export function currentStudyPlanYear() {
  return academicYears.find((year) => year.current) ?? academicYears[0]
}

export function formatAvailableYears(years: AcademicYear[] = academicYears) {
  return years
    .map((year) => `${year.year} — ${labelYearStatus(year.status)}`)
    .join(' · ')
}

export function findStudyPlanGrade(gradeId: string) {
  return studyPlanGrades.find((grade) => grade.id === gradeId) ?? studyPlanGrades[0]
}

export function filterStudyPlanEntries(
  entries: StudyPlanEntry[],
  filters: { yearId: string; gradeId: string; area: string; query: string },
) {
  const query = filters.query.trim().toLowerCase()

  return entries.filter((entry) => {
    if (entry.yearId !== filters.yearId || entry.gradeId !== filters.gradeId) {
      return false
    }

    if (filters.area && entry.area !== filters.area) {
      return false
    }

    if (!query) {
      return true
    }

    return entry.subject.toLowerCase().includes(query)
  })
}

export function configuredWeeklyHours(entries: StudyPlanEntry[]) {
  return entries.reduce((total, entry) => total + entry.weeklyHours, 0)
}

export const studyPlanShifts = ['Mañana', 'Tarde'] as const

export const planSubjectFormHint =
  'La asignatura se selecciona desde M-07. El área se deriva automáticamente. Las horas corresponden al Plan de Estudios del grado.'

export const derivedAreaLabel = 'Derivada de la asignatura'

export function studyPlanShiftOptions(current?: string) {
  const shifts = current && !studyPlanShifts.includes(current as (typeof studyPlanShifts)[number])
    ? [current, ...studyPlanShifts]
    : [...studyPlanShifts]

  return shifts.map((shift) => ({ value: shift, label: shift }))
}
