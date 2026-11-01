import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type DeleteTitleModalProps = {
  open: boolean
  onClose: () => void
}

export function DeleteTitleModal({ open, onClose }: DeleteTitleModalProps) {
  return (
    <Modal
      open={open}
      title="Eliminar título profesional"
      description="¿Deseas eliminar este título profesional?"
      onClose={onClose}
      footer={<ModalActions onCancel={onClose} />}
    >
      <Callout>
        <Text variant="body">
          Esta acción solo estará disponible cuando el título no esté siendo
          utilizado como soporte de una idoneidad.
        </Text>
      </Callout>
    </Modal>
  )
}
