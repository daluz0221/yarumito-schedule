import { useState } from 'react'
import { ApiError, retirarPlanEstudios } from '../../../api'
import type { PlanEstudiosResponse } from '../../../api/planesEstudio'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'

export type RemovePlanSubjectModalProps = {
  open: boolean
  subjectName: string
  gradeLabel: string
  year: number
  planId: string
  onClose: () => void
  onRemoved?: (plan: PlanEstudiosResponse) => void
}

export function RemovePlanSubjectModal({
  open,
  subjectName,
  gradeLabel,
  year,
  planId,
  onClose,
  onRemoved,
}: RemovePlanSubjectModalProps) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const confirm = async () => {
    setBusy(true)
    setError('')

    try {
      const plan = await retirarPlanEstudios(planId)
      onRemoved?.(plan)
      onClose()
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo quitar la asignatura del plan.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal
      open={open}
      title="Quitar asignatura del plan"
      description={`¿Quitar ${subjectName} del plan de ${gradeLabel} del año ${year}?`}
      onClose={onClose}
      footer={
        <ModalActions
          confirmLabel={busy ? 'Quitando...' : 'Quitar del plan'}
          onCancel={onClose}
          onConfirm={confirm}
        />
      }
    >
      <Callout>
        <p>
          La asignatura continuará existiendo en Áreas y asignaturas (M-07). Solo se
          quitará su relación con este plan.
        </p>
      </Callout>
      {error ? <p role="alert">{error}</p> : null}
    </Modal>
  )
}
