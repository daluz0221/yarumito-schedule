import { useState, type FormEvent } from 'react'
import { actualizarArea, ApiError, registrarArea } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import {
  areaFormStatusOptions,
  areaYesNoOptions,
  isAreaCodeTaken,
} from '../../../content/areas'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './AreaFormModal.module.css'

export type AreaFormMode = 'create' | 'edit'

export type AreaFormModalProps = {
  open: boolean
  mode: AreaFormMode
  areas: AreaResponse[]
  area?: AreaResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (area: AreaResponse) => void
  onUpdated?: (area: AreaResponse) => void
}

type YesNo = '' | 'si' | 'no'
type AreaStatus = '' | 'activa' | 'inactiva'

type FormValues = {
  nombre: string
  codigo: string
  obligatoria: YesNo
  soloMedia: YesNo
  activa: AreaStatus
}

const emptyValues: FormValues = {
  nombre: '',
  codigo: '',
  obligatoria: '',
  soloMedia: '',
  activa: '',
}

const duplicateCodeMessage = 'Ya existe un área con este código.'
const requiredChoiceMessage = 'Seleccione una opción.'

function yesNoValue(value?: boolean): YesNo {
  if (value === undefined) {
    return ''
  }

  return value ? 'si' : 'no'
}

function statusValue(activa?: boolean): AreaStatus {
  if (activa === undefined) {
    return ''
  }

  return activa ? 'activa' : 'inactiva'
}

function valuesFromArea(area?: AreaResponse): FormValues {
  if (!area) {
    return emptyValues
  }

  return {
    nombre: area.nombre,
    codigo: area.codigo,
    obligatoria: yesNoValue(area.obligatoria),
    soloMedia: yesNoValue(area.soloMedia),
    activa: statusValue(area.activa),
  }
}

type AreaFormFieldsProps = {
  mode: AreaFormMode
  areas: AreaResponse[]
  area?: AreaResponse
  onClose: () => void
  onCreated?: (area: AreaResponse) => void
  onUpdated?: (area: AreaResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function AreaFormFields({
  mode,
  areas,
  area,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: AreaFormFieldsProps) {
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' ? valuesFromArea(area) : emptyValues,
  )
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `area-${mode}-form`
  const codeTaken = isAreaCodeTaken(areas, values.codigo, area?.id)
  const missingChoice = !values.obligatoria || !values.soloMedia || !values.activa
  const creating = mode === 'create'

  const setBusy = (next: boolean) => {
    setSubmitting(next)
    onSubmittingChange(next)
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)
    setSubmitError('')

    if (submitting) {
      return
    }

    if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || codeTaken) {
      return
    }

    if (!creating && !area?.id) {
      setSubmitError('No se encontró el área a actualizar.')
      return
    }

    const payload = {
      nombre: values.nombre.trim(),
      codigo: values.codigo.trim(),
      obligatoria: values.obligatoria === 'si',
      soloMedia: values.soloMedia === 'si',
      activa: values.activa === 'activa',
    }

    setBusy(true)

    try {
      if (creating) {
        const created = await registrarArea(payload)
        onCreated?.(created)
      } else if (area) {
        const updated = await actualizarArea(area.id, payload)
        onUpdated?.(updated)
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar el área. Inténtalo de nuevo.'
            : 'No se pudo actualizar el área. Inténtalo de nuevo.',
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
          label="Nombre del área"
          name="nombre"
          required
          disabled={submitting}
          placeholder="Ingresa el nombre del área"
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
          disabled={submitting}
          placeholder="Ingresa el código"
          value={values.codigo}
          error={codeTaken ? duplicateCodeMessage : undefined}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              codigo: event.target.value.toUpperCase(),
            }))
          }
        />
        <SelectField
          id={`${formId}-required`}
          label="Área obligatoria"
          name="obligatoria"
          value={values.obligatoria}
          options={areaYesNoOptions}
          disabled={submitting}
          error={attempted && !values.obligatoria ? requiredChoiceMessage : undefined}
          onChange={(obligatoria) =>
            setValues((current) => ({
              ...current,
              obligatoria: obligatoria as YesNo,
            }))
          }
        />
        <SelectField
          id={`${formId}-media`}
          label="Solo para Media"
          name="soloMedia"
          value={values.soloMedia}
          options={areaYesNoOptions}
          disabled={submitting}
          error={attempted && !values.soloMedia ? requiredChoiceMessage : undefined}
          onChange={(soloMedia) =>
            setValues((current) => ({
              ...current,
              soloMedia: soloMedia as YesNo,
            }))
          }
        />
      </div>
      <SelectField
        id={`${formId}-status`}
        label="Estado"
        name="activa"
        value={values.activa}
        options={areaFormStatusOptions}
        disabled={submitting}
        error={attempted && !values.activa ? requiredChoiceMessage : undefined}
        onChange={(activa) =>
          setValues((current) => ({
            ...current,
            activa: activa as AreaStatus,
          }))
        }
      />
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
      <p className={styles.hint}>
        “Solo para Media” indica si el área se utiliza exclusivamente en los
        grados de Media.
      </p>
    </form>
  )
}

export function AreaFormModal({
  open,
  mode,
  areas,
  area,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: AreaFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(area))
  const formId = `area-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Nueva área académica' : 'Editar área académica'}
      description={
        mode === 'create'
          ? 'Registra una nueva área para el catálogo académico institucional.'
          : 'Actualiza la información del área seleccionada.'
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
                    ? 'Guardar área'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando área...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <AreaFormFields
          key={mode === 'edit' ? area?.id : 'create'}
          mode={mode}
          areas={areas}
          area={area}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando área...</p>
      )}
    </Modal>
  )
}
