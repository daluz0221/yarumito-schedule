import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './StatCard.module.css'

export type StatCardTone = 'default' | 'danger' | 'success' | 'warning'

export type StatCardProps = {
  value: string
  label: string
  tone?: StatCardTone
}

export function StatCard({ value, label, tone = 'default' }: StatCardProps) {
  return (
    <Card as="article" padding="sm" className={styles.card}>
      <Text variant="stat" className={tone === 'default' ? undefined : styles[tone]}>
        {value}
      </Text>
      <Text variant="caption">{label}</Text>
    </Card>
  )
}
