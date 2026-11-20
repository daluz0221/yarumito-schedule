import { ConfirmActionModal } from '../ConfirmActionModal'

export type DeleteTitleModalProps = {
  open: boolean
  titleName?: string
  submitting?: boolean
  error?: string
  onClose: () => void
  onConfirm: () => void
}

// H-04: retira (baja lógica) un título profesional; no lo elimina.
export function DeleteTitleModal({
  open,
  titleName,
  submitting,
  error,
  onClose,
  onConfirm,
}: DeleteTitleModalProps) {
  return (
    <ConfirmActionModal
      open={open}
      title="Retirar título profesional"
      description={`¿Deseas retirar ${titleName || 'este título profesional'}?`}
      note="El título se conserva como histórico y podrá reactivarse. No se puede retirar mientras respalde una idoneidad vigente."
      confirmLabel="Retirar título"
      busyLabel="Retirando..."
      submitting={submitting}
      error={error}
      onClose={onClose}
      onConfirm={onConfirm}
    />
  )
}
