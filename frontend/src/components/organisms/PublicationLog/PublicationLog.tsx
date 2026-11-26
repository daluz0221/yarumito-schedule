import { publicationCopy } from '../../../content/publication'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationLog.module.css'

export type PublicationLogProps = {
  message: string
  note: string
  actionLabel: string
  busy?: boolean
  disabled?: boolean
  onAction?: () => void
}

export function PublicationLog({
  message,
  note,
  actionLabel,
  busy = false,
  disabled = false,
  onAction,
}: PublicationLogProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.logTitle}
      </Text>

      <p className={styles.empty}>{message}</p>

      <div className={styles.footer}>
        <p className={styles.note}>{note}</p>
        <Button variant="primary" size="sm" disabled={disabled || busy} onClick={onAction}>
          {actionLabel}
        </Button>
      </div>
    </Card>
  )
}
