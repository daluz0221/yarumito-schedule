import type { ReactNode } from 'react'
import { Label } from '../../atoms/Label'
import styles from './FormField.module.css'

export type FormFieldProps = {
  id: string
  label: string
  children: ReactNode
}

export function FormField({ id, label, children }: FormFieldProps) {
  return (
    <div className={styles.field}>
      <Label htmlFor={id}>{label}</Label>
      {children}
    </div>
  )
}
