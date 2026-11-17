import { useRef, useState, type FormEvent } from 'react'
import type { RestriccionDocenteVista } from '../../../api/restriccionesDocentes'
import {
  labelAvailability,
  restrictionApprovalHint,
  restrictionDayOptions,
  restrictionEditApprovalHint,
  restrictionEditTypeHint,
  restrictionEffect,
  restrictionTypeHint,
  restrictionTypeOptions,
  type RestrictionKind,
  type WeekDay,
} from '../../../content/teacherRestrictions'
import { TextArea } from '../../atoms/TextArea'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { FormField } from '../../molecules/FormField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import styles from './RestrictionFormModal.module.css'

export type RestrictionFormMode = 'create' | 'edit'

export type RestrictionFormValues = {
  day: WeekDay
  bandId: string
  tipo: RestrictionKind
  motivo: string
  documentoSoporte: string | null
}

export type RestrictionFormModalProps = {
  open: boolean
  mode: RestrictionFormMode
  year: number
  teacherName: string
  day?: WeekDay | ''
  bandId?: string
  bandLabel?: string
  tipo?: RestrictionKind | ''
  restriction?: RestriccionDocenteVista
  bandOptions: { value: string; label: string }[]
  isTaken?: (day: string, bandId: string) => boolean
  submitting?: boolean
  error?: string
  onClose: () => void
  onSubmit: (values: RestrictionFormValues) => void
}

type FormValues = {
  day: string
  bandId: string
  tipo: string
  motivo: string
  fileName: string
  documentoSoporte: string | null
}

const requiredChoiceMessage = 'Seleccione una opción.'
const motivoMessage = 'Ingresa el motivo.'
const duplicateMessage = 'Esta franja ya tiene una restricción.'
const fileMessage = 'No se pudo leer el documento soporte.'
const fileSizeMessage = 'El documento soporte no puede superar 1.5 MB.'
const maxFileBytes = 1_500_000

function createValues(day = '', bandId = '', tipo = ''): FormValues {
  return { day, bandId, tipo, motivo: '', fileName: '', documentoSoporte: null }
}

function valuesFromRestriction(restriction: RestriccionDocenteVista): FormValues {
  return {
    day: restriction.day,
    bandId: restriction.bandId,
    tipo: restriction.status,
    motivo: restriction.motivo,
    fileName: '',
    documentoSoporte: restriction.documentoSoporte,
  }
}

function effectNote(status: RestrictionKind) {
  const sentence = restrictionEffect(status)
  return `${labelAvailability(status)}: ${sentence.charAt(0).toLowerCase()}${sentence.slice(1)}`
}

function archivoABase64(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const comma = result.indexOf(',')
      resolve(comma >= 0 ? result.slice(comma + 1) : result)
    }
    reader.onerror = () => reject(reader.error ?? new Error(fileMessage))
    reader.readAsDataURL(file)
  })
}

