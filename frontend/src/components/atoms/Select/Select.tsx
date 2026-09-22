import type { SelectHTMLAttributes } from 'react'
import styles from './Select.module.css'

export type SelectOption = {
  value: string
  label: string
}

export type SelectProps = Omit<
  SelectHTMLAttributes<HTMLSelectElement>,
  'children'
> & {
  options: SelectOption[]
  selectSize?: 'md' | 'sm'
}

export function Select({
  options,
  className,
  selectSize = 'md',
  ...props
}: SelectProps) {
  const classes = [styles.select, styles[selectSize], className ?? '']
    .filter(Boolean)
    .join(' ')

  return (
    <select className={classes} {...props}>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  )
}
