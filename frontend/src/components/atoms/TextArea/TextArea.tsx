import type { TextareaHTMLAttributes } from 'react'
import styles from './TextArea.module.css'

export type TextAreaProps = TextareaHTMLAttributes<HTMLTextAreaElement>

export function TextArea({ className, ...props }: TextAreaProps) {
  const classes = [styles.input, className ?? ''].filter(Boolean).join(' ')

  return <textarea className={classes} {...props} />
}
