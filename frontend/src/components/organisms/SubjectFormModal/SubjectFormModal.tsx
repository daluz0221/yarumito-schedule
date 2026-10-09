import { useState, type FormEvent } from 'react'
import type { AreaResponse } from '../../../api/areas'
import {
  areaSelectOptions,
  subjectClassroomOptions,
  subjectColorOptions,
  subjectFormHint,
  subjectStatusOptions,
  subjectYesNoOptions,
} from '../../../content/subjects'
import { Modal } from '../../atoms/Modal'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './SubjectFormModal.module.css'

export type SubjectFormMode = 'create' | 'edit'

type YesNo = '' | 'si' | 'no'
type SubjectStatus = '' | 'activa' | 'inactiva'

export type SubjectFormSource = {
  id: string
  areaId: string
  nombre: string
  codigo: string
  abreviatura?: string
  colorUi?: string | null
  exigeIdoneidadEstricta?: boolean
  esMediaTecnica?: boolean
  requiereDocenteExclusivo?: boolean
  tipoAulaRequerida?: string | null
  maxClasesConsecutivas?: number
  activa?: boolean
}

export type SubjectFormModalProps = {
  open: boolean
  mode: SubjectFormMode
  areas: AreaResponse[]
  selectedAreaId?: string
  subject?: SubjectFormSource
  onClose: () => void
}

type FormValues = {
  areaId: string
  nombre: string
  codigo: string
  abreviatura: string
  color: string
  idoneidad: YesNo
  media: YesNo
  exclusivo: YesNo
  aula: string
  maxConsecutivas: string
  activa: SubjectStatus
}

const requiredChoiceMessage = 'Seleccione una opción.'

function yesNoValue(value?: boolean): YesNo {
  if (value === undefined) {
    return ''
  }

  return value ? 'si' : 'no'
}

function statusValue(activa?: boolean): SubjectStatus {
  if (activa === undefined) {
    return ''
  }

  return activa ? 'activa' : 'inactiva'
}

function createValues(areaId: string): FormValues {
  return {
    areaId,
    nombre: '',
    codigo: '',
    abreviatura: '',
    color: '',
    idoneidad: '',
    media: '',
    exclusivo: '',
    aula: '',
    maxConsecutivas: '2',
    activa: '',
  }
}

function valuesFromSubject(subject: SubjectFormSource): FormValues {
  return {
    areaId: subject.areaId,
    nombre: subject.nombre,
    codigo: subject.codigo,
    abreviatura: subject.abreviatura ?? '',
    color: subject.colorUi ?? '',
    idoneidad: yesNoValue(subject.exigeIdoneidadEstricta),
    media: yesNoValue(subject.esMediaTecnica),
    exclusivo: yesNoValue(subject.requiereDocenteExclusivo),
    aula: subject.tipoAulaRequerida ?? '',
    maxConsecutivas: String(subject.maxClasesConsecutivas ?? 2),
    activa: statusValue(subject.activa),
  }
}

