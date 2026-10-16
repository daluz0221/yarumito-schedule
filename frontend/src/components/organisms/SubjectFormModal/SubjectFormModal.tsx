import { useState, type FormEvent } from 'react'
import { actualizarAsignatura, ApiError, registrarAsignatura } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { AsignaturaResponse } from '../../../api/asignaturas'
import {
  areaSelectOptions,
  SIN_REQUISITO_AULA,
  subjectClassroomOptions,
  subjectColorOptions,
  subjectFormHint,
  subjectClassroomFormValue,
  subjectStatusOptions,
  subjectYesNoOptions,
  type SubjectClassroomFormValue,
  type TipoAulaRequerida,
} from '../../../content/subjects'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
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
  tipoAulaRequerida?: TipoAulaRequerida | null
  maxClasesConsecutivas?: number
  activa?: boolean
}

export type SubjectFormModalProps = {
  open: boolean
  mode: SubjectFormMode
  areas: AreaResponse[]
  selectedAreaId?: string
  subject?: SubjectFormSource
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (subject: AsignaturaResponse) => void
  onUpdated?: (subject: AsignaturaResponse) => void
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
  aula: SubjectClassroomFormValue
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
    aula: subject.tipoAulaRequerida ?? SIN_REQUISITO_AULA,
    maxConsecutivas: String(subject.maxClasesConsecutivas ?? 2),
    activa: statusValue(subject.activa),
  }
}

