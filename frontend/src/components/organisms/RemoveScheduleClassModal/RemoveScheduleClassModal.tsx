import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RemoveScheduleClassModal.module.css'

export type RemoveScheduleClassModalProps = {
  open: boolean
  submitting?: boolean
  error?: string
  onClose: () => void
  onConfirm: () => void
}

export function RemoveScheduleClassModal({
  open,
  submitting = false,
  error = '',
  onClose,
  onConfirm,
}: RemoveScheduleClassModalProps) {
  return (
    <Modal
      open={open}
      title="Quitar del horario"
      onClose={submitting ? () => undefined : onClose}
      footer={
        <>
          <Button variant="muted" size="sm" onClick={onClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button variant="primary" size="sm" onClick={onConfirm} disabled={submitting}>
            {submitting ? 'Quitando…' : 'Quitar del horario'}
          </Button>
        </>
      }
    >
      <p className={styles.copy}>
        Esta clase dejará de estar programada en este bloque. La asignación académica se
        conservará y volverá a quedar pendiente de programación.
      </p>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
    </Modal>
  )
}
