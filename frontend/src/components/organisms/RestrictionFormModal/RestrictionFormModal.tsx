import { useRef, useState, type FormEvent } from 'react'
import {
  findTeacherRestriction,
  labelAvailability,
  restrictionApprovalHint,
  restrictionBandOptions,
  restrictionDayOptions,
  restrictionEditApprovalHint,
  restrictionEditTypeHint,
  restrictionEffect,
  restrictionTypeHint,
  restrictionTypeOptions,
  timeBandLabel,
  type RestrictionKind,
  type TeacherRestriction,
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

export type RestrictionFormModalProps = {
  open: boolean
  mode: RestrictionFormMode
  yearId: string
  year: number
  teacherName: string
  day?: WeekDay | ''
  bandId?: string
  tipo?: RestrictionKind | ''
  restriction?: TeacherRestriction
  onClose: () => void
}

type FormValues = {
  day: string
  bandId: string
  tipo: string
  motivo: string
  fileName: string
}

const requiredChoiceMessage = 'Seleccione una opción.'
const motivoMessage = 'Ingresa el motivo.'
const duplicateMessage = 'Esta franja ya tiene una restricción.'

function createValues(day = '', bandId = '', tipo = ''): FormValues {
  return { day, bandId, tipo, motivo: '', fileName: '' }
}

function valuesFromRestriction(restriction: TeacherRestriction): FormValues {
  return {
    day: restriction.day,
    bandId: restriction.bandId,
    tipo: restriction.status,
    motivo: restriction.motivo,
    fileName: restriction.fileName ?? '',
  }
}

function effectNote(status: RestrictionKind) {
  const sentence = restrictionEffect(status)
  return `${labelAvailability(status)}: ${sentence.charAt(0).toLowerCase()}${sentence.slice(1)}`
}

export function RestrictionFormModal({
  open,
  mode,
  yearId,
  year,
  teacherName,
  day = '',
  bandId = '',
  tipo = '',
  restriction,
  onClose,
}: RestrictionFormModalProps) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && restriction
      ? valuesFromRestriction(restriction)
      : createValues(day, bandId, tipo),
  )
  const [attempted, setAttempted] = useState(false)
  const formId = `restriction-${mode}-form`
  const editing = mode === 'edit'
  const occupied = editing
    ? undefined
    : values.day && values.bandId
      ? findTeacherRestriction(yearId, teacherName, values.bandId, values.day as WeekDay)
      : undefined
  const duplicate = Boolean(occupied)

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (!values.day || !values.bandId || !values.tipo || !values.motivo.trim() || duplicate) {
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
      title={editing ? 'Editar restricción' : 'Agregar restricción'}
      description={editing ? undefined : `${teacherName} · Año lectivo ${year}`}
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={editing ? 'Guardar cambios' : 'Guardar restricción'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        {editing ? (
          <div className={styles.context}>
            <p>{teacherName} · Año lectivo {year}</p>
            <p className={styles.slot}>
              {values.day} · {timeBandLabel(values.bandId)}
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
            options={restrictionBandOptions()}
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
          options={editing ? restrictionTypeOptions.filter((option) => option.value) : restrictionTypeOptions}
          hint={editing ? restrictionEditTypeHint : restrictionTypeHint}
          error={attempted && !values.tipo ? requiredChoiceMessage : undefined}
          onChange={(tipo) => patch({ tipo })}
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
          <Button type="button" variant="outline" size="sm" onClick={() => fileRef.current?.click()}>
            {editing ? 'Vincular documento soporte' : 'Adjuntar archivo'}
          </Button>
          <input
            ref={fileRef}
            type="file"
            className={styles.fileInput}
            onChange={(event) => patch({ fileName: event.target.files?.[0]?.name ?? values.fileName })}
          />
          <p>
            Documento soporte (opcional) · {values.fileName || 'Ningún archivo adjunto'}
          </p>
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
      </form>
    </Modal>
  )
}
