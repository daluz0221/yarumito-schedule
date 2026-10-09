import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type RemovePlanSubjectModalProps = {
  open: boolean
  subjectName: string
  gradeLabel: string
  year: number
  onClose: () => void
}

export function RemovePlanSubjectModal({
  open,
  subjectName,
  gradeLabel,
  year,
  onClose,
}: RemovePlanSubjectModalProps) {
  return (
    <Modal
      open={open}
      title="Quitar asignatura del plan"
      description={`¿Quitar ${subjectName} del plan de ${gradeLabel} del año ${year}?`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Quitar del plan"
          onCancel={onClose}
          onConfirm={onClose}
        />
      }
    >
      <Callout>
        <p>
          La asignatura continuará existiendo en Áreas y asignaturas (M-07). Solo se
          quitará su relación con este plan.
        </p>
      </Callout>
    </Modal>
  )
}
