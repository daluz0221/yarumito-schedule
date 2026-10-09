import type { AreaResponse } from '../../../api/areas'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type DeactivateAreaModalProps = {
  open: boolean
  area?: AreaResponse
  onClose: () => void
}

export function DeactivateAreaModal({
  open,
  area,
  onClose,
}: DeactivateAreaModalProps) {
  return (
    <Modal
      open={open}
      title="Inactivar área"
      description={`¿Deseas inactivar el área ${area?.nombre ?? ''}?`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel="Inactivar área"
          onCancel={onClose}
          onConfirm={onClose}
        />
      }
    >
      <Callout>
        <p>
          El área dejará de estar disponible para nuevas configuraciones
          académicas, pero su información histórica se conservará. Sus
          asignaturas no se eliminarán automáticamente.
        </p>
      </Callout>
    </Modal>
  )
}
