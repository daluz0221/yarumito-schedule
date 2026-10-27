import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RemoveScheduleClassModal.module.css'

export type RemoveScheduleClassModalProps = {
  open: boolean
  onClose: () => void
  onConfirm: () => void
}

export function RemoveScheduleClassModal({
  open,
  onClose,
  onConfirm,
}: RemoveScheduleClassModalProps) {
  return (
    <Modal
      open={open}
      title="Quitar del horario"
      onClose={onClose}
      footer={
        <>
          <Button variant="muted" size="sm" onClick={onClose}>
            Cancelar
          </Button>
          <Button variant="primary" size="sm" onClick={onConfirm}>
            Quitar del horario
          </Button>
        </>
      }
    >
      <p className={styles.copy}>
        Esta clase dejará de estar programada en este bloque. La asignación académica se
        conservará y volverá a quedar pendiente de programación.
      </p>
    </Modal>
  )
}
