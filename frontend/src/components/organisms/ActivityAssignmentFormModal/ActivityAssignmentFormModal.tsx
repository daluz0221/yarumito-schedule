import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  activityAssignmentHint,
  assignmentRestrictionWarning,
  namedTeacherValue,
  parseTeacherSelection,
  type AssignmentRow,
} from '../../../content/academicAssignment'
import { countRestrictions } from '../../../content/teacherRestrictions'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { TextArea } from '../../atoms/TextArea'
import { FormField } from '../../molecules/FormField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './ActivityAssignmentFormModal.module.css'

export type ActivityAssignmentFormMode = 'create' | 'edit'

export type ActivityAssignmentFormValues = {
  subjectKey: string
  teacherId: string | null
  teacherName: string
  assignedHours: number
  note: string
}

export type ActivityAssignmentFormModalProps = {
  open: boolean
  mode: ActivityAssignmentFormMode
  yearId: string
  year: number
  activities: AssignmentRow[]
  teachers: Teacher[]
  row?: AssignmentRow
  onClose: () => void
  onSubmit: (values: ActivityAssignmentFormValues) => void
}

type FormValues = {
  subjectKey: string
  teacherValue: string
  hours: string
  note: string
}

const requiredChoiceMessage = 'Seleccione una opción.'
const hoursMessage = 'Ingresa las horas asignadas.'

function initialTeacherValue(row: AssignmentRow | undefined, teachers: Teacher[]) {
  if (row?.teacherId && teachers.some((teacher) => teacher.id === row.teacherId)) {
    return row.teacherId
  }

  if (row?.teacherName) {
    const match = teachers.find(
      (teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase(),
    )
    return match?.id ?? namedTeacherValue(row.teacherName)
  }

  return ''
}

export function ActivityAssignmentFormModal({
  open,
  mode,
  yearId,
  year,
  activities,
  teachers,
  row,
  onClose,
  onSubmit,
}: ActivityAssignmentFormModalProps) {
  const navigate = useNavigate()
  const editing = mode === 'edit'
  const initialActivity =
    row?.key ?? activities.find((item) => !item.assigned)?.key ?? activities[0]?.key ?? ''
  const [values, setValues] = useState<FormValues>(() => ({
    subjectKey: initialActivity,
    teacherValue: initialTeacherValue(row, teachers),
    hours: row ? String(row.assignedHours) : '1',
    note: row?.note ?? '',
  }))
  const [attempted, setAttempted] = useState(false)
  const formId = `activity-assignment-${mode}-form`
  const hoursValue = Number(values.hours)
  const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1
  const selectedTeacher = teachers.find((teacher) => teacher.id === values.teacherValue)
  const teacherName = selectedTeacher
    ? formatTeacherName(selectedTeacher)
    : parseTeacherSelection(values.teacherValue).teacherName
  const restrictionCount = teacherName ? countRestrictions(yearId, teacherName) : 0
  const teacherOptions = [
    { value: '', label: 'Seleccionar docente' },
    ...teachers
      .filter((teacher) => teacher.estado === 'ACTIVO')
      .map((teacher) => ({
        value: teacher.id,
        label: formatTeacherName(teacher),
      })),
    ...(row?.teacherName &&
    !teachers.some(
      (teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase(),
    )
      ? [{ value: namedTeacherValue(row.teacherName), label: row.teacherName }]
      : []),
  ]

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)

    if (!values.subjectKey || !values.teacherValue || hoursInvalid) {
      return
    }

    const teacher = parseTeacherSelection(values.teacherValue)
    const catalogTeacher = teacher.teacherId
      ? teachers.find((item) => item.id === teacher.teacherId)
      : undefined

    onSubmit({
      subjectKey: values.subjectKey,
      teacherId: teacher.teacherId,
      teacherName: catalogTeacher
        ? formatTeacherName(catalogTeacher)
        : teacher.teacherName,
      assignedHours: hoursValue,
      note: values.note.trim(),
    })
  }

  const patch = (partial: Partial<FormValues>) => {
    setValues((current) => ({ ...current, ...partial }))
  }

  return (
    <Modal
      open={open}
      title={editing ? 'Editar actividad o proyecto' : 'Asignar actividad o proyecto'}
      description={`Año lectivo ${year} · Asignación institucional por docente`}
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={editing ? 'Guardar cambios' : 'Guardar asignación'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <SelectField
          id={`${formId}-teacher`}
          label="Docente"
          name="teacher"
          required
          value={values.teacherValue}
          options={teacherOptions}
          error={attempted && !values.teacherValue ? requiredChoiceMessage : undefined}
          onChange={(teacherValue) => patch({ teacherValue })}
        />
        <SelectField
          id={`${formId}-activity`}
          label="Actividad institucional"
          name="activity"
          required
          value={values.subjectKey}
          options={activities.map((item) => ({
            value: item.key,
            label: item.subject,
          }))}
          disabled={editing}
          hint={activityAssignmentHint}
          error={attempted && !values.subjectKey ? requiredChoiceMessage : undefined}
          onChange={(subjectKey) => patch({ subjectKey })}
        />
        <TextField
          id={`${formId}-hours`}
          label="Horas asignadas"
          name="hours"
          required
          inputMode="numeric"
          placeholder="Ej. 1"
          value={values.hours}
          error={attempted && hoursInvalid ? hoursMessage : undefined}
          onChange={(event) => patch({ hours: event.target.value.replace(/\D/g, '') })}
        />
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
        {restrictionCount > 0 ? (
          <p className={styles.warning}>{assignmentRestrictionWarning}</p>
        ) : selectedTeacher ? (
          <p className={styles.note}>Sin restricciones de disponibilidad registradas.</p>
        ) : null}
        {selectedTeacher ? (
          <div className={styles.links}>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() =>
                navigate(`/dashboard/restricciones?docente=${selectedTeacher.id}`)
              }
            >
              Ver restricciones
            </Button>
          </div>
        ) : null}
      </form>
    </Modal>
  )
}
