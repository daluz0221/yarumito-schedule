import { useState } from 'react'
import { actualizarAnioEscolar, ApiError } from '../../../api'
import type { AnioEscolarResponse } from '../../../api/aniosEscolares'
import type { AcademicYear } from '../../../content/academicYear'
import { Modal } from '../../atoms/Modal'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import styles from './CloseAcademicYearModal.module.css'

export type CloseAcademicYearModalProps = {
  open: boolean
  year?: AcademicYear
  onClose: () => void
  onClosed?: (anio: AnioEscolarResponse) => void
}

function fechaIso(value: string) {
  const local = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(value ?? '')

  if (local) {
    return `${local[3]}-${local[2]}-${local[1]}`
  }

  return value
}

export function CloseAcademicYearModal({
  open,
  year,
  onClose,
  onClosed,
}: CloseAcademicYearModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const aviso =
    year && year.status !== 'ACTIVO'
      ? `Transición de estado no permitida: ${year.status} -> CERRADO`
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
        estado: 'CERRADO',
        esActual: false,
      })
      onClosed?.(updated)
      setError('')
      onClose()
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo cerrar el año escolar. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      title="Cerrar año lectivo"
      description={`¿Deseas cerrar el año lectivo ${year?.year ?? ''}?`}
      onClose={handleClose}
      footer={
        <ModalActions
          confirmLabel={submitting ? 'Guardando...' : 'Cerrar año'}
          disabled={submitting}
          onCancel={handleClose}
          onConfirm={confirmar}
        />
      }
    >
      <Callout>
        <p>
          La información se conservará para consulta histórica y no se
          utilizará en nuevas configuraciones académicas.
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