export function SubjectFormModal({
  open,
  mode,
  areas,
  selectedAreaId = '',
  subject,
  onClose,
}: SubjectFormModalProps) {
  const initialAreaId = areas.some((area) => area.id === selectedAreaId)
    ? selectedAreaId
    : ''
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && subject ? valuesFromSubject(subject) : createValues(initialAreaId),
  )
  const [attempted, setAttempted] = useState(false)
  const formId = `subject-${mode}-form`
  const maxValue = Number(values.maxConsecutivas)
  const maxInvalid = !values.maxConsecutivas || !Number.isInteger(maxValue) || maxValue < 1
  const missingChoice =
    !values.areaId ||
    !values.idoneidad ||
    !values.media ||
    !values.exclusivo ||
    !values.aula ||
    !values.activa

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || maxInvalid) {
      return
    }

    onClose()
  }

  const patch = (partial: Partial<FormValues>) => {
    setValues((current) => ({ ...current, ...partial }))
  }

  return (
    <Modal
      open={open}
      size="lg"
      title={mode === 'create' ? 'Nueva asignatura' : 'Editar asignatura'}
      description={
        mode === 'create'
          ? 'Agrega una asignatura al área seleccionada del catálogo académico.'
          : `Actualiza las propiedades académicas de ${subject?.nombre ?? values.nombre}.`
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={mode === 'create' ? 'Guardar asignatura' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <SelectField
            id={`${formId}-area`}
            label="Área"
            name="areaId"
            required
            value={values.areaId}
            options={areaSelectOptions(areas)}
            error={attempted && !values.areaId ? requiredChoiceMessage : undefined}
            onChange={(areaId) => patch({ areaId })}
          />
          <TextField
            id={`${formId}-name`}
            label="Nombre"
            name="nombre"
            required
            placeholder="Ingresa el nombre"
            value={values.nombre}
            onChange={(event) => patch({ nombre: event.target.value })}
          />
          <TextField
            id={`${formId}-code`}
            label="Código"
            name="codigo"
            required
            placeholder="Ingresa el código"
            value={values.codigo}
            onChange={(event) => patch({ codigo: event.target.value.toUpperCase() })}
          />
          <TextField
            id={`${formId}-abbreviation`}
            label="Abreviatura"
            name="abreviatura"
            placeholder="Ingresa la abreviatura"
            value={values.abreviatura}
            onChange={(event) => patch({ abreviatura: event.target.value.toUpperCase() })}
          />
          <SelectField
            id={`${formId}-color`}
            label="Color de identificación (opcional)"
            name="color"
            value={values.color}
            options={subjectColorOptions}
            onChange={(color) => patch({ color })}
          />
          <SelectField
            id={`${formId}-suitability`}
            label="Exige idoneidad estricta"
            name="idoneidad"
            value={values.idoneidad}
            options={subjectYesNoOptions}
            error={attempted && !values.idoneidad ? requiredChoiceMessage : undefined}
            onChange={(idoneidad) => patch({ idoneidad: idoneidad as YesNo })}
          />
          <SelectField
            id={`${formId}-media`}
            label="Asignatura de Media Técnica"
            name="media"
            value={values.media}
            options={subjectYesNoOptions}
            error={attempted && !values.media ? requiredChoiceMessage : undefined}
            onChange={(media) => patch({ media: media as YesNo })}
          />
          <SelectField
            id={`${formId}-exclusive`}
            label="Requiere docente exclusivo"
            name="exclusivo"
            value={values.exclusivo}
            options={subjectYesNoOptions}
            error={attempted && !values.exclusivo ? requiredChoiceMessage : undefined}
            onChange={(exclusivo) => patch({ exclusivo: exclusivo as YesNo })}
          />
          <SelectField
            id={`${formId}-classroom`}
            label="Tipo de aula requerida"
            name="aula"
            value={values.aula}
            options={subjectClassroomOptions}
            error={attempted && !values.aula ? requiredChoiceMessage : undefined}
            onChange={(aula) => patch({ aula })}
          />
          <TextField
            id={`${formId}-max`}
            label="Máximo de clases consecutivas"
            name="maxConsecutivas"
            required
            inputMode="numeric"
            value={values.maxConsecutivas}
            error={
              attempted && maxInvalid
                ? 'Ingresa un máximo de al menos 1.'
                : undefined
            }
            onChange={(event) =>
              patch({ maxConsecutivas: event.target.value.replace(/\D/g, '') })
            }
          />
        </div>
        <SelectField
          id={`${formId}-status`}
          label="Estado"
          name="activa"
          value={values.activa}
          options={subjectStatusOptions}
          error={attempted && !values.activa ? requiredChoiceMessage : undefined}
          onChange={(activa) => patch({ activa: activa as SubjectStatus })}
        />
        <p className={styles.hint}>{subjectFormHint}</p>
      </form>
    </Modal>
  )
}
