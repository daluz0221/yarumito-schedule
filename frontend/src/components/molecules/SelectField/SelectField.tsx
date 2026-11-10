import { Select, type SelectOption } from '../../atoms/Select'
import { FormField } from '../FormField'

export type SelectFieldProps = {
  id: string
  label: string
  name?: string
  value: string
  options: SelectOption[]
  required?: boolean
  hint?: string
  error?: string
  disabled?: boolean
  onChange: (value: string) => void
}

export function SelectField({
  id,
  label,
  name,
  value,
  options,
  required = false,
  hint,
  error,
  disabled = false,
  onChange,
}: SelectFieldProps) {
  return (
    <FormField id={id} label={label} required={required} hint={hint} error={error}>
      <Select
        id={id}
        name={name}
        value={value}
        options={options}
        selectSize="sm"
        required={required}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${id}-error` : undefined}
        onChange={(event) => onChange(event.target.value)}
      />
    </FormField>
  )
}
