import { useState, type FormEvent } from 'react'
import { actualizarTurno, ApiError, horaCorta } from '../../../api'
import type { TurnoResponse } from '../../../api/turnos'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { TextField } from '../../molecules/TextField'
import styles from './TurnoFormModal.module.css'

export type TurnoFormModalProps = {
  open: boolean
  turno?: TurnoResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onUpdated?: (turno: TurnoResponse) => void
}

type FormValues = {
  horaInicio: string
  horaFin: string
  duracionClaseMinutos: string
  clasesPorDia: string
  clasesAntesDeDescanso: string
  duracionDescansoMinutos: string
}

const rangeMessage = 'La hora de inicio debe ser anterior a la hora de finalización'
const structureMessage = 'Las clases antes del descanso no pueden superar las clases por día'

function valuesFromTurno(turno: TurnoResponse): FormValues {
  return {
    horaInicio: horaCorta(turno.horaInicio),
    horaFin: horaCorta(turno.horaFin),
    duracionClaseMinutos: String(turno.duracionClaseMinutos),
    clasesPorDia: String(turno.clasesPorDia),
    clasesAntesDeDescanso: String(turno.clasesAntesDeDescanso),
    duracionDescansoMinutos: String(turno.duracionDescansoMinutos),
  }
}

function entero(value: string) {
  return value === '' ? Number.NaN : Number(value)
}

type TurnoFormFieldsProps = {
  turno: TurnoResponse
  onClose: () => void
  onUpdated?: (turno: TurnoResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function TurnoFormFields({ turno, onClose, onUpdated, onSubmittingChange }: TurnoFormFieldsProps) {
  const [values, setValues] = useState<FormValues>(() => valuesFromTurno(turno))
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = 'turno-edit-form'
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
      !values.horaInicio ||
      !values.horaFin ||
      rangeInvalid ||
      structureInvalid ||
      durationInvalid ||
      classesInvalid ||
      breakInvalid ||
      !Number.isFinite(antes)
    ) {
      return
    }

    // El nombre no se edita: las jornadas de la institución son fijas.
    const payload = {
      nombre: turno.nombre,
      horaInicio: values.horaInicio,
      horaFin: values.horaFin,
      duracionClaseMinutos: entero(values.duracionClaseMinutos),
      clasesPorDia: clases,
      clasesAntesDeDescanso: antes,
      duracionDescansoMinutos: entero(values.duracionDescansoMinutos),
    }

    setBusy(true)

    try {
      onUpdated?.(await actualizarTurno(turno.id, payload))
      onClose()
    } catch (cause) {
      setSubmitError(cause instanceof ApiError ? cause.message : 'No se pudo actualizar el turno.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
      <TextField
        id={`${formId}-name`}
        label="Turno"
        disabled
        value={turno.nombre}
        hint="Las dos jornadas de la institución (Mañana y Contrajornada) vienen definidas y no se renombran."
        onChange={() => undefined}
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
        La hora de inicio y de finalización marcan el rango total de la jornada; las franjas de
        cada clase se definen después en Franjas horarias. La hora de inicio debe ser anterior a la
        de finalización y las clases antes del descanso no pueden superar las del día.
      </p>
    </form>
  )
}

export function TurnoFormModal({
  open,
  turno,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onUpdated,
}: TurnoFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const showForm = !loading && !loadError && Boolean(turno)

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title="Editar turno"
      description="Ajusta el horario y la estructura de la jornada."
      onClose={handleClose}
      footer={
        <ModalActions
          formId="turno-edit-form"
          confirmLabel={
            loadError
              ? undefined
              : loading
                ? 'Cargando...'
                : submitting
                  ? 'Guardando...'
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
      ) : showForm && turno ? (
        <TurnoFormFields
          key={turno.id}
          turno={turno}
          onClose={onClose}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando turno...</p>
      )}
    </Modal>
  )
}
