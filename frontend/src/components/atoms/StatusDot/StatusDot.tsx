import styles from './StatusDot.module.css'

export type StatusDotTone = 'warning' | 'alert' | 'success'

export type StatusDotProps = {
  tone: StatusDotTone
  label?: string
}

export function StatusDot({ tone, label }: StatusDotProps) {
  return <span className={`${styles.dot} ${styles[tone]}`} aria-label={label} />
}
