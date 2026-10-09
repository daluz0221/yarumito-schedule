import { Modal } from '../../atoms/Modal'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './RemoveAssignmentModal.module.css'

export type RemoveAssignmentModalProps = {
  open: boolean
  variant?: 'curricular' | 'activity'
  year?: number
  groupCode?: string
  subjectName: string
  teacherName: string
  hours: number
  onClose: () => void
  onConfirm: () => void
}

export function RemoveAssignmentModal({
  open,
  variant = 'curricular',
  year,
  groupCode,
  subjectName,
  teacherName,
  hours,
  onClose,
  onConfirm,
}: RemoveAssignmentModalProps) {
  const activity = variant === 'activity'

  return (
    <Modal
      open={open}
      title={activity ? 'Quitar asignación de actividad' : 'Quitar asignación'}
      description={
        activity
          ? `${teacherName} · ${subjectName} · ${hours} h`
          : `${year} · Grupo ${groupCode} · ${subjectName} · ${teacherName} · ${hours} h`
      }
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Quitar asignación"
          onCancel={onClose}
          onConfirm={onConfirm}
        />
      }
    >
      <div className={styles.copy}>
        <p>
          {activity
            ? 'Esta asignación dejará de formar parte de la carga de actividades del docente.'
            : 'Esta asignación dejará de formar parte de la carga académica del grupo.'}
        </p>
        {activity ? (
          <p>Se conserva el docente y la actividad institucional.</p>
        ) : null}
      </div>
    </Modal>
  )
}
