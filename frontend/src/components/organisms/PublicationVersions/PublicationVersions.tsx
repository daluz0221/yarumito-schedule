import { publicationCopy, type PublicationView } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationVersions.module.css'

export type PublicationVersionsProps = {
  view: PublicationView
}

export function PublicationVersions({ view }: PublicationVersionsProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.versionsTitle}
      </Text>
      <div className={styles.grid}>
        <article className={styles.current}>
          <p className={styles.version}>{publicationCopy.currentVersion}</p>
          <Badge tone={view.status === 'PUBLICADO' ? 'success' : 'warning'}>{view.status}</Badge>
          <Text variant="caption">{view.versionCaption}</Text>
        </article>
        <article className={styles.previous}>
          <p className={styles.version}>{publicationCopy.previousTitle}</p>
          <Text variant="caption">{publicationCopy.previousEmpty}</Text>
        </article>
      </div>
    </Card>
  )
}
