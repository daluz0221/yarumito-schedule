import {
  kindFilterOptions,
  severityFilterOptions,
  statusFilterOptions,
  type ValidationFilters as ValidationFilterValues,
} from '../../../content/validations'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import styles from './ValidationFilters.module.css'

export type ValidationFiltersProps = {
  values: ValidationFilterValues
  groupOptions: { value: string; label: string }[]
  kindOptions?: { value: string; label: string }[]
  validating: boolean
  onChange: (patch: Partial<ValidationFilterValues>) => void
  onRevalidate: () => void
}

export function ValidationFilters({
  values,
  groupOptions,
  kindOptions = kindFilterOptions,
  validating,
  onChange,
  onRevalidate,
}: ValidationFiltersProps) {
  return (
    <Card className={styles.card}>
      <SectionHeader
        title="Filtros"
        description="Acota los conflictos por severidad, estado o tipo. El grupo elige el horario que se consulta."
      />

      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <SelectField
          id="validation-severity"
          label="Severidad"
          name="severity"
          value={values.severity}
          options={severityFilterOptions}
          onChange={(severity) => onChange({ severity })}
        />
        <SelectField
          id="validation-status"
          label="Estado"
          name="status"
          value={values.status}
          options={statusFilterOptions}
          onChange={(status) => onChange({ status })}
        />
        <SelectField
          id="validation-kind"
          label="Tipo de conflicto"
          name="kind"
          value={values.kind}
          options={kindOptions}
          onChange={(kind) => onChange({ kind })}
        />
        <SelectField
          id="validation-group"
          label="Grupo"
          name="group"
          value={groupOptions.some((option) => option.value === values.groupId) ? values.groupId : ''}
          options={groupOptions}
          onChange={(groupId) => onChange({ groupId })}
        />
        <div className={styles.action}>
          <Button variant="primary" size="sm" disabled={validating} onClick={onRevalidate}>
            {validating ? 'Validando…' : 'Volver a validar'}
          </Button>
        </div>
      </form>
    </Card>
  )
}
