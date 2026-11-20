import type { ReactNode } from 'react'
import { ArchiveIcon, ClockIcon, PencilIcon, RestoreIcon, TrashIcon } from '../icons'
import styles from './IconAction.module.css'

export type IconActionProps = {
  label: string
  tone?: 'edit' | 'danger' | 'restore' | 'archive' | 'history'
  // Solo para tone="archive" y "history": indica que la vista está activa.
  pressed?: boolean
  disabled?: boolean
  onClick: () => void
}

export function IconActions({ children }: { children: ReactNode }) {
  return <span className={styles.group}>{children}</span>
}

const ICONS = {
  edit: PencilIcon,
  danger: TrashIcon,
  restore: RestoreIcon,
  archive: ArchiveIcon,
  history: ClockIcon,
} as const

export function IconAction({
  label,
  tone = 'edit',
  pressed,
  disabled = false,
  onClick,
}: IconActionProps) {
  const Icon = ICONS[tone]
  const classes = [styles.action]

  if (tone === 'danger') {
    classes.push(styles.danger)
  }

  if ((tone === 'archive' || tone === 'history') && pressed) {
    classes.push(styles.pressed)
  }

  return (
    <button
      type="button"
      className={classes.join(' ')}
      onClick={onClick}
      aria-label={label}
      aria-pressed={tone === 'archive' || tone === 'history' ? Boolean(pressed) : undefined}
      title={label}
      disabled={disabled}
    >
      <Icon />
    </button>
  )
}
