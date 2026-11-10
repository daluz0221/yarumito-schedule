import { academicYearOptions, type AcademicYearStatus } from '../../../content/academicYear'
import { academicYears } from '../../../content/academicYear'
import { Card } from '../../atoms/Card'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './AssignmentSelector.module.css'

export type AssignmentSelectorProps = {
  yearId: string
  groupId: string
  gradeLabel: string
  yearStatus: AcademicYearStatus
  groupOptions: { value: string; label: string }[]
  onYearChange: (yearId: string) => void
  onGroupChange: (groupId: string) => void
}

export function AssignmentSelector({
  yearId,
  groupId,
  gradeLabel,
  yearStatus,
  groupOptions,
  onYearChange,
  onGroupChange,
}: AssignmentSelectorProps) {
  const emptyGroups = groupOptions.length === 0

  return (
    <Card className={styles.card}>
      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <SelectField
          id="assignment-year"
          label="Año lectivo"
          name="year"
          value={yearId}
          options={academicYearOptions(academicYears)}
          onChange={onYearChange}
        />
        <SelectField
          id="assignment-group"
          label="Grupo"
          name="group"
          value={emptyGroups ? '' : groupId}
          options={
            emptyGroups
              ? [{ value: '', label: 'Sin grupos activos' }]
              : groupOptions
          }
          disabled={emptyGroups || yearStatus === 'CERRADO'}
          onChange={onGroupChange}
        />
        <FormField id="assignment-grade" label="Grado">
          <div className={styles.derived} id="assignment-grade">
            {gradeLabel ? `${gradeLabel} · Derivado del grupo` : 'Derivado del grupo'}
          </div>
        </FormField>
      </form>
    </Card>
  )
}
