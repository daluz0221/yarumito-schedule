import { publicationCopy, type PublicationCheck } from '../../../content/publication'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationChecks.module.css'

export type PublicationChecksProps = {
  checks: PublicationCheck[]
}

export function PublicationChecks({ checks }: PublicationChecksProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.checksTitle}
      </Text>
      <ul className={styles.list}>
        {checks.map((check) => (
          <li key={check.id} className={styles.item}>
            <span className={check.passed ? styles.pass : styles.fail} aria-hidden="true">
              {check.passed ? '✓' : '!'}
            </span>
            <span>{check.label}</span>
          </li>
        ))}
      </ul>
    </Card>
  )
}
