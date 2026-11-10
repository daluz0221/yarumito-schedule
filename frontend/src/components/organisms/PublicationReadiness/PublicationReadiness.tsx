import { publicationCopy, type PublicationView } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import styles from './PublicationReadiness.module.css'

export type PublicationReadinessProps = {
  view: PublicationView
  onSeeWarnings: () => void
  onSeeBlocked: () => void
}

export function PublicationReadiness({
  view,
  onSeeWarnings,
  onSeeBlocked,
}: PublicationReadinessProps) {
  const blocked = view.readiness === 'blocked'

  return (
    <div className={blocked ? styles.blocked : styles.clear} role="status">
      <strong>{blocked ? publicationCopy.blocking : publicationCopy.noBlocking}</strong>
      <div className={styles.actions}>
        {view.warnings > 0 ? (
          <Badge tone="warning">
            {view.warnings} {view.warnings === 1 ? 'advertencia pendiente' : 'advertencias pendientes'}
          </Badge>
        ) : null}
        {view.warnings > 0 ? (
          <Button variant="outline" size="sm" onClick={onSeeWarnings}>
            {publicationCopy.seeWarnings}
          </Button>
        ) : null}
        {blocked ? (
          <Button variant="outline" size="sm" onClick={onSeeBlocked}>
            {publicationCopy.seeValidations}
          </Button>
        ) : null}
      </div>
      <p>
        {blocked
          ? publicationCopy.blockingNote
          : view.warnings > 0
            ? publicationCopy.warningsNote
            : publicationCopy.readyNote}
      </p>
    </div>
  )
}
