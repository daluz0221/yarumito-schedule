import type { StatItemData } from './dashboard'

export type AcademicYearStatus = 'CERRADO' | 'ACTIVO' | 'PLANEACION'

export type AcademicYear = {
  id: string
  year: number
  startDate: string
  endDate: string
  status: AcademicYearStatus
  current: boolean
}

export type SchoolLevel = 'BACHILLERATO' | 'MEDIA'

export type SchoolGroupStatus = 'ACTIVO' | 'INACTIVO'

export type SchoolGroup = {
  id: string
  yearId: string
  schoolYear?: number
  code: string
  grade: string
  level: SchoolLevel
  weeklyHours: number | null
  students: number
  director: string | null
  status: SchoolGroupStatus
  sedeId?: string
  sedeNombre?: string
}

export type SchoolGroupFilters = {
  grade: string
  status: string
  query: string
}

export const academicYears: AcademicYear[] = [
  {
    id: '2025',
    year: 2025,
    startDate: '20/01/2025',
    endDate: '28/11/2025',
    status: 'CERRADO',
    current: false,
  },
  {
    id: '2026',
    year: 2026,
    startDate: '19/01/2026',
    endDate: '27/11/2026',
    status: 'ACTIVO',
    current: true,
  },
  {
    id: '2027',
    year: 2027,
    startDate: '18/01/2027',
    endDate: '26/11/2027',
    status: 'PLANEACION',
    current: false,
  },
]


export const groupStatusFilterOptions = [
  { value: '', label: 'Todos' },
  { value: 'ACTIVO', label: 'Activo' },
  { value: 'INACTIVO', label: 'Inactivo' },
]

export const groupStatusOptions = groupStatusFilterOptions.filter(
  (option) => option.value,
)

export const classroomOptions = [
  { value: '', label: 'Sin asignar' },
  { value: 'A-101', label: 'A-101' },
  { value: 'A-102', label: 'A-102' },
  { value: 'B-201', label: 'B-201' },
]

export function isGroupCodeTaken(
  groups: SchoolGroup[],
  yearId: string,
  code: string,
  ignoreId?: string,
) {
  const normalized = code.trim().toLowerCase()

  if (!normalized) {
    return false
  }

  return groups.some(
    (item) =>
      item.yearId === yearId &&
      item.id !== ignoreId &&
      item.code.trim().toLowerCase() === normalized,
  )
}

export function matchesAcademicYear(group: SchoolGroup, year: AcademicYear) {
  return (
    group.yearId === year.id ||
    group.yearId === String(year.year) ||
    group.schoolYear === year.year
  )
}

export function academicYearOptions(years: AcademicYear[]) {
  return years.map((year) => ({
    value: year.id,
    label: String(year.year),
  }))
}

export function labelYearStatus(status: AcademicYearStatus) {
  const labels: Record<AcademicYearStatus, string> = {
    CERRADO: 'Cerrado',
    ACTIVO: 'Activo',
    PLANEACION: 'Planeación',
  }

  return labels[status]
}

export const academicYearStatusOptions = (
  ['PLANEACION', 'ACTIVO', 'CERRADO'] as AcademicYearStatus[]
).map((status) => ({
  value: status,
  label: labelYearStatus(status),
}))


export function labelSchoolLevel(level: SchoolLevel) {
  const labels: Record<SchoolLevel, string> = {
    BACHILLERATO: 'Bachillerato',
    MEDIA: 'Media',
  }

  return labels[level]
}

export function labelGroupStatus(status: SchoolGroupStatus) {
  const labels: Record<SchoolGroupStatus, string> = {
    ACTIVO: 'Activo',
    INACTIVO: 'Inactivo',
  }

  return labels[status]
}

export function formatYearTimeline(years: AcademicYear[]) {
  return [...years]
    .sort((left, right) => left.year - right.year)
    .map((year) => {
      const current = year.current ? ' (actual)' : ''
      return `${year.year} — ${labelYearStatus(year.status)}${current}`
    })
    .join(' · ')
}

export function nextPlannedYear(years: AcademicYear[], selected: AcademicYear) {
  return (
    [...years]
      .filter((year) => year.status === 'PLANEACION' && year.year > selected.year)
      .sort((left, right) => left.year - right.year)[0] ?? null
  )
}

export function filterSchoolGroups(
  groups: SchoolGroup[],
  filters: SchoolGroupFilters,
) {
  const query = filters.query.trim().toLowerCase()

  return groups.filter((item) => {
    if (filters.grade && item.grade !== filters.grade) {
      return false
    }

    if (filters.status && item.status !== filters.status) {
      return false
    }

    if (query && !item.code.toLowerCase().includes(query)) {
      return false
    }

    return true
  })
}

export function schoolGroupStats(groups: SchoolGroup[]): StatItemData[] {
  const active = groups.filter((item) => item.status === 'ACTIVO')

  return [
    {
      id: 'active',
      value: String(active.length),
      label: 'Grupos activos',
    },
    {
      id: 'bachillerato',
      value: String(
        active.filter((item) => item.level === 'BACHILLERATO').length,
      ),
      label: 'Bachillerato',
    },
    {
      id: 'media',
      value: String(active.filter((item) => item.level === 'MEDIA').length),
      label: 'Media',
    },
  ]
}
