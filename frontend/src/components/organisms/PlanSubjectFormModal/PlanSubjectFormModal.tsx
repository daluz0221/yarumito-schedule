import { useEffect, useState, type FormEvent } from 'react'
import {
  actualizarPlanEstudios,
  ApiError,
  listarAreas,
  listarAsignaturas,
  listarCatalogoTurnos,
  registrarPlanEstudios,
} from '../../../api'
import type { PlanEstudiosResponse } from '../../../api/planesEstudio'
import type { TurnoResponse } from '../../../api/turnos'
import { derivedAreaLabel } from '../../../content/studyPlan'
import { TextArea } from '../../atoms/TextArea'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { FormField } from '../../molecules/FormField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './PlanSubjectFormModal.module.css'

export type PlanSubjectFormMode = 'create' | 'edit'

type CatalogSubject = {
  id: string
  nombre: string
  areaNombre: string
}

export type PlanSubjectFormModalProps = {
  open: boolean
  mode: PlanSubjectFormMode
  year: number
  yearId: string
  gradeId: string
  gradeLabel: string
  planes: PlanEstudiosResponse[]
  plan?: PlanEstudiosResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (plan: PlanEstudiosResponse) => void
  onUpdated?: (plan: PlanEstudiosResponse) => void
}

type FormValues = {
  subjectId: string
  hours: string
  turnoId: string
  note: string
}

const requiredChoiceMessage = 'Seleccione una opción.'
const hoursMessage = 'Ingresa las horas semanales.'
const duplicateCreateMessage =
  'La asignatura ya está registrada para ese grado en el año escolar indicado'
const duplicateEditMessage =
  'Ya existe otro registro de esa asignatura para ese grado en el año escolar indicado'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function createValues(): FormValues {
  return {
    subjectId: '',
    hours: '',
    turnoId: '',
    note: '',
  }
}

function valuesFromPlan(plan: PlanEstudiosResponse): FormValues {
  return {
    subjectId: plan.asignaturaId,
    hours: String(plan.horasSemanales),
    turnoId: plan.turnoId ?? '',
    note: plan.observacion ?? '',
  }
}

function asignaturaOcupada(
  planes: PlanEstudiosResponse[],
  asignaturaId: string,
  planId?: string,
) {
  return planes.some((plan) => plan.id !== planId && plan.asignaturaId === asignaturaId)
}

