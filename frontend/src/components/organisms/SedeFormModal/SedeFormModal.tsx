import { useState, type FormEvent } from 'react'
import { actualizarSede, ApiError, registrarSede } from '../../../api'
import type { SedeResponse } from '../../../api/sedes'
import { Checkbox } from '../../atoms/Checkbox'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { TextField } from '../../molecules/TextField'
import styles from './SedeFormModal.module.css'

export type SedeFormMode = 'create' | 'edit'

export type SedeFormModalProps = {
  open: boolean
  mode: SedeFormMode
  sedes: SedeResponse[]
  sede?: SedeResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (sede: SedeResponse) => void
  onUpdated?: (sede: SedeResponse) => void
}

type FormValues = {
  nombre: string
  codigo: string
  direccion: string
  esPrincipal: boolean
}

const emptyValues: FormValues = {
  nombre: '',
  codigo: '',
  direccion: '',
  esPrincipal: false,
}

function valuesFromSede(sede?: SedeResponse): FormValues {
  if (!sede) {
    return emptyValues
  }

  return {
    nombre: sede.nombre,
    codigo: sede.codigo,
    direccion: sede.direccion ?? '',
    esPrincipal: sede.esPrincipal,
  }
}

function codigoOcupado(sedes: SedeResponse[], codigo: string, sedeId?: string) {
  const normalizado = codigo.trim().toUpperCase()

  if (!normalizado) {
    return false
  }

  return sedes.some(
    (sede) => sede.id !== sedeId && sede.codigo.trim().toUpperCase() === normalizado,
  )
}

type SedeFormFieldsProps = {
  mode: SedeFormMode
  sedes: SedeResponse[]
  sede?: SedeResponse
  onClose: () => void
  onCreated?: (sede: SedeResponse) => void
  onUpdated?: (sede: SedeResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function SedeFormFields({
  mode,
  sedes,
  sede,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: SedeFormFieldsProps) {
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' ? valuesFromSede(sede) : emptyValues,
  )
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `sede-${mode}-form`
  const creating = mode === 'create'
  const codeTaken = codigoOcupado(sedes, values.codigo, sede?.id)

  const setBusy = (next: boolean) => {
    setSubmitting(next)
    onSubmittingChange(next)
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitError('')

    if (submitting || !values.nombre.trim() || !values.codigo.trim() || codeTaken) {
      return
    }

    if (!creating && !sede?.id) {
      setSubmitError('No se encontró la sede a actualizar.')
      return
    }

    const payload = {
      nombre: values.nombre.trim(),
      codigo: values.codigo.trim(),
      direccion: values.direccion.trim() || null,
      esPrincipal: values.esPrincipal,
    }

    setBusy(true)

    try {
      if (creating) {
        const created = await registrarSede(payload)
        onCreated?.(created)
      } else if (sede) {
        const updated = await actualizarSede(sede.id, payload)
        onUpdated?.(updated)
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar la sede. Inténtalo de nuevo.'
            : 'No se pudo actualizar la sede. Inténtalo de nuevo.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.grid}>
        <TextField
          id={`${formId}-name`}
          label="Nombre de la sede"
          name="nombre"
          required
          maxLength={100}
          disabled={submitting}
          placeholder="Ingresa el nombre de la sede"
          value={values.nombre}
          onChange={(event) =>
            setValues((current) => ({ ...current, nombre: event.target.value }))
          }
        />
        <TextField
          id={`${formId}-code`}
          label="Código"
          name="codigo"
          required
          maxLength={20}
          disabled={submitting}
          placeholder="Ingresa el código"
          value={values.codigo}
          error={codeTaken ? 'Ya existe una sede con ese código' : undefined}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              codigo: event.target.value.toUpperCase(),
            }))
          }
        />
      </div>
      <TextField
        id={`${formId}-address`}
        label="Dirección"
        name="direccion"
        maxLength={100}
        disabled={submitting}
        placeholder="Ingresa la dirección"
        value={values.direccion}
        onChange={(event) =>
          setValues((current) => ({ ...current, direccion: event.target.value }))
        }
      />
      <label className={styles.check} htmlFor={`${formId}-principal`}>
        <Checkbox
          id={`${formId}-principal`}
          name="esPrincipal"
          checked={values.esPrincipal}
          disabled={submitting}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              esPrincipal: event.target.checked,
            }))
          }
        />
        Esta es la sede principal
      </label>
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
      <p className={styles.hint}>
        El nombre y el código se guardan en mayúsculas. Solo puede existir una
        sede principal.
      </p>
    </form>
  )
}

export function SedeFormModal({
  open,
  mode,
  sedes,
  sede,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: SedeFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(sede))
  const formId = `sede-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title={creating ? 'Nueva sede' : 'Editar sede'}
      description={
        creating
          ? 'Registra una sede de la institución.'
          : 'Actualiza la información de la sede seleccionada.'
      }
      onClose={handleClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={
            loadError
              ? undefined
              : loading
                ? 'Cargando...'
                : submitting
                  ? 'Guardando...'
                  : creating
                    ? 'Guardar sede'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading || Boolean(loadError)}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando sede...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <SedeFormFields
          key={mode === 'edit' ? sede?.id : 'create'}
          mode={mode}
          sedes={sedes}
          sede={sede}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando sede...</p>
      )}
    </Modal>
  )
}
