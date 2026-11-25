import {
  publicationCopy,
  type PublicationWarningItem,
} from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './PublishWarningsModal.module.css'

export type PublishWarningsModalProps = {
  open: boolean
  warnings: PublicationWarningItem[]
  onClose: () => void
  onReview: () => void
  onConfirm?: () => void
  confirming?: boolean
}

export function PublishWarningsModal({
  open,
  warnings,
  onClose,
  onReview,
  onConfirm,
  confirming = false,
}: PublishWarningsModalProps) {
  const count = warnings.length

  return (
    <Modal
      open={open}
      title={publicationCopy.warningsTitle}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          {onConfirm ? (
            <Button variant="primary" size="sm" onClick={onConfirm} disabled={confirming}>
              {confirming ? publicationCopy.publishing : publicationCopy.warningsConfirm}
            </Button>
          ) : null}
          <Button variant={onConfirm ? 'outline' : 'primary'} size="sm" onClick={onReview} disabled={confirming}>
            {publicationCopy.warningsReview}
          </Button>
        </div>
      }
    >
      <div className={styles.body}>
        <div className={styles.pills}>
          <Badge tone="warning">
            {count} {count === 1 ? 'ADVERTENCIA' : 'ADVERTENCIAS'}
          </Badge>
        </div>
        <p className={styles.lead}>{publicationCopy.warningsLead(count)}</p>
        <div className={styles.panel}>
          <ul className={styles.list}>
            {warnings.map((item) => (
              <li key={item.id}>{item.title}</li>
            ))}
          </ul>
          <p className={styles.note}>{publicationCopy.warningsDisclaimer}</p>
        </div>
      </div>
    </Modal>
  )
}