type PlanSubjectFormFieldsProps = {
  mode: PlanSubjectFormMode
  yearId: string
  gradeId: string
  planes: PlanEstudiosResponse[]
  plan?: PlanEstudiosResponse
  onClose: () => void
  onCreated?: (plan: PlanEstudiosResponse) => void
  onUpdated?: (plan: PlanEstudiosResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function PlanSubjectFormFields({
  mode,
  yearId,
  gradeId,
  planes,
  plan,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: PlanSubjectFormFieldsProps) {
  const [catalog, setCatalog] = useState<CatalogSubject[]>([])
  const [turnos, setTurnos] = useState<TurnoResponse[]>([])
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && plan ? valuesFromPlan(plan) : createValues(),
  )
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `plan-subject-${mode}-form`
  const creating = mode === 'create'
  const selected = catalog.find((item) => item.id === values.subjectId)
  const areaName = selected?.areaNombre ?? (plan?.asignaturaId === values.subjectId ? '' : '')
  const hoursValue = Number(values.hours)
  const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1
  const duplicate = values.subjectId
    ? asignaturaOcupada(planes, values.subjectId, plan?.id)
    : false
  const duplicateMessage = creating ? duplicateCreateMessage : duplicateEditMessage
  const nameCounts = catalog.reduce<Record<string, number>>((counts, item) => {
    counts[item.nombre] = (counts[item.nombre] ?? 0) + 1
    return counts
  }, {})
  const subjectOptions = [
    { value: '', label: 'Seleccionar asignatura' },
    ...(plan && !catalog.some((item) => item.id === plan.asignaturaId)
      ? [{ value: plan.asignaturaId, label: plan.asignaturaNombre }]
      : []),
    ...catalog.map((item) => ({
      value: item.id,
      label: nameCounts[item.nombre] > 1 ? `${item.nombre} (${item.areaNombre})` : item.nombre,
    })),
  ]

  const setBusy = (next: boolean) => {
    setSubmitting(next)
    onSubmittingChange(next)
  }

  useEffect(() => {
    const controller = new AbortController()

    listarAreas(controller.signal)
      .then(async (areas) => {
        const groups = await Promise.all(
          areas.map((area) =>
            listarAsignaturas(area.id, controller.signal, { activa: true }).then((subjects) =>
              subjects.map((subject) => ({
                id: subject.id,
                nombre: subject.nombre,
                areaNombre: area.nombre,
              })),
            ),
          ),
        )

        if (!controller.signal.aborted) {
          setCatalog(
            groups.flat().sort((left, right) => left.nombre.localeCompare(right.nombre, 'es')),
          )
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setCatalog([])
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoTurnos(controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setTurnos(items)
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setTurnos([])
      })

    return () => controller.abort()
  }, [])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)
    setSubmitError('')

    if (submitting || !values.subjectId || hoursInvalid || duplicate) {
      return
    }

    if (!creating && !plan?.id) {
      setSubmitError('No se encontró el registro del plan a actualizar.')
      return
    }

    const payload = {
      anioEscolarId: yearId,
      gradoId: gradeId,
      asignaturaId: values.subjectId,
      horasSemanales: hoursValue,
      turnoId: values.turnoId || null,
      observacion: values.note.trim() || null,
    }

    setBusy(true)

    try {
      if (creating) {
        const created = await registrarPlanEstudios(payload)
        onCreated?.(created)
      } else if (plan) {
        const updated = await actualizarPlanEstudios(plan.id, payload)
        onUpdated?.(updated)
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar la asignatura en el plan. Inténtalo de nuevo.'
            : 'No se pudo actualizar el plan de estudios. Inténtalo de nuevo.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.grid}>
        <SelectField
          id={`${formId}-subject`}
          label="Asignatura"
          name="subject"
          required
          disabled={submitting}
          value={values.subjectId}
          options={subjectOptions}
          error={
            attempted && !values.subjectId
              ? requiredChoiceMessage
              : duplicate
                ? duplicateMessage
                : undefined
          }
          onChange={(subjectId) => setValues((current) => ({ ...current, subjectId }))}
        />
        <TextField
          id={`${formId}-area`}
          label="Área"
          name="area"
          disabled
          className={areaName ? styles.locked : undefined}
          placeholder={derivedAreaLabel}
          value={areaName}
          onChange={() => undefined}
        />
        <TextField
          id={`${formId}-hours`}
          label="Horas semanales"
          name="hours"
          required
          inputMode="numeric"
          disabled={submitting}
          placeholder="Ej. 4"
          value={values.hours}
          error={attempted && hoursInvalid ? hoursMessage : undefined}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              hours: event.target.value.replace(/\D/g, ''),
            }))
          }
        />
        <SelectField
          id={`${formId}-shift`}
          label="Turno"
          name="shift"
          disabled={submitting}
          value={values.turnoId}
          options={[
            { value: '', label: 'Sin asignar' },
            ...turnos.map((turno) => ({ value: turno.id, label: turno.nombre })),
            ...(values.turnoId && !turnos.some((turno) => turno.id === values.turnoId)
              ? [{ value: values.turnoId, label: 'Turno actual' }]
              : []),
          ]}
          onChange={(turnoId) => setValues((current) => ({ ...current, turnoId }))}
        />
      </div>
      <FormField id={`${formId}-note`} label="Observación">
        <TextArea
          id={`${formId}-note`}
          name="note"
          rows={3}
          disabled={submitting}
          placeholder="Observación opcional"
          value={values.note}
          onChange={(event) =>
            setValues((current) => ({ ...current, note: event.target.value }))
          }
        />
      </FormField>
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
      <p className={styles.hint}>
        El área sale de la asignatura. Una asignatura no puede repetirse en el mismo
        año y grado. El turno es opcional.
      </p>
    </form>
  )
}

export function PlanSubjectFormModal({
  open,
  mode,
  year,
  yearId,
  gradeId,
  gradeLabel,
  planes,
  plan,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: PlanSubjectFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(plan))
  const formId = `plan-subject-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title={creating ? 'Agregar asignatura al plan' : 'Editar asignatura del plan'}
      description={
        creating
          ? `Año lectivo: ${year} · Grado: ${gradeLabel}`
          : `Año lectivo ${year} · Grado ${gradeLabel}. Los datos se cargan desde el servidor.`
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
                    ? 'Agregar al plan'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading || Boolean(loadError)}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando el registro del plan...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <PlanSubjectFormFields
          key={mode === 'edit' ? plan?.id : 'create'}
          mode={mode}
          yearId={yearId}
          gradeId={gradeId}
          planes={planes}
          plan={plan}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando el registro del plan...</p>
      )}
    </Modal>
  )
}
