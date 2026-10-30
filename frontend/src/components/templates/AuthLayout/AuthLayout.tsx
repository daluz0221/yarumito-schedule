import type { ReactNode } from 'react'
import styles from './AuthLayout.module.css'

export type AuthLayoutProps = {
  children: ReactNode
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <main className={styles.layout}>
      <div className={styles.content}>{children}</div>
    </main>
  )
}
