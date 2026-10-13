import type { ReactNode } from 'react'
import styles from './Callout.module.css'

export type CalloutProps = {
  children: ReactNode
}

export function Callout({ children }: CalloutProps) {
  return <div className={styles.callout}>{children}</div>
}
