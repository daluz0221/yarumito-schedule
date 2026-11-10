import { useState, type FormEvent } from 'react'
import { actualizarTurno, ApiError, horaCorta, registrarTurno } from '../../../api'
import type { TurnoResponse } from '../../../api/turnos'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { TextField } from '../../molecules/TextField'
import styles from './TurnoFormModal.module.css'

export type TurnoFormMode = 'create' | 'edit'

export type TurnoFormModalProps = {
  open: boolean
  mode: TurnoFormMode
  turnos: TurnoResponse[]
  turno?: TurnoResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (turno: TurnoResponse) => void
  onUpdated?: (turno: TurnoResponse) => void
}

type FormValues = {
  nombre: string
  horaInicio: string
  horaFin: string
  duracionClaseMinutos: string
  clasesPorDia: string
  clasesAntesDeDescanso: string
  duracionDescansoMinutos: string
}

const duplicateMessage = 'Ya existe un turno con ese nombre'
const rangeMessage = 'La hora de inicio debe ser anterior a la hora de finalización'
const structureMessage = 'Las clases antes del descanso no pueden superar las clases por día'

const emptyValues: FormValues = {
  nombre: '',
  horaInicio: '',
  horaFin: '',
  duracionClaseMinutos: '55',
  clasesPorDia: '6',
  clasesAntesDeDescanso: '3',
  duracionDescansoMinutos: '30',
}

function normalizarNombre(nombre: string) {
  return nombre.trim().replace(/\s+/g, ' ')
}

function valuesFromTurno(turno?: TurnoResponse): FormValues {
  if (!turno) {
    return emptyValues
  }

  return {
    nombre: turno.nombre,
    horaInicio: horaCorta(turno.horaInicio),
    horaFin: horaCorta(turno.horaFin),
    duracionClaseMinutos: String(turno.duracionClaseMinutos),
    clasesPorDia: String(turno.clasesPorDia),
    clasesAntesDeDescanso: String(turno.clasesAntesDeDescanso),
    duracionDescansoMinutos: String(turno.duracionDescansoMinutos),
  }
}

function nombreOcupado(turnos: TurnoResponse[], nombre: string, turnoId?: string) {
  const normalizado = normalizarNombre(nombre).toLowerCase()

  if (!normalizado) {
    return false
  }

  return turnos.some(
    (turno) => turno.id !== turnoId && normalizarNombre(turno.nombre).toLowerCase() === normalizado,
  )
}

function entero(value: string) {
  return value === '' ? Number.NaN : Number(value)
}

