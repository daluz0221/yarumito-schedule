import {
  gradeFilterOptions,
  groupStatusFilterOptions,
} from '../../../content/academicYear'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './SchoolGroupsFilters.module.css'

export type SchoolGroupsFiltersProps = {
  grade: string
  status: string
  query: string
  onGradeChange: (value: string) => void
  onStatusChange: (value: string) => void
  onQueryChange: (value: string) => void
}

export function SchoolGroupsFilters({
  grade,
  status,
  query,
  onGradeChange,
  onStatusChange,
  onQueryChange,
}: SchoolGroupsFiltersProps) {
  return (
    <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
      <SelectField
        id="group-grade"
        label="Grado"
        name="grade"
        value={grade}
        options={gradeFilterOptions}
        onChange={onGradeChange}
      />
      <SelectField
        id="group-status"
        label="Estado"
        name="status"
        value={status}
        options={groupStatusFilterOptions}
        onChange={onStatusChange}
      />
      <FormField id="group-search" label="Buscar grupo">
        <TextInput
          id="group-search"
          name="query"
          inputSize="sm"
          placeholder="Buscar por código..."
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
        />
      </FormField>
    </form>
  )
}
