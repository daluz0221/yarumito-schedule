import { useState, type FormEvent } from 'react'
import {
  defaultClassroom,
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
  onClose: () => void
  onSubmit: (values: ScheduleClassFormValues) => void
  onRemove?: () => void
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

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (creating && !selected) {
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
      onClose={onClose}
      footer={
        <>
          <Button variant="muted" size="sm" onClick={onClose}>
            Cancelar
          </Button>
          {creating ? null : (
            <Button variant="outline" size="sm" onClick={onRemove}>
              Quitar del horario
            </Button>
          )}
          <Button type="submit" form={formId} variant="primary" size="sm" disabled={creating && !selected}>
            {creating ? 'Programar clase' : 'Guardar cambios'}
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
            disabled={assignmentOptions.length === 0}
            onChange={(nextKey) => {
              const next = assignments.find((item) => item.key === nextKey)
              setValues((current) => ({
                ...current,
                subjectKey: nextKey,
                classroom: defaultClassroom(next?.note ?? null),
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
            options={scheduleBandOptions()}
            onChange={(nextBand) => setValues((current) => ({ ...current, bandId: nextBand }))}
          />
        </div>

        <SelectField
          id="schedule-classroom"
          label="Aula"
          name="classroom"
          required
          value={values.classroom}
          options={scheduleClassroomOptions()}
          onChange={(nextRoom) => setValues((current) => ({ ...current, classroom: nextRoom }))}
        />

        {creating ? (
          <p className={styles.hint}>
            La asignación determina docente, asignatura y grupo. No se reasignan desde este módulo.
          </p>
        ) : null}
      </form>
    </Modal>
  )
}
