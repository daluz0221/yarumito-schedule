import type { LabelHTMLAttributes, ReactNode } from 'react'
import styles from './Label.module.css'

export type LabelProps = LabelHTMLAttributes<HTMLLabelElement> & {
  variant?: 'default' | 'inline'
  children: ReactNode
}

export function Label({
  variant = 'default',
  className,
  children,
  ...props
}: LabelProps) {
  const classes = [styles.label, styles[variant], className ?? '']
    .filter(Boolean)
    .join(' ')

  return (
    <label className={classes} {...props}>
      {children}
    </label>
  )
}
