import { useEffect, useState, type FormEvent } from 'react'
import { listarAreas, listarAsignaturas } from '../../../api'
import {
  derivedAreaLabel,
  planSubjectFormHint,
  studyPlanShiftOptions,
  type StudyPlanEntry,
} from '../../../content/studyPlan'
import { TextArea } from '../../atoms/TextArea'
import { Modal } from '../../atoms/Modal'
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
  gradeLabel: string
  subjectsInPlan: string[]
  entry?: StudyPlanEntry
  onClose: () => void
}

type FormValues = {
  subjectId: string
  hours: string
  shift: string
  note: string
}

const requiredChoiceMessage = 'Seleccione una opción.'
const hoursMessage = 'Ingresa las horas semanales.'
const duplicateMessage = 'Esta asignatura ya está en el plan de este grado.'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function createValues(): FormValues {
  return {
    subjectId: '',
    hours: '',
    shift: 'Mañana',
    note: '',
  }
}

function valuesFromEntry(entry: StudyPlanEntry): FormValues {
  return {
    subjectId: `plan:${entry.id}`,
    hours: String(entry.weeklyHours),
    shift: entry.shift || 'Mañana',
    note: entry.note ?? '',
  }
}

export function PlanSubjectFormModal({
  open,
  mode,
  year,
  gradeLabel,
  subjectsInPlan,
  entry,
  onClose,
}: PlanSubjectFormModalProps) {
  const [catalog, setCatalog] = useState<CatalogSubject[]>([])
  const [values, setValues] = useState<FormValues>(() =>
    mode === 'edit' && entry ? valuesFromEntry(entry) : createValues(),
  )
  const [attempted, setAttempted] = useState(false)
  const formId = `plan-subject-${mode}-form`
  const localId = entry ? `plan:${entry.id}` : ''
  const catalogMatch = entry
    ? catalog.find((item) => item.nombre === entry.subject)
    : undefined
  const selectedId =
    values.subjectId === localId && catalogMatch ? catalogMatch.id : values.subjectId
  const selected = catalog.find((item) => item.id === selectedId)
  const areaName =
    selected?.areaNombre ?? (entry && selectedId === localId ? entry.area : '')
  const selectedName =
    selected?.nombre ?? (entry && selectedId === localId ? entry.subject : '')
  const hoursValue = Number(values.hours)
  const hoursInvalid =
    !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1
  const duplicate =
    Boolean(selectedName) &&
    subjectsInPlan.includes(selectedName) &&
    selectedName !== entry?.subject
  const nameCounts = catalog.reduce<Record<string, number>>((counts, item) => {
    counts[item.nombre] = (counts[item.nombre] ?? 0) + 1
    return counts
  }, {})
  const subjectOptions = [
    { value: '', label: 'Seleccionar asignatura' },
    ...(entry && !catalogMatch ? [{ value: localId, label: entry.subject }] : []),
    ...catalog.map((item) => ({
      value: item.id,
      label: nameCounts[item.nombre] > 1 ? `${item.nombre} (${item.areaNombre})` : item.nombre,
    })),
  ]

  useEffect(() => {
    const controller = new AbortController()

    listarAreas(controller.signal)
      .then(async (areas) => {
        const groups = await Promise.all(
          areas.map((area) =>
            listarAsignaturas(area.id, controller.signal).then((subjects) =>
              subjects
                .filter((subject) => subject.activa)
                .map((subject) => ({
                  id: subject.id,
                  nombre: subject.nombre,
                  areaNombre: area.nombre,
                })),
            ),
          ),
        )

        setCatalog(
          groups
            .flat()
            .sort((left, right) => left.nombre.localeCompare(right.nombre, 'es')),
        )
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setCatalog([])
      })

    return () => controller.abort()
  }, [])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (!selectedId || hoursInvalid || duplicate) {
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
      title={mode === 'create' ? 'Agregar asignatura al plan' : 'Editar asignatura del plan'}
      description={
        mode === 'create'
          ? `Año lectivo: ${year} · Grado: ${gradeLabel}`
          : `Año lectivo ${year} · Grado ${gradeLabel} · Edita la asignatura seleccionada.`
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={mode === 'create' ? 'Agregar al plan' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <SelectField
            id={`${formId}-subject`}
            label="Asignatura"
            name="subject"
            required
            value={selectedId}
            options={subjectOptions}
            error={
              attempted && !selectedId
                ? requiredChoiceMessage
                : attempted && duplicate
                  ? duplicateMessage
                  : undefined
            }
            onChange={(subjectId) => patch({ subjectId })}
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
            placeholder="Ej. 4"
            value={values.hours}
            error={attempted && hoursInvalid ? hoursMessage : undefined}
            onChange={(event) => patch({ hours: event.target.value.replace(/\D/g, '') })}
          />
          <SelectField
            id={`${formId}-shift`}
            label="Turno"
            name="shift"
            required
            value={values.shift}
            options={studyPlanShiftOptions(values.shift)}
            onChange={(shift) => patch({ shift })}
          />
        </div>
        <FormField id={`${formId}-note`} label="Observación">
          <TextArea
            id={`${formId}-note`}
            name="note"
            rows={3}
            placeholder="Observación opcional"
            value={values.note}
            onChange={(event) => patch({ note: event.target.value })}
          />
        </FormField>
        <p className={styles.hint}>{planSubjectFormHint}</p>
      </form>
    </Modal>
  )
}
