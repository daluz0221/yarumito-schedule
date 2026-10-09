import { useState } from 'react'
import {
  academicYears,
  filterSchoolGroups,
  schoolGroupStats,
  schoolGroups,
} from '../../../content/academicYear'
import { Text } from '../../atoms/Text'
import { AcademicYearCard } from '../AcademicYearCard'
import { SchoolGroupsSection } from '../SchoolGroupsSection'
import styles from './AcademicYearWorkspace.module.css'

const currentYear =
  academicYears.find((year) => year.current) ?? academicYears[0]

export function AcademicYearWorkspace() {
  const [yearId, setYearId] = useState(currentYear.id)
  const [grade, setGrade] = useState('')
  const [status, setStatus] = useState('')
  const [query, setQuery] = useState('')

  const year =
    academicYears.find((item) => item.id === yearId) ?? currentYear
  const yearGroups = schoolGroups.filter((item) => item.yearId === year.id)
  const rows = filterSchoolGroups(yearGroups, { grade, status, query })

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setGrade('')
    setStatus('')
    setQuery('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Año lectivo y grupos
        </Text>
        <Text variant="body">
          Configura el período académico y los grupos activos de la institución
          para cada año lectivo.
        </Text>
      </div>

      <AcademicYearCard
        years={academicYears}
        year={year}
        onYearChange={selectYear}
      />

      <SchoolGroupsSection
        year={year}
        groups={yearGroups}
        stats={schoolGroupStats(yearGroups)}
        grade={grade}
        status={status}
        query={query}
        rows={rows}
        emptyMessage={
          yearGroups.length === 0
            ? 'Este año lectivo aún no tiene grupos registrados.'
            : 'No se encontraron grupos con esos filtros.'
        }
        onGradeChange={setGrade}
        onStatusChange={setStatus}
        onQueryChange={setQuery}
      />
    </section>
  )
}
