import { useState } from 'react'
import { ApiError, cambiarEstadoArea } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './DeactivateAreaModal.module.css'

export type DeactivateAreaModalProps = {
  open: boolean
  area?: AreaResponse
  onClose: () => void
  onUpdated?: (area: AreaResponse) => void
}

export function DeactivateAreaModal({
  open,
  area,
  onClose,
  onUpdated,
}: DeactivateAreaModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const activating = area?.activa === false

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  const handleConfirm = async () => {
    if (submitting) {
      return
    }

    if (!area?.id) {
      setSubmitError('No se encontró el área a actualizar.')
      return
    }

    setSubmitError('')
    setSubmitting(true)

    try {
      const updated = await cambiarEstadoArea(area.id, activating)
      onUpdated?.(updated)
      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : activating
            ? 'No se pudo activar el área. Inténtalo de nuevo.'
            : 'No se pudo inactivar el área. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      title={activating ? 'Activar área' : 'Inactivar área'}
      description={
        activating
          ? `¿Deseas activar el área ${area?.nombre ?? ''}?`
          : `¿Deseas inactivar el área ${area?.nombre ?? ''}?`
      }
      onClose={handleClose}
      footer={
        <ModalActions
          confirmLabel={
            submitting
              ? 'Guardando...'
              : activating
                ? 'Activar área'
                : 'Inactivar área'
          }
          disabled={submitting}
          onCancel={handleClose}
          onConfirm={handleConfirm}
        />
      }
    >
      <Callout>
        {activating ? (
          <p>
            El área volverá a estar disponible para nuevas configuraciones
            académicas. Sus asignaturas se conservan.
          </p>
        ) : (
          <p>
            El área dejará de estar disponible para nuevas configuraciones
            académicas, pero su información histórica se conservará. Sus
            asignaturas no se eliminarán automáticamente.
          </p>
        )}
      </Callout>
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
    </Modal>
  )
}
