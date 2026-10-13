import { registerStatusOptions, type TeacherStatus } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { FormSection } from '../../molecules/FormSection'
import { SelectField } from '../../molecules/SelectField'
import styles from './TeacherStatusSection.module.css'

export type TeacherStatusSectionProps = {
  value: TeacherStatus
  hint?: string
  disabled?: boolean
  onChange: (value: TeacherStatus) => void
}

export function TeacherStatusSection({
  value,
  hint,
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
        options={registerStatusOptions}
        hint={hint}
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
