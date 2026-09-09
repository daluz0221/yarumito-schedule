import type { ReactNode } from 'react'
import styles from './Badge.module.css'

export type BadgeTone = 'neutral' | 'warning'

export type BadgeProps = {
  children: ReactNode
  tone?: BadgeTone
}

export function Badge({ children, tone = 'neutral' }: BadgeProps) {
  const classes = [styles.badge, styles[tone]].join(' ')

  return <span className={classes}>{children}</span>
}
