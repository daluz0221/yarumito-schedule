import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './StatCard.module.css'

export type StatCardProps = {
  value: string
  label: string
}

export function StatCard({ value, label }: StatCardProps) {
  return (
    <Card as="article" padding="sm" className={styles.card}>
      <Text variant="stat">{value}</Text>
      <Text variant="caption">{label}</Text>
    </Card>
  )
}
