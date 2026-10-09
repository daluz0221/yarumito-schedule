import { useEffect, useState } from 'react'
import { listarAreas } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import { academicYears } from '../../../content/academicYear'
import {
  configuredWeeklyHours,
  currentStudyPlanYear,
  filterStudyPlanEntries,
  findStudyPlanGrade,
  studyPlanEntries,
} from '../../../content/studyPlan'
import { Text } from '../../atoms/Text'
import { GradePlanSummary } from '../GradePlanSummary'
import { GradeSubjectsSection } from '../GradeSubjectsSection'
import { StudyPlanSelector } from '../StudyPlanSelector'
import styles from './StudyPlanWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function StudyPlanWorkspace() {
  const [yearId, setYearId] = useState(currentStudyPlanYear().id)
  const [gradeId, setGradeId] = useState('6')
  const [area, setArea] = useState('')
  const [query, setQuery] = useState('')
  const [areas, setAreas] = useState<AreaResponse[]>([])

  useEffect(() => {
    const controller = new AbortController()

    listarAreas(controller.signal)
      .then(setAreas)
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setAreas([])
      })

    return () => controller.abort()
  }, [])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const grade = findStudyPlanGrade(gradeId)
  const planEntries = studyPlanEntries.filter(
    (entry) => entry.yearId === year.id && entry.gradeId === grade.id,
  )
  const rows = filterStudyPlanEntries(studyPlanEntries, {
    yearId: year.id,
    gradeId: grade.id,
    area,
    query,
  })
  const areaNames = areas.length
    ? areas.map((item) => item.nombre)
    : [...new Set(planEntries.map((entry) => entry.area))]

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setArea('')
    setQuery('')
  }

  const selectGrade = (nextGradeId: string) => {
    setGradeId(nextGradeId)
    setArea('')
    setQuery('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Plan de estudios
        </Text>
        <Text variant="body">
          Configura las asignaturas y la intensidad horaria semanal
          correspondiente a cada grado durante el año lectivo.
        </Text>
      </div>

      <StudyPlanSelector
        yearId={year.id}
        gradeId={grade.id}
        status={year.status}
        onYearChange={selectYear}
        onGradeChange={selectGrade}
      />

      <GradePlanSummary
        grade={grade}
        configuredHours={configuredWeeklyHours(planEntries)}
      />

      <GradeSubjectsSection
        year={year.year}
        gradeLabel={grade.label}
        areaNames={areaNames}
        area={area}
        query={query}
        rows={rows}
        planEntries={planEntries}
        onAreaChange={setArea}
        onQueryChange={setQuery}
      />
    </section>
  )
}
