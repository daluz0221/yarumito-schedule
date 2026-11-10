import type { ButtonHTMLAttributes, ReactNode } from 'react'
import styles from './IconButton.module.css'

export type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  label: string
  children: ReactNode
}

export function IconButton({
  label,
  className,
  type = 'button',
  children,
  ...props
}: IconButtonProps) {
  const classes = [styles.button, className ?? ''].filter(Boolean).join(' ')

  return (
    <button type={type} className={classes} aria-label={label} {...props}>
      {children}
    </button>
  )
}
