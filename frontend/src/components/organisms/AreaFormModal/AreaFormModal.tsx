import { useState, type FormEvent } from 'react'
import type { AreaResponse } from '../../../api/areas'
import {
  areaFormStatusOptions,
  areaYesNoOptions,
  isAreaCodeTaken,
} from '../../../content/areas'
import { Modal } from '../../atoms/Modal'
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
  onClose: () => void
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

export function AreaFormModal({
  open,
  mode,
  areas,
  area,
  onClose,
}: AreaFormModalProps) {
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' ? valuesFromArea(area) : emptyValues,
  )
  const [attempted, setAttempted] = useState(false)
  const formId = `area-${mode}-form`
  const codeTaken = isAreaCodeTaken(areas, values.codigo, area?.id)
  const missingChoice = !values.obligatoria || !values.soloMedia || !values.activa

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || codeTaken) {
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
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={mode === 'create' ? 'Guardar área' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <TextField
            id={`${formId}-name`}
            label="Nombre del área"
            name="nombre"
            required
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
          error={attempted && !values.activa ? requiredChoiceMessage : undefined}
          onChange={(activa) =>
            setValues((current) => ({
              ...current,
              activa: activa as AreaStatus,
            }))
          }
        />
        <p className={styles.hint}>
          “Solo para Media” indica si el área se utiliza exclusivamente en los
          grados de Media.
        </p>
      </form>
    </Modal>
  )
}
