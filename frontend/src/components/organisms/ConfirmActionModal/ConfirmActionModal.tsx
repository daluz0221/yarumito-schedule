import type { ReactNode } from 'react'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './ConfirmActionModal.module.css'

export type ConfirmActionModalProps = {
  open: boolean
  title: string
  description: string
  note?: ReactNode
  confirmLabel: string
  busyLabel?: string
  submitting?: boolean
  error?: string
  onClose: () => void
  onConfirm: () => void
}

// Confirmación genérica para bajas lógicas y reactivaciones (H-04).
export function ConfirmActionModal({
  open,
  title,
  description,
  note,
  confirmLabel,
  busyLabel = 'Guardando...',
  submitting = false,
  error = '',
  onClose,
  onConfirm,
}: ConfirmActionModalProps) {
  return (
    <Modal
      open={open}
      title={title}
      description={description}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel={submitting ? busyLabel : confirmLabel}
          disabled={submitting}
          onCancel={onClose}
          onConfirm={onConfirm}
        />
      }
    >
      <div className={styles.body}>
        {note ? <Callout>{note}</Callout> : null}
        {error ? (
          <p className={styles.error} role="alert">
            {error}
          </p>
        ) : null}
      </div>
    </Modal>
  )
}
