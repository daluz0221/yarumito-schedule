import type { InputHTMLAttributes, ReactNode } from 'react'
import styles from './TextInput.module.css'

export type TextInputSize = 'md' | 'sm'

export type TextInputProps = InputHTMLAttributes<HTMLInputElement> & {
  rightSlot?: ReactNode
  inputSize?: TextInputSize
}

export function TextInput({
  className,
  rightSlot,
  inputSize = 'md',
  ...props
}: TextInputProps) {
  const classes = [
    styles.input,
    styles[inputSize],
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
