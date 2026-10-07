import { useEffect, useState, type FormEvent } from 'react'
import { PENDING_BACKEND_LABEL } from '../../../content/teachers'
import {
  emptyCrearIdoneidadForm,
  idoneidadToForm,
  suitabilityTypeOptions,
  type CrearIdoneidadForm,
  type Idoneidad,
} from '../../../content/teacherProfile'
import type { SelectOption } from '../../atoms/Select'
import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './SuitabilityFormModal.module.css'

export type SuitabilityFormModalProps = {
  open: boolean
  mode: 'add' | 'edit'
  item?: Idoneidad
  areaOptions: SelectOption[]
  submitting?: boolean
  error?: string
  onClose: () => void
  onSubmit: (values: CrearIdoneidadForm) => void
}

const pendingOptions: SelectOption[] = [
  { value: '', label: PENDING_BACKEND_LABEL },
]

export function SuitabilityFormModal({
  open,
  mode,
  item,
  areaOptions,
  submitting = false,
  error = '',
  onClose,
  onSubmit,
}: SuitabilityFormModalProps) {
  const [values, setValues] = useState<CrearIdoneidadForm>(emptyCrearIdoneidadForm)
  const [localError, setLocalError] = useState('')
  const isEdit = mode === 'edit'
  const isExceptional = values.tipo === 'EXCEPCIONAL'

  useEffect(() => {
    if (!open) {
      return
    }

    setLocalError('')
    setValues(
      mode === 'edit' && item ? idoneidadToForm(item) : emptyCrearIdoneidadForm,
    )
  }, [item, mode, open])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (submitting) {
      return
    }

    if (!isEdit && !values.areaId) {
      setLocalError('El área es obligatoria')
      return
    }

    if (!isEdit && !values.vigenteDesde) {
      setLocalError('La fecha de inicio de vigencia es obligatoria')
      return
    }

    if (isExceptional && !values.justificacion.trim()) {
      setLocalError('Una idoneidad excepcional requiere justificación')
      return
    }

    setLocalError('')
    onSubmit(values)
  }

  return (
    <Modal
      open={open}
      title={
        mode === 'add'
          ? 'Agregar idoneidad académica'
          : 'Editar idoneidad académica'
      }
      description={
        isEdit
          ? 'El PUT solo permite cambiar justificación y título de soporte.'
          : 'Define el área y el tipo de idoneidad. Asignatura y título quedan pendientes.'
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId="suitability-form"
          confirmLabel={
            submitting
              ? 'Guardando...'
              : mode === 'add'
                ? 'Guardar idoneidad'
                : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting}
          onCancel={onClose}
        />
      }
    >
      <form
        id="suitability-form"
        className={styles.form}
        onSubmit={handleSubmit}
      >
        <SelectField
          id="suitability-area"
          label="Área"
          required
          value={values.areaId}
          options={
            areaOptions.length > 0
              ? [{ value: '', label: 'Seleccione un área' }, ...areaOptions]
              : pendingOptions
          }
          disabled={isEdit || areaOptions.length === 0}
          hint={areaOptions.length === 0 ? PENDING_BACKEND_LABEL : undefined}
          onChange={(areaId) =>
            setValues((current) => ({ ...current, areaId, asignaturaId: '' }))
          }
        />
        <SelectField
          id="suitability-subject"
          label="Asignatura"
          value=""
          options={pendingOptions}
          hint={PENDING_BACKEND_LABEL}
          disabled
          onChange={() => undefined}
        />
        <SelectField
          id="suitability-type"
          label="Tipo de idoneidad"
          required
          value={values.tipo}
          options={suitabilityTypeOptions}
          disabled={isEdit}
          onChange={(tipo) =>
            setValues((current) => ({
              ...current,
              tipo: tipo as CrearIdoneidadForm['tipo'],
              justificacion:
                tipo === 'EXCEPCIONAL' ? current.justificacion : '',
            }))
          }
        />
        <SelectField
          id="suitability-title"
          label="Título profesional de soporte"
          value=""
          options={pendingOptions}
          hint={PENDING_BACKEND_LABEL}
          disabled
          onChange={() => undefined}
        />
        <div className={styles.dates}>
          <TextField
            id="suitability-from"
            label="Vigente desde"
            type="date"
            required
            value={values.vigenteDesde}
            disabled={isEdit}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                vigenteDesde: event.target.value,
              }))
            }
          />
          <TextField
            id="suitability-to"
            label="Vigente hasta"
            type="date"
            value={values.vigenteHasta}
            disabled={isEdit}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                vigenteHasta: event.target.value,
              }))
            }
          />
        </div>
        <TextField
          id="suitability-justification"
          label="Justificación"
          required={isExceptional}
          placeholder="Obligatoria solo para idoneidad excepcional"
          value={values.justificacion}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              justificacion: event.target.value,
            }))
          }
        />
        {error || localError ? (
          <p className={styles.error} role="alert">
            {error || localError}
          </p>
        ) : null}
        {isEdit ? (
          <Text variant="caption" className={styles.note}>
            Área, tipo y fechas no se pueden cambiar en el PUT. El título de
            soporte queda pendiente hasta que exista API de títulos.
          </Text>
        ) : null}
      </form>
    </Modal>
  )
}
