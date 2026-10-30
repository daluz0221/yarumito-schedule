import { useEffect, useState, type FormEvent } from 'react'
import {
  formatVigencia,
  labelTipoIdoneidad,
  type Idoneidad,
} from '../../../content/teacherProfile'
import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import { Callout } from '../../molecules/Callout'
import { ModalActions } from '../../molecules/ModalActions'
import { TextField } from '../../molecules/TextField'
import styles from './EndSuitabilityModal.module.css'

export type EndSuitabilityModalProps = {
  open: boolean
  item?: Idoneidad
  areaNombre?: string
  submitting?: boolean
  error?: string
  onClose: () => void
  onSubmit: (vigenteHasta: string) => void
}

export function EndSuitabilityModal({
  open,
  item,
  areaNombre,
  submitting = false,
  error = '',
  onClose,
  onSubmit,
}: EndSuitabilityModalProps) {
  const [vigenteHasta, setVigenteHasta] = useState('')
  const [localError, setLocalError] = useState('')

  useEffect(() => {
    if (open) {
      setVigenteHasta('')
      setLocalError('')
    }
  }, [open])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (submitting) {
      return
    }

    if (!vigenteHasta) {
      setLocalError('La fecha de finalización es obligatoria')
      return
    }

    setLocalError('')
    onSubmit(vigenteHasta)
  }

  return (
    <Modal
      open={open}
      title="Finalizar vigencia de idoneidad"
      onClose={onClose}
      footer={
        <ModalActions
          formId="end-suitability-form"
          confirmLabel={submitting ? 'Guardando...' : 'Finalizar vigencia'}
          confirmType="submit"
          disabled={submitting}
          onCancel={onClose}
        />
      }
    >
      <form
        id="end-suitability-form"
        className={styles.form}
        onSubmit={handleSubmit}
      >
        <Callout>
          <p>Área: {areaNombre || '—'}</p>
          <p>Tipo: {item ? labelTipoIdoneidad(item.tipo) : '—'}</p>
          <p>Vigencia: {item ? formatVigencia(item) : '—'}</p>
        </Callout>
        <TextField
          id="end-date"
          name="vigenteHasta"
          label="Fecha de finalización"
          type="date"
          required
          value={vigenteHasta}
          onChange={(event) => setVigenteHasta(event.target.value)}
        />
        {error || localError ? (
          <p className={styles.error} role="alert">
            {error || localError}
          </p>
        ) : null}
        <Text variant="body">
          La idoneidad dejará de estar vigente a partir de la fecha indicada. El
          registro se conservará como histórico.
        </Text>
      </form>
    </Modal>
  )
}
