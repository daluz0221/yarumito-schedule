import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './ProcessStatusCard.module.css'

export type ProcessStatusCardProps = {
  year: string
  status: string
}

export function ProcessStatusCard({ year, status }: ProcessStatusCardProps) {
  return (
    <Card className={styles.card}>
      <Text variant="sectionTitle">Estado del proceso</Text>
      <div className={styles.meta}>
        <Text variant="body">Año lectivo: {year}</Text>
        <Badge>{status}</Badge>
      </div>
      <Button variant="muted" size="sm" fullWidth disabled>
        Próximamente
      </Button>
    </Card>
  )
}
