import { Button } from '../../atoms/Button'

export type ModalActionsProps = {
  cancelLabel?: string
  confirmLabel?: string
  confirmType?: 'button' | 'submit'
  formId?: string
  disabled?: boolean
  onCancel: () => void
  onConfirm?: () => void
}

export function ModalActions({
  cancelLabel = 'Cancelar',
  confirmLabel,
  confirmType = 'button',
  formId,
  disabled = false,
  onCancel,
  onConfirm,
}: ModalActionsProps) {
  return (
    <>
      <Button variant="muted" size="sm" disabled={disabled} onClick={onCancel}>
        {cancelLabel}
      </Button>
      {confirmLabel ? (
        <Button
          type={confirmType}
          form={formId}
          variant="primary"
          size="sm"
          disabled={disabled}
          onClick={onConfirm}
        >
          {confirmLabel}
        </Button>
      ) : null}
    </>
  )
}