export function RestrictionFormModal({
  open,
  mode,
  year,
  teacherName,
  day = '',
  bandId = '',
  bandLabel = '',
  tipo = '',
  restriction,
  bandOptions,
  isTaken,
  submitting = false,
  error = '',
  onClose,
  onSubmit,
}: RestrictionFormModalProps) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && restriction
      ? valuesFromRestriction(restriction)
      : createValues(day, bandId, tipo),
  )
  const [attempted, setAttempted] = useState(false)
  const [fileError, setFileError] = useState('')
  const formId = `restriction-${mode}-form`
  const editing = mode === 'edit'
  const duplicate =
    !editing &&
    Boolean(values.day && values.bandId && isTaken?.(values.day, values.bandId))
  const attachmentLabel = values.fileName
    ? values.fileName
    : values.documentoSoporte
      ? 'Documento adjunto'
      : 'Ningún archivo adjunto'

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (
      submitting ||
      !values.day ||
      !values.bandId ||
      !values.tipo ||
      !values.motivo.trim() ||
      duplicate
    ) {
      return
    }

    onSubmit({
      day: values.day as WeekDay,
      bandId: values.bandId,
      tipo: values.tipo as RestrictionKind,
      motivo: values.motivo.trim(),
      documentoSoporte: values.documentoSoporte,
    })
  }

  const patch = (partial: Partial<FormValues>) => {
    setValues((current) => ({ ...current, ...partial }))
  }

  const attachFile = async (file: File | undefined) => {
    if (!file) {
      return
    }

    if (file.size > maxFileBytes) {
      setFileError(fileSizeMessage)
      return
    }

    try {
      const documentoSoporte = await archivoABase64(file)
      setFileError('')
      patch({ fileName: file.name, documentoSoporte })
    } catch {
      setFileError(fileMessage)
    }
  }

  return (
    <Modal
      open={open}
      title={editing ? 'Editar restricción' : 'Agregar restricción'}
      description={editing ? undefined : `${teacherName} · Año lectivo ${year}`}
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={
            submitting ? 'Guardando…' : editing ? 'Guardar cambios' : 'Guardar restricción'
          }
          confirmType="submit"
          disabled={submitting}
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        {editing ? (
          <div className={styles.context}>
            <p>
              {teacherName} · Año lectivo {year}
            </p>
            <p className={styles.slot}>
              {values.day} · {bandLabel}
            </p>
          </div>
        ) : null}
        {editing ? null : (
          <div className={styles.grid}>
            <SelectField
              id={`${formId}-day`}
              label="Día"
              name="day"
              required
              value={values.day}
              options={restrictionDayOptions()}
              error={attempted && !values.day ? requiredChoiceMessage : undefined}
              onChange={(nextDay) => patch({ day: nextDay })}
            />
            <SelectField
              id={`${formId}-band`}
              label="Franja"
              name="band"
              required
              value={values.bandId}
              options={[{ value: '', label: 'Seleccione una franja' }, ...bandOptions]}
              error={
                attempted && !values.bandId
                  ? requiredChoiceMessage
                  : attempted && duplicate
                    ? duplicateMessage
                    : undefined
              }
              onChange={(nextBand) => patch({ bandId: nextBand })}
            />
          </div>
        )}
        <SelectField
          id={`${formId}-type`}
          label="Tipo"
          name="tipo"
          required
          value={values.tipo}
          options={
            editing
              ? restrictionTypeOptions.filter((option) => option.value)
              : restrictionTypeOptions
          }
          hint={editing ? restrictionEditTypeHint : restrictionTypeHint}
          error={attempted && !values.tipo ? requiredChoiceMessage : undefined}
          onChange={(nextTipo) => patch({ tipo: nextTipo })}
        />
        <FormField
          id={`${formId}-motivo`}
          label="Motivo"
          required
          error={attempted && !values.motivo.trim() ? motivoMessage : undefined}
        >
          <TextArea
            id={`${formId}-motivo`}
            name="motivo"
            required
            rows={3}
            placeholder="Describe la condición de disponibilidad"
            value={values.motivo}
            aria-invalid={attempted && !values.motivo.trim()}
            onChange={(event) => patch({ motivo: event.target.value })}
          />
        </FormField>
        <div className={styles.file}>
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={submitting}
            onClick={() => fileRef.current?.click()}
          >
            {editing ? 'Vincular documento soporte' : 'Adjuntar archivo'}
          </Button>
          <input
            ref={fileRef}
            type="file"
            className={styles.fileInput}
            onChange={(event) => {
              const file = event.target.files?.[0]
              event.target.value = ''
              void attachFile(file)
            }}
          />
          <p>Documento soporte (opcional) · {attachmentLabel}</p>
          {fileError ? (
            <p className={styles.error} role="alert">
              {fileError}
            </p>
          ) : null}
        </div>
        <p className={styles.note}>
          {editing ? restrictionEditApprovalHint : restrictionApprovalHint}
        </p>
        {editing ? null : (
          <>
            <p className={styles.note}>{effectNote('NO_DISPONIBLE')}</p>
            <p className={styles.note}>{effectNote('PREFERENCIA')}</p>
          </>
        )}
        {error ? (
          <p className={styles.error} role="alert">
            {error}
          </p>
        ) : null}
      </form>
    </Modal>
  )
}
