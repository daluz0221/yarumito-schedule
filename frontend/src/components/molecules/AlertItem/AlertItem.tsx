import { StatusDot, type StatusDotTone } from '../../atoms/StatusDot'
import { Text } from '../../atoms/Text'
import styles from './AlertItem.module.css'

export type AlertItemProps = {
  message: string
  tone: StatusDotTone
}

export function AlertItem({ message, tone }: AlertItemProps) {
  return (
    <li className={styles.item}>
      <StatusDot tone={tone} />
      <Text variant="body">{message}</Text>
    </li>
  )
}
