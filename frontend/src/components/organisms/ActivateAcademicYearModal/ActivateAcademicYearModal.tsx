import { useState } from 'react'
import { actualizarAnioEscolar, ApiError } from '../../../api'
import type { AnioEscolarResponse } from '../../../api/aniosEscolares'
import type { AcademicYear } from '../../../content/academicYear'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './ActivateAcademicYearModal.module.css'

export type ActivateAcademicYearModalProps = {
  open: boolean
  year?: AcademicYear
  otroActual?: boolean
  onClose: () => void
  onActivated?: (anio: AnioEscolarResponse) => void
}

function fechaIso(value: string) {
  const local = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(value ?? '')

  if (local) {
    return `${local[3]}-${local[2]}-${local[1]}`
  }

  return value
}

export function ActivateAcademicYearModal({
  open,
  year,
  otroActual = false,
  onClose,
  onActivated,
}: ActivateAcademicYearModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const aviso = otroActual
    ? 'Ya existe otro año escolar marcado como actual'
    : year && year.status !== 'PLANEACION'
      ? `Transición de estado no permitida: ${year.status} -> ACTIVO`
      : ''

  const handleClose = () => {
    if (submitting) {
      return
    }

    setError('')
    onClose()
  }

  const confirmar = async () => {
    if (!year || submitting || aviso) {
      return
    }

    setSubmitting(true)
    setError('')

    try {
      const updated = await actualizarAnioEscolar(year.id, {
        anio: year.year,
        fechaInicio: fechaIso(year.startDate),
        fechaFin: fechaIso(year.endDate),
        estado: 'ACTIVO',
        esActual: true,
      })
      onActivated?.(updated)
      setError('')
      onClose()
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo activar el año escolar. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      title={`Activar año lectivo ${year?.year ?? ''}`}
      description="Al activar este año lectivo, será utilizado como período académico actual del sistema."
      onClose={handleClose}
      footer={
        <ModalActions
          confirmLabel={submitting ? 'Guardando...' : 'Activar año'}
          disabled={submitting}
          onCancel={handleClose}
          onConfirm={confirmar}
        />
      }
    >
      <Callout>
        <p>
          Los grupos configurados como activos estarán disponibles para la
          planificación académica.
        </p>
      </Callout>
      {aviso || error ? (
        <p className={styles.error} role="alert">
          {aviso || error}
        </p>
      ) : null}
    </Modal>
  )
}
