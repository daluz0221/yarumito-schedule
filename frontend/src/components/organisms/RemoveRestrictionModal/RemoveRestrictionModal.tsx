import { timeBandLabel, type WeekDay } from '../../../content/teacherRestrictions'
import { Modal } from '../../atoms/Modal'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './RemoveRestrictionModal.module.css'

export type RemoveRestrictionModalProps = {
  open: boolean
  teacherName: string
  year: number
  day: WeekDay
  bandId: string
  onConfirm: () => void
  onClose: () => void
}

export function RemoveRestrictionModal({
  open,
  teacherName,
  year,
  day,
  bandId,
  onConfirm,
  onClose,
}: RemoveRestrictionModalProps) {
  return (
    <Modal
      open={open}
      title="Quitar restricción"
      description={`${teacherName} · ${year} · ${day} · ${timeBandLabel(bandId)}`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Quitar restricción"
          onCancel={onClose}
          onConfirm={onConfirm}
        />
      }
    >
      <p className={styles.copy}>
        Esta franja volverá a considerarse disponible para la planificación del docente.
      </p>
    </Modal>
  )
}