type TurnoFormFieldsProps = {
  mode: TurnoFormMode
  turnos: TurnoResponse[]
  turno?: TurnoResponse
  onClose: () => void
  onCreated?: (turno: TurnoResponse) => void
  onUpdated?: (turno: TurnoResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function TurnoFormFields({
  mode,
  turnos,
  turno,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: TurnoFormFieldsProps) {
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' ? valuesFromTurno(turno) : emptyValues,
  )
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `turno-${mode}-form`
  const creating = mode === 'create'
  const nameTaken = nombreOcupado(turnos, values.nombre, turno?.id)
  const rangeInvalid = Boolean(values.horaInicio && values.horaFin && values.horaInicio >= values.horaFin)
  const clases = entero(values.clasesPorDia)
  const antes = entero(values.clasesAntesDeDescanso)
  const structureInvalid = Number.isFinite(clases) && Number.isFinite(antes) && antes > clases
  const durationInvalid = entero(values.duracionClaseMinutos) < 1
  const classesInvalid = clases < 1
  const breakInvalid = entero(values.duracionDescansoMinutos) < 0 || values.duracionDescansoMinutos === ''

  const setBusy = (next: boolean) => {
    setSubmitting(next)
    onSubmittingChange(next)
  }

  const setNumber =
    (field: 'duracionClaseMinutos' | 'clasesPorDia' | 'clasesAntesDeDescanso' | 'duracionDescansoMinutos') =>
    (value: string) => {
      setValues((current) => ({ ...current, [field]: value.replace(/\D/g, '') }))
    }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)
    setSubmitError('')

    if (
      submitting ||
      !normalizarNombre(values.nombre) ||
      !values.horaInicio ||
      !values.horaFin ||
      nameTaken ||
      rangeInvalid ||
      structureInvalid ||
      durationInvalid ||
      classesInvalid ||
      breakInvalid ||
      !Number.isFinite(antes)
    ) {
      return
    }

    if (!creating && !turno?.id) {
      setSubmitError('No se encontró el turno a actualizar.')
      return
    }

    const payload = {
      nombre: normalizarNombre(values.nombre),
      horaInicio: values.horaInicio,
      horaFin: values.horaFin,
      duracionClaseMinutos: entero(values.duracionClaseMinutos),
      clasesPorDia: clases,
      clasesAntesDeDescanso: antes,
      duracionDescansoMinutos: entero(values.duracionDescansoMinutos),
    }

    setBusy(true)

    try {
      if (creating) {
        onCreated?.(await registrarTurno(payload))
      } else if (turno) {
        onUpdated?.(await actualizarTurno(turno.id, payload))
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar el turno.'
            : 'No se pudo actualizar el turno.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
      <TextField
        id={`${formId}-name`}
        label="Nombre del turno"
        required
        maxLength={50}
        disabled={submitting}
        placeholder="Ejemplo: Mañana"
        value={values.nombre}
        error={
          nameTaken
            ? duplicateMessage
            : attempted && !normalizarNombre(values.nombre)
              ? 'El nombre del turno es obligatorio'
              : undefined
        }
        onChange={(event) => setValues((current) => ({ ...current, nombre: event.target.value }))}
      />
      <div className={styles.grid}>
        <TextField
          id={`${formId}-start`}
          label="Hora de inicio"
          type="time"
          required
          disabled={submitting}
          value={values.horaInicio}
          error={
            rangeInvalid
              ? rangeMessage
              : attempted && !values.horaInicio
                ? 'La hora de inicio es obligatoria'
                : undefined
          }
          onChange={(event) =>
            setValues((current) => ({ ...current, horaInicio: event.target.value }))
          }
        />
        <TextField
          id={`${formId}-end`}
          label="Hora de finalización"
          type="time"
          required
          disabled={submitting}
          value={values.horaFin}
          error={attempted && !values.horaFin ? 'La hora de finalización es obligatoria' : undefined}
          onChange={(event) => setValues((current) => ({ ...current, horaFin: event.target.value }))}
        />
        <TextField
          id={`${formId}-class`}
          label="Duración de la clase (min)"
          inputMode="numeric"
          required
          disabled={submitting}
          value={values.duracionClaseMinutos}
          error={
            attempted && durationInvalid ? 'La duración de la clase debe ser mayor que cero' : undefined
          }
          onChange={(event) => setNumber('duracionClaseMinutos')(event.target.value)}
        />
        <TextField
          id={`${formId}-per-day`}
          label="Clases por día"
          inputMode="numeric"
          required
          disabled={submitting}
          value={values.clasesPorDia}
          error={
            attempted && classesInvalid ? 'La cantidad de clases por día debe ser mayor que cero' : undefined
          }
          onChange={(event) => setNumber('clasesPorDia')(event.target.value)}
        />
        <TextField
          id={`${formId}-before-break`}
          label="Clases antes del descanso"
          inputMode="numeric"
          required
          disabled={submitting}
          value={values.clasesAntesDeDescanso}
          error={
            structureInvalid
              ? structureMessage
              : attempted && !Number.isFinite(antes)
                ? 'Indica las clases antes del descanso'
                : undefined
          }
          onChange={(event) => setNumber('clasesAntesDeDescanso')(event.target.value)}
        />
        <TextField
          id={`${formId}-break`}
          label="Descanso (min)"
          inputMode="numeric"
          required
          disabled={submitting}
          value={values.duracionDescansoMinutos}
          error={attempted && breakInvalid ? 'La duración del descanso no puede ser negativa' : undefined}
          onChange={(event) => setNumber('duracionDescansoMinutos')(event.target.value)}
        />
      </div>
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
      <p className={styles.hint}>
        La hora de inicio debe ser anterior a la de finalización. Las clases antes del descanso no
        pueden superar las clases del día.
      </p>
    </form>
  )
}

export function TurnoFormModal({
  open,
  mode,
  turnos,
  turno,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: TurnoFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(turno))
  const formId = `turno-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title={creating ? 'Nuevo turno' : 'Editar turno'}
      description={
        creating
          ? 'Registra una jornada de la institución.'
          : 'Actualiza el horario y la estructura del turno.'
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
                    ? 'Guardar turno'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading || Boolean(loadError)}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando turno...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <TurnoFormFields
          key={creating ? 'create' : turno?.id}
          mode={mode}
          turnos={turnos}
          turno={turno}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando turno...</p>
      )}
    </Modal>
  )
}
