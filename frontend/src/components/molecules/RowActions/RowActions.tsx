import type { ReactNode } from 'react'
import styles from './RowActions.module.css'

export type RowActionsProps = {
  children: ReactNode
}

export function RowActions({ children }: RowActionsProps) {
  return <div className={styles.actions}>{children}</div>
}
