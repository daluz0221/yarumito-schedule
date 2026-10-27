import type { PublicationView } from '../../../content/publication'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationMetrics.module.css'

export type PublicationMetricsProps = {
  view: PublicationView
}

export function PublicationMetrics({ view }: PublicationMetricsProps) {
  return (
    <div className={styles.row} aria-label="Indicadores de publicación">
      <Metric label="Estado" value={view.status} tone={view.status === 'PUBLICADO' ? 'success' : 'warning'} word />
      <Metric label="Errores pendientes" value={String(view.errors)} tone={view.errors > 0 ? 'danger' : 'success'} />
      <Metric
        label="Advertencias"
        value={String(view.warnings)}
        tone={view.warnings > 0 ? 'warning' : 'success'}
      />
      <Metric label="Versión" value={String(view.version)} />
      <Metric
        label="Publicación"
        value={view.publicationLabel}
        tone={view.publicationLabel === 'Publicado' ? 'success' : 'default'}
        word
      />
    </div>
  )
}

function Metric({
  label,
  value,
  tone = 'default',
  word = false,
}: {
  label: string
  value: string
  tone?: 'default' | 'success' | 'warning' | 'danger'
  word?: boolean
}) {
  return (
    <Card as="article" padding="sm" className={styles.card}>
      <Text variant="stat" className={[word ? styles.word : '', tone === 'default' ? '' : styles[tone]].filter(Boolean).join(' ')}>
        {value}
      </Text>
      <Text variant="caption">{label}</Text>
    </Card>
  )
}
