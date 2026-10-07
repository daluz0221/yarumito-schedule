import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router-dom'
import styles from './TextLink.module.css'

export type TextLinkProps = {
  children: ReactNode
  to?: string
  onClick?: () => void
  disabled?: boolean
  className?: string
  type?: ButtonHTMLAttributes<HTMLButtonElement>['type']
}

export function TextLink({
  children,
  to,
  onClick,
  disabled = false,
  className,
  type = 'button',
}: TextLinkProps) {
  const classes = [styles.link, className ?? ''].filter(Boolean).join(' ')

  if (to && !disabled) {
    return (
      <Link to={to} className={classes} onClick={onClick}>
        {children}
      </Link>
    )
  }

  return (
    <button
      type={type}
      className={classes}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  )
}