function optionalText(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function classroomPayload(value: SubjectClassroomFormValue): TipoAulaRequerida | null {
  if (!value || value === SIN_REQUISITO_AULA) {
    return null
  }

  return value
}

type SubjectFormFieldsProps = {
  mode: SubjectFormMode
  areas: AreaResponse[]
  selectedAreaId: string
  subject?: SubjectFormSource
  onClose: () => void
  onCreated?: (subject: AsignaturaResponse) => void
  onUpdated?: (subject: AsignaturaResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function SubjectFormFields({
  mode,
  areas,
  selectedAreaId,
  subject,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: SubjectFormFieldsProps) {
  const initialAreaId = areas.some((area) => area.id === selectedAreaId)
    ? selectedAreaId
    : ''
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && subject ? valuesFromSubject(subject) : createValues(initialAreaId),
  )
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `subject-${mode}-form`
  const creating = mode === 'create'
  const maxValue = Number(values.maxConsecutivas)
  const maxInvalid = !values.maxConsecutivas || !Number.isInteger(maxValue) || maxValue < 1
  const missingChoice =
    !values.areaId ||
    !values.idoneidad ||
    !values.media ||
    !values.exclusivo ||
    !values.aula ||
    !values.activa

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

    if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || maxInvalid) {
      return
    }

    if (!creating && !subject?.id) {
      setSubmitError('No se encontró la asignatura a actualizar.')
      return
    }

    const payload = {
      areaId: values.areaId,
      nombre: values.nombre.trim(),
      codigo: values.codigo.trim(),
      abreviatura: optionalText(values.abreviatura),
      colorUi: optionalText(values.color),
      exigeIdoneidadEstricta: values.idoneidad === 'si',
      esMediaTecnica: values.media === 'si',
      requiereDocenteExclusivo: values.exclusivo === 'si',
      tipoAulaRequerida: classroomPayload(values.aula),
      maxClasesConsecutivas: maxValue,
      activa: values.activa === 'activa',
    }

    setBusy(true)

    try {
      if (creating) {
        const created = await registrarAsignatura(payload)
        onCreated?.(created)
      } else if (subject) {
        const updated = await actualizarAsignatura(subject.id, payload)
        onUpdated?.(updated)
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar la asignatura. Inténtalo de nuevo.'
            : 'No se pudo actualizar la asignatura. Inténtalo de nuevo.',
      )
    } finally {
      setBusy(false)
    }
  }

  const patch = (partial: Partial<FormValues>) => {
    setValues((current) => ({ ...current, ...partial }))
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <SelectField
            id={`${formId}-area`}
            label="Área"
            name="areaId"
            required
            disabled={submitting || !creating}
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
            disabled={submitting}
            placeholder="Ingresa el nombre"
            value={values.nombre}
            onChange={(event) => patch({ nombre: event.target.value })}
          />
          <TextField
            id={`${formId}-code`}
            label="Código"
            name="codigo"
            required
            disabled={submitting}
            placeholder="Ingresa el código"
            value={values.codigo}
            onChange={(event) => patch({ codigo: event.target.value.toUpperCase() })}
          />
          <TextField
            id={`${formId}-abbreviation`}
            label="Abreviatura"
            name="abreviatura"
            disabled={submitting}
            placeholder="Ingresa la abreviatura"
            value={values.abreviatura}
            onChange={(event) => patch({ abreviatura: event.target.value.toUpperCase() })}
          />
          <SelectField
            id={`${formId}-color`}
            label="Color de identificación (opcional)"
            name="color"
            disabled={submitting}
            value={values.color}
            options={subjectColorOptions}
            onChange={(color) => patch({ color })}
          />
          <SelectField
            id={`${formId}-suitability`}
            label="Exige idoneidad estricta"
            name="idoneidad"
            disabled={submitting}
            value={values.idoneidad}
            options={subjectYesNoOptions}
            error={attempted && !values.idoneidad ? requiredChoiceMessage : undefined}
            onChange={(idoneidad) => patch({ idoneidad: idoneidad as YesNo })}
          />
          <SelectField
            id={`${formId}-media`}
            label="Asignatura de Media Técnica"
            name="media"
            disabled={submitting}
            value={values.media}
            options={subjectYesNoOptions}
            error={attempted && !values.media ? requiredChoiceMessage : undefined}
            onChange={(media) => patch({ media: media as YesNo })}
          />
          <SelectField
            id={`${formId}-exclusive`}
            label="Requiere docente exclusivo"
            name="exclusivo"
            disabled={submitting}
            value={values.exclusivo}
            options={subjectYesNoOptions}
            error={attempted && !values.exclusivo ? requiredChoiceMessage : undefined}
            onChange={(exclusivo) => patch({ exclusivo: exclusivo as YesNo })}
          />
          <SelectField
            id={`${formId}-classroom`}
            label="Tipo de aula requerida"
            name="aula"
            disabled={submitting}
            value={values.aula}
            options={subjectClassroomOptions}
            error={attempted && !values.aula ? requiredChoiceMessage : undefined}
            onChange={(aula) => patch({ aula: subjectClassroomFormValue(aula) })}
          />
          <TextField
            id={`${formId}-max`}
            label="Máximo de clases consecutivas"
            name="maxConsecutivas"
            required
            disabled={submitting}
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
          disabled={submitting}
          value={values.activa}
          options={subjectStatusOptions}
          error={attempted && !values.activa ? requiredChoiceMessage : undefined}
          onChange={(activa) => patch({ activa: activa as SubjectStatus })}
        />
        {submitError ? (
          <p className={styles.error} role="alert">
            {submitError}
          </p>
        ) : null}
        <p className={styles.hint}>{subjectFormHint}</p>
      </form>
  )
}

export function SubjectFormModal({
  open,
  mode,
  areas,
  selectedAreaId = '',
  subject,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: SubjectFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(subject))
  const formId = `subject-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      size="lg"
      title={mode === 'create' ? 'Nueva asignatura' : 'Editar asignatura'}
      description={
        mode === 'create'
          ? 'Agrega una asignatura al área seleccionada del catálogo académico.'
          : subject?.nombre
            ? `Actualiza las propiedades académicas de ${subject.nombre}.`
            : 'Actualiza las propiedades académicas de la asignatura seleccionada.'
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
                    ? 'Guardar asignatura'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando asignatura...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <SubjectFormFields
          key={mode === 'edit' ? subject?.id : 'create'}
          mode={mode}
          areas={areas}
          selectedAreaId={selectedAreaId}
          subject={subject}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando asignatura...</p>
      )}
    </Modal>
  )
}
