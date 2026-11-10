import { validationReadyCopy } from '../../../content/validations'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './ValidationReadyModal.module.css'

export type ValidationReadyModalProps = {
  open: boolean
  hasWarnings: boolean
  onClose: () => void
  onOpenSchedule: () => void
  onPreparePublish: () => void
}

export function ValidationReadyModal({
  open,
  hasWarnings,
  onClose,
  onOpenSchedule,
  onPreparePublish,
}: ValidationReadyModalProps) {
  return (
    <Modal
      open={open}
      title={validationReadyCopy.title}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          <Button variant="muted" size="sm" onClick={onClose}>
            Cerrar
          </Button>
          <Button variant="outline" size="sm" onClick={onOpenSchedule}>
            Volver al horario
          </Button>
          <Button variant="primary" size="sm" onClick={onPreparePublish}>
            Preparar publicación
          </Button>
        </div>
      }
    >
      <div className={styles.body}>
        <div className={styles.pills}>
          <Badge tone="success">{validationReadyCopy.badge}</Badge>
        </div>
        <p className={styles.message}>{validationReadyCopy.message}</p>
        {hasWarnings ? <p className={styles.note}>{validationReadyCopy.warnings}</p> : null}
      </div>
    </Modal>
  )
}
