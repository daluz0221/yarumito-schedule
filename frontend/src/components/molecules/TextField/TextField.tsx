import { TextInput, type TextInputProps } from '../../atoms/TextInput'
import { FormField } from '../FormField'

export type TextFieldProps = Omit<TextInputProps, 'id' | 'inputSize'> & {
  id: string
  label: string
  required?: boolean
  hint?: string
  error?: string
}

export function TextField({
  id,
  label,
  required = false,
  hint,
  error,
  ...inputProps
}: TextFieldProps) {
  return (
    <FormField id={id} label={label} required={required} hint={hint} error={error}>
      <TextInput
        id={id}
        inputSize="sm"
        required={required}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${id}-error` : undefined}
        {...inputProps}
      />
    </FormField>
  )
}
