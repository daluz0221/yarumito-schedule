import { StatusDot, type StatusDotTone } from '../../atoms/StatusDot'
import styles from './StatusLabel.module.css'

export type StatusLabelProps = {
  label: string
  tone: StatusDotTone
}

export function StatusLabel({ label, tone }: StatusLabelProps) {
  return (
    <span className={styles.status}>
      <StatusDot tone={tone} />
      {label}
    </span>
  )
}
