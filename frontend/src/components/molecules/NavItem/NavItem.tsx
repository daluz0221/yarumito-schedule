import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { Text } from '../../atoms/Text'
import styles from './NavItem.module.css'

export type NavItemProps = {
  icon: ReactNode
  label: string
  to?: string
  end?: boolean
  disabled?: boolean
  indented?: boolean
  onClick?: () => void
  onNavigate?: () => void
}

export function NavItem({
  icon,
  label,
  to,
  end = false,
  disabled = false,
  indented = false,
  onClick,
  onNavigate,
}: NavItemProps) {
  const classes = [
    styles.item,
    indented ? styles.indented : '',
    disabled ? styles.disabled : '',
  ]
    .filter(Boolean)
    .join(' ')

  const content = (
    <>
      <span className={styles.icon}>{icon}</span>
      <Text variant="nav">{label}</Text>
    </>
  )

  if (disabled) {
    return (
      <span className={classes} aria-disabled="true">
        {content}
      </span>
    )
  }

  if (onClick && !to) {
    return (
      <button
        type="button"
        className={`${classes} ${styles.action}`}
        onClick={() => {
          onClick()
          onNavigate?.()
        }}
      >
        {content}
      </button>
    )
  }

  if (!to) {
    return (
      <span className={classes} aria-disabled="true">
        {content}
      </span>
    )
  }

  return (
    <NavLink
      to={to}
      end={end}
      onClick={onNavigate}
      className={({ isActive }) =>
        [classes, isActive ? styles.active : ''].filter(Boolean).join(' ')
      }
    >
      {content}
    </NavLink>
  )
}
