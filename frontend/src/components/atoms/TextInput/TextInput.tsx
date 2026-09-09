import type { InputHTMLAttributes, ReactNode } from 'react'
import styles from './TextInput.module.css'

export type TextInputProps = InputHTMLAttributes<HTMLInputElement> & {
  rightSlot?: ReactNode
}

export function TextInput({
  className,
  rightSlot,
  ...props
}: TextInputProps) {
  const classes = [
    styles.input,
    rightSlot ? styles.hasSlot : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={styles.wrapper}>
      <input className={classes} {...props} />
      {rightSlot ? <div className={styles.slot}>{rightSlot}</div> : null}
    </div>
  )
}
