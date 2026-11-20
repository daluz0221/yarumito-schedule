import { publicationCopy } from '../../../content/publication'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationLog.module.css'

export type PublicationLogProps = {
  published: boolean
}

export function PublicationLog({ published }: PublicationLogProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.logTitle}
      </Text>

      <p className={styles.empty}>
        {published ? publicationCopy.logPublished : publicationCopy.logEmpty}
      </p>

      <div className={styles.footer}>
        <p className={styles.note}>{publicationCopy.logNote}</p>
        <Button variant="primary" size="sm" disabled>
          {published ? publicationCopy.published : publicationCopy.publish}
        </Button>
      </div>
    </Card>
  )
}
