import type { AlertItemData } from '../../../content/dashboard'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { AlertItem } from '../AlertItem'
import styles from './AlertsCard.module.css'

export type AlertsCardProps = {
  items: AlertItemData[]
}

export function AlertsCard({ items }: AlertsCardProps) {
  return (
    <Card className={styles.card}>
      <Text variant="sectionTitle">Alertas</Text>
      <ul className={styles.list}>
        {items.map((item) => (
          <AlertItem key={item.id} message={item.message} tone={item.tone} />
        ))}
      </ul>
    </Card>
  )
}
