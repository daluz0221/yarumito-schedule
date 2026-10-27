import { useEffect, useState, type FormEvent } from 'react'
import {
  ApiError,
  etiquetaGrado,
  gradoPorEtiqueta,
  gradoPorId,
  listarDocentes,
  opcionesCatalogoSedes,
  opcionesGradoRequerido,
  planDesdeGrado,
  registrarGrupo,
  sedeVisible,
  useCatalogoGrados,
  useCatalogoSedes,
  type GrupoResponse,
} from '../../../api'
import {
  classroomOptions,
  groupStatusOptions,
  isGroupCodeTaken,
  labelYearStatus,
  type AcademicYear,
  type SchoolGroup,
  type SchoolGroupStatus,
} from '../../../content/academicYear'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
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
  onCreated?: (group: GrupoResponse) => void
}

type FormValues = {
  code: string
  grade: string
  students: string
  directorId: string
  status: SchoolGroupStatus | ''
  classroomId: string
  sedeId: string
}

const createValues: FormValues = {
  code: '',
  grade: '',
  students: '',
  directorId: '',
  status: '',
  classroomId: '',
  sedeId: '',
}

const statusOptions = [
  { value: '', label: 'Seleccione un estado' },
  ...groupStatusOptions,
]

const duplicateCodeMessage =
  'Ya existe un grupo con este código para el año lectivo seleccionado.'

const UUID =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

function esUuid(value: string) {
  return UUID.test(value)
}

function opcionesDirector(docentes: Teacher[] | null, error: string) {
  const sinAsignar = { value: '', label: 'Sin asignar' }

  if (error || !docentes) {
    return [sinAsignar]
  }

  return [
    sinAsignar,
    ...docentes.map((teacher) => ({
      value: teacher.id,
      label: formatTeacherName(teacher),
    })),
  ]
}

function mensajeCrearGrupo(cause: unknown) {
  if (!(cause instanceof ApiError)) {
    return 'No se pudo crear el grupo. Inténtalo de nuevo.'
  }

  if (cause.message && cause.message !== 'No se pudo completar la solicitud') {
    return cause.message
  }

  if (cause.status === 409) {
    return duplicateCodeMessage
  }

  if (cause.status === 404) {
    return 'No se encontró el grado, la sede, el año lectivo o el director indicado.'
  }

  if (cause.status === 400) {
    return 'Revisa los datos del grupo. Hay un campo obligatorio o con formato inválido.'
  }

  if (cause.status === 0) {
    return cause.message
  }

  return 'No se pudo crear el grupo. Inténtalo de nuevo.'
}

