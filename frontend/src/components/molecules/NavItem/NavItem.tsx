import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { Text } from '../../atoms/Text'
import styles from './NavItem.module.css'

export type NavItemProps = {
  icon: ReactNode
  label: string
  to?: string
  disabled?: boolean
  indented?: boolean
  onNavigate?: () => void
}

export function NavItem({
  icon,
  label,
  to,
  disabled = false,
  indented = false,
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

  if (!to || disabled) {
    return (
      <span className={classes} aria-disabled="true">
        {content}
      </span>
    )
  }

  return (
    <NavLink
      to={to}
      onClick={onNavigate}
      className={({ isActive }) =>
        [classes, isActive ? styles.active : ''].filter(Boolean).join(' ')
      }
    >
      {content}
    </NavLink>
  )
}
