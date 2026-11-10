import { Checkbox } from '../../atoms/Checkbox'
import { Label } from '../../atoms/Label'
import styles from './CheckboxField.module.css'

export type CheckboxFieldProps = {
  id: string
  label: string
  checked: boolean
  onChange: (checked: boolean) => void
  name?: string
}

export function CheckboxField({
  id,
  label,
  checked,
  onChange,
  name,
}: CheckboxFieldProps) {
  return (
    <div className={styles.row}>
      <Checkbox
        id={id}
        name={name}
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      <Label htmlFor={id} variant="inline">
        {label}
      </Label>
    </div>
  )
}
