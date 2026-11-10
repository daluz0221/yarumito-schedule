import { publicationCopy } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './PublishBlockedModal.module.css'

export type PublishBlockedModalProps = {
  open: boolean
  errors: number
  onClose: () => void
  onReview: () => void
}

export function PublishBlockedModal({
  open,
  errors,
  onClose,
  onReview,
}: PublishBlockedModalProps) {
  return (
    <Modal
      open={open}
      title={publicationCopy.blockedTitle}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          <Button variant="primary" size="sm" onClick={onReview}>
            {publicationCopy.blockedAction}
          </Button>
        </div>
      }
    >
      <div className={styles.body}>
        <div className={styles.pills}>
          <Badge tone="danger">
            {errors === 1 ? 'ERROR pendiente' : `${errors} ERRORES pendientes`}
          </Badge>
        </div>
        <p className={styles.lead}>{publicationCopy.blockedLead}</p>
        <p className={styles.hint}>{publicationCopy.blockedHint}</p>
      </div>
    </Modal>
  )
}
