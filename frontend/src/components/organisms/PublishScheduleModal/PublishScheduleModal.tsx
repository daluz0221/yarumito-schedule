import { publicationCopy, type PublicationView } from '../../../content/publication'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './PublishScheduleModal.module.css'

export type PublishScheduleModalProps = {
  open: boolean
  view: PublicationView
  onClose: () => void
  onConfirm: () => void
}

export function PublishScheduleModal({
  open,
  view,
  onClose,
  onConfirm,
}: PublishScheduleModalProps) {
  return (
    <Modal
      open={open}
      title={publicationCopy.confirmTitle}
      description={publicationCopy.confirmLead(view.version, view.scheduleName, view.campusFull)}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          <Button variant="muted" size="sm" onClick={onClose}>
            {publicationCopy.confirmCancel}
          </Button>
          <Button variant="primary" size="sm" onClick={onConfirm}>
            {publicationCopy.confirmAction}
          </Button>
        </div>
      }
    >
      <ul className={styles.effects}>
        {publicationCopy.confirmEffects.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </Modal>
  )
}
