import { registerStatusOptions, type TeacherStatus } from '../../../content/teachers'
import type { SelectOption } from '../../atoms/Select'
import { Text } from '../../atoms/Text'
import { FormSection } from '../../molecules/FormSection'
import { SelectField } from '../../molecules/SelectField'
import styles from './TeacherStatusSection.module.css'

export type TeacherStatusSectionProps = {
  value: TeacherStatus
  options?: SelectOption[]
  hint?: string
  error?: string
  disabled?: boolean
  onChange: (value: TeacherStatus) => void
}

export function TeacherStatusSection({
  value,
  options = registerStatusOptions,
  hint,
  error,
  disabled = false,
  onChange,
}: TeacherStatusSectionProps) {
  return (
    <FormSection
      title="Estado"
      description="Estado administrativo del docente: activo, en licencia o retirado."
    >
      <SelectField
        id="estado"
        name="estado"
        label="Estado"
        required
        value={value}
        options={options}
        hint={hint}
        error={error}
        disabled={disabled}
        onChange={(next) => onChange(next as TeacherStatus)}
      />
      <div className={styles.hint}>
        <Text variant="body">
          Determina si el docente se encuentra activo, en licencia o retirado
          dentro de los procesos académicos del sistema.
        </Text>
      </div>
    </FormSection>
  )
}
