import type { ReactNode } from 'react'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './ModuleCard.module.css'

export type ModuleCardProps = {
  icon: ReactNode
  title: string
  description: string
  available?: boolean
  highlighted?: boolean
}

export function ModuleCard({
  icon,
  title,
  description,
  available = true,
  highlighted = false,
}: ModuleCardProps) {
  return (
    <Card as="article" highlighted={highlighted} className={styles.card}>
      <span className={styles.icon}>{icon}</span>
      <div className={styles.copy}>
        <Text variant="nav" as="h3">
          {title}
        </Text>
        <Text variant="body">{description}</Text>
      </div>
      <Button
        variant={available ? 'outline' : 'muted'}
        size="sm"
        fullWidth
        disabled={!available}
      >
        {available ? 'Abrir módulo' : 'Próximamente'}
      </Button>
    </Card>
  )
}
