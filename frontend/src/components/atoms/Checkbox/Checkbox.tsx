import type { InputHTMLAttributes } from 'react'
import styles from './Checkbox.module.css'

export type CheckboxProps = Omit<
  InputHTMLAttributes<HTMLInputElement>,
  'type'
>

export function Checkbox({ className, ...props }: CheckboxProps) {
  const classes = [styles.checkbox, className ?? ''].filter(Boolean).join(' ')

  return <input type="checkbox" className={classes} {...props} />
}
