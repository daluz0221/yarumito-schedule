import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { listarIdoneidadesPorDocente } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import {
  assignmentKindHint,
  assignmentRestrictionWarning,
  assignmentTeacherPriorityHint,
  hoursStatusCopy,
  namedTeacherValue,
  parseTeacherSelection,
  subjectPlanHint,
  teacherCompatibilityHint,
  type AssignmentKind,
  type AssignmentRow,
  type CurricularKind,
} from '../../../content/academicAssignment'
import { isIdoneidadVigente, resolveAreaName } from '../../../content/teacherProfile'
import { countRestrictions } from '../../../content/teacherRestrictions'
import { formatTeacherName, labelEstado, type Teacher } from '../../../content/teachers'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { TextArea } from '../../atoms/TextArea'
import { FormField } from '../../molecules/FormField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import { AssignmentKindModal } from '../AssignmentKindModal'
import styles from './AssignmentFormModal.module.css'

export type AssignmentFormMode = 'create' | 'edit'

export type AssignmentFormValues = {
  subjectKey: string
  teacherId: string | null
  teacherName: string
  assignedHours: number
  kind: CurricularKind
  note: string
}

export type AssignmentFormModalProps = {
  open: boolean
  mode: AssignmentFormMode
  yearId: string
  year: number
  groupCode: string
  gradeLabel: string
  subjects: AssignmentRow[]
  teachers: Teacher[]
  areas: AreaResponse[]
  row?: AssignmentRow
  onClose: () => void
  onSubmit: (values: AssignmentFormValues) => void
}

type FormValues = {
  subjectKey: string
  teacherValue: string
  hours: string
  kind: CurricularKind
  note: string
}

const requiredChoiceMessage = 'Seleccione una opción.'
const hoursMessage = 'Ingresa las horas semanales asignadas.'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

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

