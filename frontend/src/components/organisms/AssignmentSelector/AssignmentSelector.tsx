import type { AcademicYearStatus } from '../../../content/academicYear'
import { Card } from '../../atoms/Card'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './AssignmentSelector.module.css'

export type AssignmentSelectorProps = {
  yearId: string
  groupId: string
  gradeLabel: string
  yearStatus: AcademicYearStatus
  yearOptions: { value: string; label: string }[]
  groupOptions: { value: string; label: string }[]
  yearsDisabled?: boolean
  groupsDisabled?: boolean
  onYearChange: (yearId: string) => void
  onGroupChange: (groupId: string) => void
}

export function AssignmentSelector({
  yearId,
  groupId,
  gradeLabel,
  yearStatus,
  yearOptions,
  groupOptions,
  yearsDisabled = false,
  groupsDisabled = false,
  onYearChange,
  onGroupChange,
}: AssignmentSelectorProps) {
  const resolvedGroups =
    groupOptions.length > 0 ? groupOptions : [{ value: '', label: 'Sin grupos activos' }]
  const emptyGroups = resolvedGroups.every((option) => option.value === '')

  return (
    <Card className={styles.card}>
      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <SelectField
          id="assignment-year"
          label="Año lectivo"
          name="year"
          value={yearId}
          options={yearOptions}
          disabled={yearsDisabled}
          onChange={onYearChange}
        />
        <SelectField
          id="assignment-group"
          label="Grupo"
          name="group"
          value={emptyGroups ? '' : groupId}
          options={resolvedGroups}
          disabled={groupsDisabled || emptyGroups || yearStatus === 'CERRADO'}
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
