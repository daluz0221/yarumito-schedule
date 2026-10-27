import type { AcademicYear } from '../../../content/academicYear'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type CloseAcademicYearModalProps = {
  open: boolean
  year?: AcademicYear
  onClose: () => void
}

export function CloseAcademicYearModal({
  open,
  year,
  onClose,
}: CloseAcademicYearModalProps) {
  return (
    <Modal
      open={open}
      title="Cerrar año lectivo"
      description={`¿Deseas cerrar el año lectivo ${year?.year ?? ''}?`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Cerrar año"
          onCancel={onClose}
          onConfirm={onClose}
        />
      }
    >
      <Callout>
        <p>
          La información se conservará para consulta histórica y no se
          utilizará en nuevas configuraciones académicas.
        </p>
      </Callout>
    </Modal>
  )
}
