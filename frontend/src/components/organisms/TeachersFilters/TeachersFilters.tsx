import {
  teacherContractOptions,
  teacherStatusOptions,
} from '../../../content/teachers'
import type { SelectOption } from '../../atoms/Select'
import { TextInput } from '../../atoms/TextInput'
import { TextLink } from '../../atoms/TextLink'
import { FormField } from '../../molecules/FormField'
import { SelectField } from '../../molecules/SelectField'
import styles from './TeachersFilters.module.css'

export type TeachersFiltersProps = {
  query: string
  area: string
  status: string
  contract: string
  areaOptions: SelectOption[]
  areasLoading?: boolean
  areasError?: string
  onQueryChange: (value: string) => void
  onAreaChange: (value: string) => void
  onStatusChange: (value: string) => void
  onContractChange: (value: string) => void
  onClear: () => void
}

export function TeachersFilters({
  query,
  area,
  status,
  contract,
  areaOptions,
  areasLoading = false,
  areasError,
  onQueryChange,
  onAreaChange,
  onStatusChange,
  onContractChange,
  onClear,
}: TeachersFiltersProps) {
  return (
    <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
      <FormField id="teacher-search" label="Buscar docente">
        <TextInput
          id="teacher-search"
          name="query"
          inputSize="sm"
          placeholder="Buscar por nombre, apellido o documento"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
        />
      </FormField>
      <SelectField
        id="teacher-area"
        label="Área"
        name="area"
        value={area}
        options={areaOptions}
        hint={areasError || (areasLoading ? 'Cargando áreas...' : undefined)}
        disabled={areasLoading || Boolean(areasError)}
        onChange={onAreaChange}
      />
      <SelectField
        id="teacher-status"
        label="Estado"
        name="status"
        value={status}
        options={teacherStatusOptions}
        onChange={onStatusChange}
      />
      <SelectField
        id="teacher-contract"
        label="Tipo de vinculación"
        name="contract"
        value={contract}
        options={teacherContractOptions}
        onChange={onContractChange}
      />
      <div className={styles.clear}>
        <TextLink onClick={onClear}>Limpiar filtros</TextLink>
      </div>
    </form>
  )
}
