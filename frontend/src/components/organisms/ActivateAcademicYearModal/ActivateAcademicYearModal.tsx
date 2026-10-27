import type { AcademicYear } from '../../../content/academicYear'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type ActivateAcademicYearModalProps = {
  open: boolean
  year?: AcademicYear
  onClose: () => void
}

export function ActivateAcademicYearModal({
  open,
  year,
  onClose,
}: ActivateAcademicYearModalProps) {
  return (
    <Modal
      open={open}
      title={`Activar año lectivo ${year?.year ?? ''}`}
      description="Al activar este año lectivo, será utilizado como período académico actual del sistema."
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Activar año"
          onCancel={onClose}
          onConfirm={onClose}
        />
      }
    >
      <Callout>
        <p>
          Los grupos configurados como activos estarán disponibles para la
          planificación académica.
        </p>
      </Callout>
    </Modal>
  )
}