export function AssignmentFormModal({
  open,
  mode,
  yearId,
  year,
  groupCode,
  gradeLabel,
  subjects,
  teachers,
  areas,
  row,
  onClose,
  onSubmit,
}: AssignmentFormModalProps) {
  const navigate = useNavigate()
  const editing = mode === 'edit'
  const initialSubject =
    row?.key ?? subjects.find((item) => !item.assigned)?.key ?? subjects[0]?.key ?? ''
  const [values, setValues] = useState<FormValues>(() => ({
    subjectKey: initialSubject,
    teacherValue: initialTeacherValue(row, teachers),
    hours: row
      ? String(row.assignedHours)
      : String(subjects.find((item) => item.key === initialSubject)?.requiredHours ?? ''),
    kind: (row?.kind as CurricularKind | null) ?? 'PRINCIPAL',
    note: row?.note ?? (editing ? `Asignación curricular del grupo ${groupCode}` : ''),
  }))
  const [attempted, setAttempted] = useState(false)
  const [kindOpen, setKindOpen] = useState(false)
  const [hasValidIdoneidad, setHasValidIdoneidad] = useState(false)
  const formId = `assignment-${mode}-form`
  const selected = subjects.find((item) => item.key === values.subjectKey)
  const hoursValue = Number(values.hours)
  const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1
  const hoursCopy = selected ? hoursStatusCopy(selected.requiredHours, hoursInvalid ? 0 : hoursValue) : null
  const teacherOptions = teacherSelectOptions(teachers, row)
  const selectedTeacher = resolveSelectedTeacher(values.teacherValue, teachers)
  const teacherArea = selectedTeacher
    ? resolveAreaName(selectedTeacher.areaNombramientoId, areas)
    : ''
  const restrictionCount = selectedTeacher
    ? countRestrictions(yearId, formatTeacherName(selectedTeacher))
    : values.teacherValue.startsWith('name:')
      ? countRestrictions(yearId, parseTeacherSelection(values.teacherValue).teacherName)
      : 0
  const teacherHint = selectedTeacher
    ? [
        teacherCompatibilityHint(
          `Docente ${labelEstado(selectedTeacher.estado).toLowerCase()}`,
          teacherArea,
          selected?.area ?? '',
          hasValidIdoneidad,
        ),
        assignmentTeacherPriorityHint,
      ].join(' ')
    : values.teacherValue
      ? assignmentTeacherPriorityHint
      : undefined

  useEffect(() => {
    const teacherId = parseTeacherSelection(values.teacherValue).teacherId
    if (!teacherId || !selected?.area) {
      setHasValidIdoneidad(false)
      return
    }

    const controller = new AbortController()
    listarIdoneidadesPorDocente(teacherId, controller.signal)
      .then((items) => {
        setHasValidIdoneidad(
          items.some((item) => {
            if (!isIdoneidadVigente(item)) {
              return false
            }

            const areaName = resolveAreaName(item.areaId, areas)
            return areaName.toLowerCase() === selected.area.toLowerCase()
          }),
        )
      })
      .catch((cause) => {
        if (!isAbortError(cause)) {
          setHasValidIdoneidad(false)
        }
      })

    return () => controller.abort()
  }, [areas, selected?.area, values.teacherValue])

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
      kind: values.kind,
      note: values.note.trim(),
    })
  }

  const patch = (partial: Partial<FormValues>) => {
    setValues((current) => ({ ...current, ...partial }))
  }

  const adjustHours = (delta: number) => {
    const current = hoursInvalid ? selected?.requiredHours ?? 1 : hoursValue
    patch({ hours: String(Math.max(1, current + delta)) })
  }

  return (
    <>
      <Modal
        open={open}
        title={editing ? 'Editar asignación académica' : 'Nueva asignación académica'}
        description={`Año lectivo ${year} · Grupo ${groupCode} · Grado ${gradeLabel}`}
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
          {editing && selected ? (
            <p className={styles.context}>
              Asignatura: {selected.subject} · Requeridas: {selected.requiredHours} h
            </p>
          ) : null}
          <FormField
            id={`${formId}-kind`}
            label="Tipo de asignación"
            hint={assignmentKindHint}
          >
            <button
              id={`${formId}-kind`}
              type="button"
              className={styles.trigger}
              onClick={() => setKindOpen(true)}
            >
              {labelKind(values.kind)}
            </button>
          </FormField>

          {editing ? null : (
            <SelectField
              id={`${formId}-subject`}
              label="Asignatura"
              name="subject"
              required
              value={values.subjectKey}
              options={subjects.map((item) => ({
                value: item.key,
                label: item.subject,
              }))}
              hint={subjectPlanHint(gradeLabel, selected)}
              error={attempted && !values.subjectKey ? requiredChoiceMessage : undefined}
              onChange={(subjectKey) => {
                const next = subjects.find((item) => item.key === subjectKey)
                patch({
                  subjectKey,
                  hours: String(next ? Math.max(next.requiredHours - next.assignedHours, 1) : ''),
                })
              }}
            />
          )}

          {editing ? (
            <SelectField
              id={`${formId}-teacher`}
              label="Docente"
              name="teacher"
              required
              value={values.teacherValue}
              options={teacherOptions}
              hint={teacherHint}
              error={attempted && !values.teacherValue ? requiredChoiceMessage : undefined}
              onChange={(teacherValue) => patch({ teacherValue })}
            />
          ) : null}

          <div className={styles.hoursBlock}>
            <TextField
              id={`${formId}-hours`}
              label={editing ? 'Horas asignadas' : 'Horas a asignar'}
              name="hours"
              required
              inputMode="numeric"
              placeholder="Ej. 3"
              value={values.hours}
              error={attempted && hoursInvalid ? hoursMessage : undefined}
              onChange={(event) => patch({ hours: event.target.value.replace(/\D/g, '') })}
            />
            {editing && selected ? (
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => adjustHours(hoursValue > selected.requiredHours ? -1 : 1)}
              >
                {hoursValue > selected.requiredHours ? '- 1 hora' : '+ 1 hora'}
              </Button>
            ) : null}
            {editing && hoursCopy ? (
              <p className={`${styles.status} ${styles[hoursCopy.tone]}`}>{hoursCopy.text}</p>
            ) : null}
          </div>

          {editing ? null : (
            <SelectField
              id={`${formId}-teacher`}
              label="Docente"
              name="teacher"
              required
              value={values.teacherValue}
              options={teacherOptions}
              hint={teacherHint}
              error={attempted && !values.teacherValue ? requiredChoiceMessage : undefined}
              onChange={(teacherValue) => patch({ teacherValue })}
            />
          )}

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
          ) : null}

          {editing && selectedTeacher ? (
            <div className={styles.links}>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => navigate(`/dashboard/docentes/${selectedTeacher.id}`)}
              >
                Ver perfil académico
              </Button>
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

      <AssignmentKindModal
        open={kindOpen}
        onClose={() => setKindOpen(false)}
        onSelect={(kind) => {
          patch({ kind })
          setKindOpen(false)
        }}
      />
    </>
  )
}

function labelKind(kind: AssignmentKind) {
  if (kind === 'IDONEIDAD') {
    return 'Idoneidad'
  }

  if (kind === 'TRANSVERSAL') {
    return 'Transversal'
  }

  return 'Principal'
}

function teacherSelectOptions(teachers: Teacher[], row?: AssignmentRow) {
  const options = [
    { value: '', label: 'Seleccionar docente' },
    ...teachers
      .filter((teacher) => teacher.estado === 'ACTIVO')
      .map((teacher) => ({
        value: teacher.id,
        label: formatTeacherName(teacher),
      })),
  ]

  if (
    row?.teacherName &&
    !teachers.some(
      (teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase(),
    )
  ) {
    options.splice(1, 0, {
      value: namedTeacherValue(row.teacherName),
      label: row.teacherName,
    })
  }

  return options
}

function resolveSelectedTeacher(value: string, teachers: Teacher[]) {
  const parsed = parseTeacherSelection(value)
  if (parsed.teacherId) {
    return teachers.find((teacher) => teacher.id === parsed.teacherId)
  }

  if (parsed.teacherName) {
    return teachers.find(
      (teacher) => formatTeacherName(teacher).toLowerCase() === parsed.teacherName.toLowerCase(),
    )
  }

  return undefined
}
