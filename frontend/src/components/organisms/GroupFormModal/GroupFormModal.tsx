import { useEffect, useState, type FormEvent } from 'react'
import {
  CAMPUS_LABEL,
  classroomOptions,
  derivedGroupPlan,
  groupGradeOptions,
  groupStatusOptions,
  isGroupCodeTaken,
  labelYearStatus,
  type AcademicYear,
  type SchoolGroup,
  type SchoolGroupStatus,
} from '../../../content/academicYear'
import { formatTeacherName, teachers } from '../../../content/teachers'
import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './GroupFormModal.module.css'

export type GroupFormMode = 'create' | 'edit'

export type GroupFormModalProps = {
  open: boolean
  mode: GroupFormMode
  year: AcademicYear
  groups: SchoolGroup[]
  group?: SchoolGroup
  onClose: () => void
}

type FormValues = {
  code: string
  grade: string
  students: string
  directorId: string
  status: SchoolGroupStatus | ''
  classroomId: string
}

const createValues: FormValues = {
  code: '',
  grade: '',
  students: '',
  directorId: '',
  status: '',
  classroomId: '',
}

const gradeOptions = [
  { value: '', label: 'Seleccione un grado' },
  ...groupGradeOptions,
]

const statusOptions = [
  { value: '', label: 'Seleccione un estado' },
  ...groupStatusOptions,
]

const directorOptions = [
  { value: '', label: 'Sin asignar' },
  ...teachers
    .filter((teacher) => teacher.estado === 'ACTIVO')
    .map((teacher) => ({
      value: teacher.id,
      label: formatTeacherName(teacher),
    })),
]

const duplicateCodeMessage =
  'Ya existe un grupo con este código para el año lectivo seleccionado.'

export function GroupFormModal({
  open,
  mode,
  year,
  groups,
  group,
  onClose,
}: GroupFormModalProps) {
  const [values, setValues] = useState<FormValues>(createValues)
  const formId = `group-${mode}-form`
  const plan = values.grade ? derivedGroupPlan(values.grade) : null
  const codeTaken =
    mode === 'create' && isGroupCodeTaken(groups, year.id, values.code, group?.id)

  useEffect(() => {
    if (!open) {
      return
    }

    if (mode === 'edit' && group) {
      setValues({
        code: group.code,
        grade: group.grade,
        students: String(group.students),
        directorId: '',
        status: group.status,
        classroomId: '',
      })
      return
    }

    setValues(createValues)
  }, [group, mode, open])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (codeTaken || !values.code.trim() || !values.grade || !values.status) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      size="lg"
      title={mode === 'create' ? 'Agregar grupo' : `Editar grupo ${group?.code ?? values.code}`}
      description={
        mode === 'create'
          ? 'Configura un grupo para el año lectivo seleccionado.'
          : 'Actualiza la configuración anual del grupo.'
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={mode === 'create' ? 'Guardar grupo' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <TextField
            id={`${formId}-code`}
            label="Código del grupo"
            required
            disabled={mode === 'edit'}
            className={mode === 'edit' ? styles.locked : undefined}
            value={values.code}
            error={codeTaken ? duplicateCodeMessage : undefined}
            onChange={(event) =>
              setValues((current) => ({ ...current, code: event.target.value }))
            }
          />
          <SelectField
            id={`${formId}-grade`}
            label="Grado"
            required
            value={values.grade}
            options={gradeOptions}
            onChange={(grade) => setValues((current) => ({ ...current, grade }))}
          />
          <TextField
            id={`${formId}-year`}
            label="Año lectivo"
            disabled
            className={styles.locked}
            value={`${year.year} — ${labelYearStatus(year.status)}`}
            onChange={() => undefined}
          />
          <TextField
            id={`${formId}-campus`}
            label="Sede"
            disabled
            className={styles.locked}
            value={CAMPUS_LABEL}
            onChange={() => undefined}
          />
          <TextField
            id={`${formId}-students`}
            label="Cantidad de estudiantes"
            inputMode="numeric"
            value={values.students}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                students: event.target.value.replace(/\D/g, ''),
              }))
            }
          />
          <SelectField
            id={`${formId}-director`}
            label="Director de grupo (opcional)"
            value={values.directorId}
            options={directorOptions}
            onChange={(directorId) =>
              setValues((current) => ({ ...current, directorId }))
            }
          />
          <SelectField
            id={`${formId}-status`}
            label="Estado"
            required
            value={values.status}
            options={statusOptions}
            onChange={(status) =>
              setValues((current) => ({
                ...current,
                status: status as SchoolGroupStatus,
              }))
            }
          />
          <SelectField
            id={`${formId}-classroom`}
            label="Aula habitual (opcional)"
            value={values.classroomId}
            options={classroomOptions}
            onChange={(classroomId) =>
              setValues((current) => ({ ...current, classroomId }))
            }
          />
        </div>

        <div className={styles.derived} aria-live="polite">
          <div>
            <Text variant="caption">Tipo derivado del grado</Text>
            <p className={styles.derivedValue}>{plan?.typeLabel ?? '—'}</p>
          </div>
          <div>
            <Text variant="caption">Intensidad semanal automática</Text>
            <p className={styles.derivedValue}>
              {plan ? `${plan.weeklyHours} h` : '—'}
            </p>
          </div>
        </div>

        <p className={styles.hint}>
          {mode === 'edit'
            ? 'Tipo e intensidad derivados del grado. Director y aula son opcionales.'
            : plan
              ? `${values.grade} seleccionado → ${plan.typeLabel} · ${plan.weeklyHours} h. Para 10° y 11° → Media Técnica · 37 h. Estado: Activo o Inactivo. Director y aula son opcionales.`
              : 'Seleccione un grado para ver el tipo y la intensidad. Para 10° y 11° → Media Técnica · 37 h. De Sexto a Noveno → Bachillerato · 30 h. Director y aula son opcionales.'}
        </p>
      </form>
    </Modal>
  )
}
