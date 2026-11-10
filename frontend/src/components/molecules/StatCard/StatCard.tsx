import type { StatWash } from '../../../content/dashboard'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './StatCard.module.css'

export type StatCardTone = 'default' | 'danger' | 'success' | 'warning'

const washClass: Record<StatWash, string> = {
  total: styles.washTotal,
  active: styles.washActive,
  license: styles.washLicense,
  retired: styles.washRetired,
  danger: styles.washDanger,
  warning: styles.washLicense,
  success: styles.washActive,
  neutral: styles.washRetired,
}

export type StatCardProps = {
  value: string
  label: string
  tone?: StatCardTone
  wash?: StatWash
}

export function StatCard({ value, label, tone = 'default', wash }: StatCardProps) {
  const figureClass = [tone === 'default' ? '' : styles[tone], wash ? styles.figure : '']
    .filter(Boolean)
    .join(' ')

  return (
    <Card
      as="article"
      padding="sm"
      className={[styles.card, wash ? washClass[wash] : ''].filter(Boolean).join(' ')}
    >
      <Text variant="stat" className={figureClass || undefined}>
        {value}
      </Text>
      <Text variant="caption">{label}</Text>
    </Card>
  )
}
