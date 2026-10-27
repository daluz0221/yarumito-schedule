import { useState } from 'react'
import { ApiError, cambiarEstadoAsignatura } from '../../../api'
import type { AsignaturaResponse } from '../../../api/asignaturas'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './DeactivateSubjectModal.module.css'

export type DeactivateSubjectModalProps = {
  open: boolean
  subject?: AsignaturaResponse
  onClose: () => void
  onUpdated?: (subject: AsignaturaResponse) => void
}

export function DeactivateSubjectModal({
  open,
  subject,
  onClose,
  onUpdated,
}: DeactivateSubjectModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const activating = subject?.activa === false

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

    if (!subject?.id) {
      setSubmitError('No se encontró la asignatura a actualizar.')
      return
    }

    setSubmitError('')
    setSubmitting(true)

    try {
      const updated = await cambiarEstadoAsignatura(subject.id, activating)
      onUpdated?.(updated)
      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : activating
            ? 'No se pudo activar la asignatura. Inténtalo de nuevo.'
            : 'No se pudo inactivar la asignatura. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      title={activating ? 'Activar asignatura' : 'Inactivar asignatura'}
      description={
        activating
          ? `¿Deseas activar la asignatura ${subject?.nombre ?? ''}?`
          : `¿Deseas inactivar la asignatura ${subject?.nombre ?? ''}?`
      }
      onClose={handleClose}
      footer={
        <ModalActions
          confirmLabel={
            submitting
              ? 'Guardando...'
              : activating
                ? 'Activar asignatura'
                : 'Inactivar asignatura'
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
            La asignatura volverá a estar disponible para nuevas configuraciones
            académicas. Sus relaciones históricas se conservan.
          </p>
        ) : (
          <p>
            La asignatura dejará de estar disponible para nuevas configuraciones
            académicas. Sus relaciones históricas con Plan de Estudios, Asignación
            Académica e Idoneidad se conservarán.
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
