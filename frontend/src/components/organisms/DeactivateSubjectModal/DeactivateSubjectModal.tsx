import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type DeactivateSubjectModalProps = {
  open: boolean
  subjectName?: string
  onClose: () => void
}

export function DeactivateSubjectModal({
  open,
  subjectName,
  onClose,
}: DeactivateSubjectModalProps) {
  return (
    <Modal
      open={open}
      title="Inactivar asignatura"
      description={`¿Deseas inactivar la asignatura ${subjectName ?? ''}?`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Inactivar asignatura"
          onCancel={onClose}
          onConfirm={onClose}
        />
      }
    >
      <Callout>
        <p>
          La asignatura dejará de estar disponible para nuevas configuraciones
          académicas. Sus relaciones históricas con Plan de Estudios, Asignación
          Académica e Idoneidad se conservarán.
        </p>
      </Callout>
    </Modal>
  )
}
