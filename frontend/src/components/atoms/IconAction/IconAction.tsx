import type { ReactNode } from 'react'
import { PencilIcon, TrashIcon } from '../icons'
import styles from './IconAction.module.css'

export type IconActionProps = {
  label: string
  tone?: 'edit' | 'danger'
  onClick: () => void
}

export function IconActions({ children }: { children: ReactNode }) {
  return <span className={styles.group}>{children}</span>
}

export function IconAction({ label, tone = 'edit', onClick }: IconActionProps) {
  const danger = tone === 'danger'

  return (
    <button
      type="button"
      className={danger ? `${styles.action} ${styles.danger}` : styles.action}
      onClick={onClick}
      aria-label={label}
      title={label}
    >
      {danger ? <TrashIcon /> : <PencilIcon />}
    </button>
  )
}
