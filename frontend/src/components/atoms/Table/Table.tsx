import type { ReactNode } from 'react'
import styles from './Table.module.css'

export type TableProps = {
  children: ReactNode
  minWidth?: number
  className?: string
}

export function Table({ children, minWidth = 760, className }: TableProps) {
  const classes = [styles.scroll, className ?? ''].filter(Boolean).join(' ')

  return (
    <div className={classes}>
      <table className={styles.table} style={{ minWidth }}>
        {children}
      </table>
    </div>
  )
}
