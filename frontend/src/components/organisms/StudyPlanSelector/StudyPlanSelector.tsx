import {
  labelYearStatus,
  type AcademicYearStatus,
} from '../../../content/academicYear'
import {
  formatAvailableYears,
  studyPlanGradeOptions,
  studyPlanYearOptions,
} from '../../../content/studyPlan'
import { Card } from '../../atoms/Card'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './StudyPlanSelector.module.css'

export type StudyPlanSelectorProps = {
  yearId: string
  gradeId: string
  status: AcademicYearStatus
  onYearChange: (yearId: string) => void
  onGradeChange: (gradeId: string) => void
}

export function StudyPlanSelector({
  yearId,
  gradeId,
  status,
  onYearChange,
  onGradeChange,
}: StudyPlanSelectorProps) {
  return (
    <Card className={styles.card}>
      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <SelectField
          id="plan-year"
          label="Año lectivo"
          name="year"
          value={yearId}
          options={studyPlanYearOptions()}
          onChange={onYearChange}
        />
        <SelectField
          id="plan-grade"
          label="Grado"
          name="grade"
          value={gradeId}
          options={studyPlanGradeOptions}
          onChange={onGradeChange}
        />
        <FormField id="plan-status" label="Estado">
          <div className={styles.status} id="plan-status">
            <span className={status === 'ACTIVO' ? styles.active : styles.muted}>
              {labelYearStatus(status)}
            </span>
          </div>
        </FormField>
      </form>
      <p className={styles.hint}>Años disponibles: {formatAvailableYears()}</p>
    </Card>
  )
}
