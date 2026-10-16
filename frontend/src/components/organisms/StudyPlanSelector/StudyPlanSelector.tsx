import type { OpcionGrado } from '../../../api/grados'
import {
  labelYearStatus,
  type AcademicYearStatus,
} from '../../../content/academicYear'
import { Card } from '../../atoms/Card'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './StudyPlanSelector.module.css'

export type StudyPlanSelectorProps = {
  yearId: string
  yearOptions: { value: string; label: string }[]
  yearsSummary: string
  yearDisabled?: boolean
  gradeId: string
  gradeOptions: OpcionGrado[]
  gradeError?: string
  gradeDisabled?: boolean
  status: AcademicYearStatus | null
  onYearChange: (yearId: string) => void
  onGradeChange: (gradeId: string) => void
}

export function StudyPlanSelector({
  yearId,
  yearOptions,
  yearsSummary,
  yearDisabled = false,
  gradeId,
  gradeOptions,
  gradeError,
  gradeDisabled = false,
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
          value={yearDisabled ? '' : yearId}
          options={yearOptions}
          disabled={yearDisabled}
          onChange={onYearChange}
        />
        <SelectField
          id="plan-grade"
          label="Grado"
          name="grade"
          value={gradeDisabled ? '' : gradeId}
          options={gradeOptions}
          disabled={gradeDisabled}
          error={gradeError || undefined}
          onChange={onGradeChange}
        />
        <FormField id="plan-status" label="Estado">
          <div className={styles.status} id="plan-status">
            <span className={status === 'ACTIVO' ? styles.active : styles.muted}>
              {status ? labelYearStatus(status) : '—'}
            </span>
          </div>
        </FormField>
      </form>
      <p className={styles.hint}>Años disponibles: {yearsSummary}</p>
    </Card>
  )
}
