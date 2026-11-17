import { useState, type FormEvent } from 'react'
import {
  scheduleBandOptions,
  scheduleClassroomOptions,
  scheduleDayOptions,
  type PendingPlacement,
} from '../../../content/scheduleConstruction'
import type { WeekDay } from '../../../content/teacherRestrictions'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { SelectField } from '../../molecules/SelectField'
import styles from './ScheduleClassFormModal.module.css'

export type ScheduleClassFormValues = {
  subjectKey: string
  day: WeekDay
  bandId: string
  classroom: string
}

export type ScheduleClassFormModalProps = {
  open: boolean
  mode: 'create' | 'edit'
  groupCode: string
  subject: string
  teacherName: string
  pendingHours: number
  assignments: PendingPlacement[]
  day: WeekDay
  bandId: string
  classroom: string
  subjectKey: string
  bandOptions?: { value: string; label: string }[]
  classroomOptions?: { value: string; label: string }[]
  submitting?: boolean
  error?: string
  onClose: () => void
  onSubmit: (values: ScheduleClassFormValues) => void
  onRemove?: () => void
}

function aulaParaAsignacion(
  note: string | null,
  options: { value: string; label: string }[],
  current: string,
) {
  const disponibles = options.filter((option) => option.value)

  if (note && /laborat/i.test(note)) {
    const laboratorio = disponibles.find((option) => /lab/i.test(option.label))

    if (laboratorio) {
      return laboratorio.value
    }
  }

  if (disponibles.some((option) => option.value === current)) {
    return current
  }

  return disponibles[0]?.value ?? ''
}

export function ScheduleClassFormModal({
  open,
  mode,
  groupCode,
  subject,
  teacherName,
  pendingHours,
  assignments,
  day,
  bandId,
  classroom,
  subjectKey,
  bandOptions,
  classroomOptions,
  submitting = false,
  error = '',
  onClose,
  onSubmit,
  onRemove,
}: ScheduleClassFormModalProps) {
  const creating = mode === 'create'
  const [values, setValues] = useState({ subjectKey, day, bandId, classroom })
  const formId = creating ? 'schedule-class-create' : 'schedule-class-edit'
  const selected = assignments.find((item) => item.key === values.subjectKey)
  const summarySubject = creating ? selected?.subject ?? subject : subject
  const summaryTeacher = creating ? selected?.teacherName ?? teacherName : teacherName
  const summaryHours = creating ? selected?.pendingHours ?? pendingHours : pendingHours
  const assignmentOptions = assignments.map((item) => ({
    value: item.key,
    label: `${item.subject} · ${item.teacherName} · ${groupCode}`,
  }))

  const roomOptions = classroomOptions ?? scheduleClassroomOptions()
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (submitting || (creating && !selected) || !values.classroom) {
      return
    }

    onSubmit({
      subjectKey: values.subjectKey,
      day: values.day,
      bandId: values.bandId,
      classroom: values.classroom,
    })
  }

  return (
    <Modal
      open={open}
      title={creating ? 'Programar clase' : 'Editar clase programada'}
      onClose={submitting ? () => undefined : onClose}
      footer={
        <>
          <Button variant="muted" size="sm" onClick={onClose} disabled={submitting}>
            Cancelar
          </Button>
          {creating ? null : (
            <Button variant="outline" size="sm" onClick={onRemove} disabled={submitting}>
              Quitar del horario
            </Button>
          )}
          <Button
            type="submit"
            form={formId}
            variant="primary"
            size="sm"
            disabled={submitting || (creating && !selected) || !values.classroom}
          >
            {submitting
              ? creating
                ? 'Programando…'
                : 'Guardando…'
              : creating
                ? 'Programar clase'
                : 'Guardar cambios'}
          </Button>
        </>
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <p className={styles.lead}>
          {creating
            ? 'Ubica una asignación académica existente en el horario.'
            : 'Modifica la ubicación temporal sin alterar la asignación académica.'}
        </p>

        <dl className={styles.summary}>
          {creating ? (
            <>
              <div>
                <dt>Grupo</dt>
                <dd>{groupCode}</dd>
              </div>
              <div>
                <dt>Docente</dt>
                <dd>{summaryTeacher || '—'}</dd>
              </div>
              <div>
                <dt>Asignatura</dt>
                <dd>{summarySubject || '—'}</dd>
              </div>
              <div>
                <dt>Horas pendientes</dt>
                <dd>{summaryHours} h</dd>
              </div>
            </>
          ) : (
            <>
              <div>
                <dt>Asignatura</dt>
                <dd>{summarySubject}</dd>
              </div>
              <div>
                <dt>Docente</dt>
                <dd>{summaryTeacher}</dd>
              </div>
              <div>
                <dt>Grupo</dt>
                <dd>{groupCode}</dd>
              </div>
            </>
          )}
        </dl>

        {creating ? (
          <SelectField
            id="schedule-assignment"
            label="Asignación académica"
            name="assignment"
            required
            value={selected ? values.subjectKey : ''}
            options={
              assignmentOptions.length > 0
                ? assignmentOptions
                : [{ value: '', label: 'Sin horas pendientes' }]
            }
            disabled={assignmentOptions.length === 0 || submitting}
            onChange={(nextKey) => {
              const next = assignments.find((item) => item.key === nextKey)
              setValues((current) => ({
                ...current,
                subjectKey: nextKey,
                classroom: aulaParaAsignacion(next?.note ?? null, roomOptions, current.classroom),
              }))
            }}
          />
        ) : null}

        <div className={styles.pair}>
          <SelectField
            id="schedule-day"
            label="Día"
            name="day"
            required
            value={values.day}
            options={scheduleDayOptions()}
            disabled={submitting}
            onChange={(nextDay) =>
              setValues((current) => ({ ...current, day: nextDay as WeekDay }))
            }
          />
          <SelectField
            id="schedule-band"
            label="Franja horaria"
            name="band"
            required
            value={values.bandId}
            options={bandOptions ?? scheduleBandOptions()}
            disabled={submitting}
            onChange={(nextBand) => setValues((current) => ({ ...current, bandId: nextBand }))}
          />
        </div>

        <SelectField
          id="schedule-classroom"
          label="Aula"
          name="classroom"
          required
          value={values.classroom}
          options={roomOptions.length > 0 ? roomOptions : [{ value: '', label: 'Sin aulas activas' }]}
          disabled={submitting || roomOptions.length === 0}
          onChange={(nextRoom) => setValues((current) => ({ ...current, classroom: nextRoom }))}
        />

        {creating ? (
          <p className={styles.hint}>
            La asignación determina docente, asignatura y grupo. No se reasignan desde este módulo.
          </p>
        ) : null}
        {error ? (
          <p className={styles.error} role="alert">
            {error}
          </p>
        ) : null}
      </form>
    </Modal>
  )
}
