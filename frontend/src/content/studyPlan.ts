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


// Filas de referencia para pantallas que aún no consultan el plan.
// La pantalla de plan de estudios lee GET /api/v1/planes-estudio.



export function formatAvailableYears(years: AcademicYear[] = academicYears) {
  return years
    .map((year) => `${year.year} — ${labelYearStatus(year.status)}`)
    .join(' · ')
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



export const derivedAreaLabel = 'Derivada de la asignatura'

