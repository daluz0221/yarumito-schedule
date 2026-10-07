import { useState } from 'react'
import { IconButton } from '../../atoms/IconButton'
import { EyeIcon, EyeOffIcon } from '../../atoms/icons'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../FormField'
import type { TextInputProps } from '../../atoms/TextInput'

export type PasswordFieldProps = Omit<
  TextInputProps,
  'type' | 'rightSlot' | 'id'
> & {
  id: string
  label: string
}

export function PasswordField({
  id,
  label,
  ...inputProps
}: PasswordFieldProps) {
  const [visible, setVisible] = useState(false)

  return (
    <FormField id={id} label={label}>
      <TextInput
        id={id}
        type={visible ? 'text' : 'password'}
        autoComplete="current-password"
        rightSlot={
          <IconButton
            label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            onClick={() => setVisible((current) => !current)}
          >
            {visible ? <EyeOffIcon /> : <EyeIcon />}
          </IconButton>
        }
        {...inputProps}
      />
    </FormField>
  )
}