export function GroupFormModal({
  open,
  mode,
  year,
  groups,
  group,
  onClose,
  onCreated,
}: GroupFormModalProps) {
  const [values, setValues] = useState<FormValues>(createValues)
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [directores, setDirectores] = useState<Teacher[] | null>(null)
  const [directoresError, setDirectoresError] = useState('')
  const { grados, error: gradosError } = useCatalogoGrados(open)
  const { sedes, error: sedesError } = useCatalogoSedes(open)
  const gradeOptions = opcionesGradoRequerido(grados, gradosError)
  const sedeOptions = opcionesCatalogoSedes(sedes, sedesError)
  const gradoLista = Boolean(grados?.length) && !gradosError
  const sedeLista = Boolean(sedes?.length) && !sedesError
  const gradoSeleccionado = gradoPorId(grados, values.grade)
  const formId = `group-${mode}-form`
  const plan = gradoSeleccionado ? planDesdeGrado(gradoSeleccionado) : null
  const etiquetaSeleccionada = gradoSeleccionado
    ? etiquetaGrado(gradoSeleccionado.nombre)
    : ''
  const directorOptions = opcionesDirector(directores, directoresError)
  const codeTaken =
    mode === 'create' && isGroupCodeTaken(groups, year.id, values.code, group?.id)
  const aulaSinCatalogo = Boolean(values.classroomId) && !esUuid(values.classroomId)
  const creating = mode === 'create'

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  useEffect(() => {
    if (!open) {
      return
    }

    if (mode === 'edit' && group) {
      setValues({
        code: group.code,
        grade: '',
        students: String(group.students),
        directorId: '',
        status: group.status,
        classroomId: '',
        sedeId: group.sedeId ?? '',
      })
      return
    }

    setValues(createValues)
    setAttempted(false)
    setSubmitError('')
  }, [group, mode, open])

  useEffect(() => {
    if (!open) {
      return
    }

    const controller = new AbortController()
    setDirectores(null)
    setDirectoresError('')

    const cargar = async () => {
      const size = 100
      const first = await listarDocentes(
        { estado: 'ACTIVO', page: 0, size },
        controller.signal,
      )
      const docentes = [...first.content]
      const pages = Math.ceil(first.total / size)

      for (let page = 1; page < pages; page += 1) {
        const next = await listarDocentes(
          { estado: 'ACTIVO', page, size },
          controller.signal,
        )
        docentes.push(...next.content)
      }

      return docentes
    }

    cargar()
      .then((docentes) => {
        setDirectores(docentes)
        setDirectoresError('')
      })
      .catch((cause) => {
        if (cause instanceof DOMException && cause.name === 'AbortError') {
          return
        }

        setDirectores([])
        setDirectoresError(
          'No se pudieron cargar los docentes. Puedes crear el grupo sin director.',
        )
      })

    return () => controller.abort()
  }, [open])

  useEffect(() => {
    if (!open || mode !== 'edit' || !group || !grados) {
      return
    }

    const grade = gradoPorEtiqueta(grados, group.grade)?.id ?? ''

    setValues((current) => (current.grade === grade ? current : { ...current, grade }))
  }, [grados, group, mode, open])

  useEffect(() => {
    if (!open || !sedes?.length) {
      return
    }

    const sedeId = sedeVisible(sedes)?.id ?? ''

    setValues((current) =>
      sedes.some((sede) => sede.id === current.sedeId) ? current : { ...current, sedeId },
    )
  }, [open, sedes])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)
    setSubmitError('')

    if (!creating) {
      onClose()
      return
    }

    if (submitting) {
      return
    }

    if (
      codeTaken ||
      aulaSinCatalogo ||
      !gradoLista ||
      !sedeLista ||
      !esUuid(year.id) ||
      !values.code.trim() ||
      !values.grade ||
      !values.sedeId ||
      !values.status
    ) {
      if (!esUuid(year.id)) {
        setSubmitError(
          'El año lectivo seleccionado no está disponible en el servidor. No se puede crear el grupo.',
        )
      }

      return
    }

    if (values.directorId && !esUuid(values.directorId)) {
      setSubmitError('El director seleccionado no es válido. Déjalo en Sin asignar.')
      return
    }

    const payload = {
      codigo: values.code.trim(),
      gradoId: values.grade,
      anioEscolarId: year.id,
      sedeId: values.sedeId,
      activo: values.status === 'ACTIVO',
      ...(values.students !== ''
        ? { cantidadEstudiantes: Number(values.students) }
        : {}),
      ...(values.directorId ? { directorGrupoId: values.directorId } : {}),
    }

    setSubmitting(true)

    try {
      const created = await registrarGrupo(payload)
      onCreated?.(created)
      onClose()
    } catch (cause) {
      setSubmitError(mensajeCrearGrupo(cause))
    } finally {
      setSubmitting(false)
    }
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
      onClose={handleClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={
            submitting ? 'Guardando...' : creating ? 'Guardar grupo' : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting}
          onCancel={handleClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.grid}>
          <TextField
            id={`${formId}-code`}
            label="Código del grupo"
            required
            disabled={mode === 'edit' || submitting}
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
            value={gradoLista ? values.grade : ''}
            options={gradeOptions}
            disabled={!gradoLista || submitting}
            error={
              gradosError ||
              (attempted && creating && gradoLista && !values.grade
                ? 'Seleccione un grado.'
                : undefined)
            }
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
          <SelectField
            id={`${formId}-campus`}
            label="Sede"
            required={creating}
            value={sedeLista ? values.sedeId : ''}
            options={sedeOptions}
            disabled={!sedeLista || submitting}
            error={
              sedesError ||
              (attempted && creating && sedeLista && !values.sedeId
                ? 'Seleccione una sede.'
                : undefined)
            }
            onChange={(sedeId) => setValues((current) => ({ ...current, sedeId }))}
          />
          <TextField
            id={`${formId}-students`}
            label="Cantidad de estudiantes"
            inputMode="numeric"
            disabled={submitting}
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
            disabled={submitting || directores === null}
            error={directoresError || undefined}
            onChange={(directorId) =>
              setValues((current) => ({ ...current, directorId }))
            }
          />
          <SelectField
            id={`${formId}-status`}
            label="Estado"
            required
            disabled={submitting}
            value={values.status}
            options={statusOptions}
            error={
              attempted && creating && !values.status ? 'Seleccione un estado.' : undefined
            }
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
            disabled={submitting}
            value={values.classroomId}
            options={classroomOptions}
            error={
              aulaSinCatalogo
                ? 'El catálogo de aulas aún no está disponible. Déjala en Sin asignar para crear el grupo.'
                : undefined
            }
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

        {submitError ? (
          <p className={styles.error} role="alert">
            {submitError}
          </p>
        ) : null}

        <p className={styles.hint}>
          {mode === 'edit'
            ? 'Tipo e intensidad derivados del grado. Director y aula son opcionales.'
            : gradosError
              ? 'No se pudieron cargar los grados. El grupo no se puede guardar hasta elegir un grado del catálogo.'
              : grados === null
                ? 'Cargando grados del catálogo…'
                : grados.length === 0
                  ? 'No hay grados registrados. El grupo no se puede guardar hasta que exista un grado en el catálogo.'
                  : plan
                    ? `${etiquetaSeleccionada} seleccionado → ${plan.typeLabel} · ${plan.weeklyHours} h. Estado: Activo o Inactivo. Director y aula son opcionales.`
                    : 'Seleccione un grado para ver el tipo y la intensidad que vienen del catálogo. Director y aula son opcionales.'}
        </p>
      </form>
    </Modal>
  )
}
