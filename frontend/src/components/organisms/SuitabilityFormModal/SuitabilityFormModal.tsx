import { useEffect, useState, type FormEvent } from 'react'
import { ApiError, listarAsignaturas } from '../../../api'
import type { AsignaturaResponse } from '../../../api/asignaturas'
import { PENDING_BACKEND_LABEL } from '../../../content/teachers'
import {
  emptyCrearIdoneidadForm,
  formatTituloSoporte,
  idoneidadToForm,
  suitabilityTypeOptions,
  type CrearIdoneidadForm,
  type Idoneidad,
  type ProfessionalTitle,
} from '../../../content/teacherProfile'
import type { SelectOption } from '../../atoms/Select'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './SuitabilityFormModal.module.css'

export type SuitabilityFormModalProps = {
  open: boolean
  mode: 'add' | 'edit'
  item?: Idoneidad
  areaOptions: SelectOption[]
  titles?: ProfessionalTitle[]
  titlesError?: string
  loading?: boolean
  loadError?: string
  submitting?: boolean
  error?: string
  onClose: () => void
  onRetry?: () => void
  onSubmit: (values: CrearIdoneidadForm) => void
}

const pendingOptions: SelectOption[] = [
  { value: '', label: PENDING_BACKEND_LABEL },
]

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function subjectLoadError(cause: unknown) {
  return cause instanceof ApiError
    ? cause.message
    : 'No se pudieron cargar las asignaturas.'
}

function titleOptions(
  titles: ProfessionalTitle[],
  selectedId: string,
  titlesError: string,
): SelectOption[] {
  const options: SelectOption[] = [
    {
      value: '',
      label: 'Sin título de soporte',
    },
    ...titles.map((title) => ({
      value: title.id,
      label: formatTituloSoporte(title),
    })),
  ]

  if (selectedId && !options.some((option) => option.value === selectedId)) {
    options.push({
      value: selectedId,
      label: titlesError ? 'Título actual' : 'Título no disponible',
    })
  }

  return options
}

export function SuitabilityFormModal({
  open,
  mode,
  item,
  areaOptions,
  titles = [],
  titlesError = '',
  loading = false,
  loadError = '',
  submitting = false,
  error = '',
  onClose,
  onRetry,
  onSubmit,
}: SuitabilityFormModalProps) {
  const formStateKey = `${open ? 'open' : 'closed'}:${mode}:${item?.id ?? ''}`
  const initialValues =
    mode === 'edit' && item
      ? idoneidadToForm(item)
      : emptyCrearIdoneidadForm
  const [activeFormStateKey, setActiveFormStateKey] = useState(formStateKey)
  const [values, setValues] = useState<CrearIdoneidadForm>(initialValues)
  const [subjectCatalog, setSubjectCatalog] = useState<{
    areaId: string
    subjects: AsignaturaResponse[]
    error: string
  } | null>(null)
  const [localError, setLocalError] = useState('')
  const isEdit = mode === 'edit'
  const isExceptional = values.tipo === 'EXCEPCIONAL'

  if (activeFormStateKey !== formStateKey) {
    setActiveFormStateKey(formStateKey)
    setLocalError('')
    setValues(initialValues)
  }

  useEffect(() => {
    if (!open || !values.areaId) {
      return
    }

    const controller = new AbortController()
    const areaId = values.areaId

    listarAsignaturas(areaId, controller.signal)
      .then((loaded) => {
        if (controller.signal.aborted) {
          return
        }

        setSubjectCatalog({
          areaId,
          subjects: [...loaded].sort((left, right) =>
            left.nombre.localeCompare(right.nombre, 'es'),
          ),
          error: '',
        })
      })
      .catch((cause: unknown) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setSubjectCatalog({
          areaId,
          subjects: [],
          error: subjectLoadError(cause),
        })
      })

    return () => controller.abort()
  }, [open, values.areaId])

  const subjectCatalogForArea =
    open && subjectCatalog?.areaId === values.areaId ? subjectCatalog : null
  const subjects = subjectCatalogForArea?.subjects ?? []
  const subjectsError = subjectCatalogForArea?.error ?? ''
  const subjectsLoading = Boolean(values.areaId) && subjectCatalogForArea === null

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (submitting || loading || loadError) {
      return
    }

    if (!values.areaId) {
      setLocalError('El área es obligatoria')
      return
    }

    if (!values.vigenteDesde) {
      setLocalError('La fecha de inicio de vigencia es obligatoria')
      return
    }

    if (
      values.vigenteHasta &&
      values.vigenteHasta < values.vigenteDesde
    ) {
      setLocalError(
        'La fecha de finalización no puede ser anterior al inicio de vigencia',
      )
      return
    }

    if (isExceptional && !values.justificacion.trim()) {
      setLocalError('Una idoneidad excepcional requiere justificación')
      return
    }

    setLocalError('')
    onSubmit(values)
  }

  const visibleSubjects = subjects.filter(
    (subject) => subject.activa || subject.id === values.asignaturaId,
  )
  const subjectOptions: SelectOption[] = [
    {
      value: '',
      label: values.areaId ? 'Sin asignatura específica' : 'Seleccione un área',
    },
    ...visibleSubjects.map((subject) => ({
      value: subject.id,
      label: subject.activa ? subject.nombre : `${subject.nombre} (inactiva)`,
    })),
  ]

  if (
    values.asignaturaId &&
    !subjectOptions.some((option) => option.value === values.asignaturaId)
  ) {
    subjectOptions.push({
      value: values.asignaturaId,
      label: subjectsLoading ? 'Cargando asignatura...' : 'Asignatura no disponible',
    })
  }

  const subjectHint = !values.areaId
    ? 'Seleccione un área para listar sus asignaturas.'
    : subjectsLoading
      ? 'Cargando asignaturas...'
      : subjectsError
        ? undefined
        : visibleSubjects.length === 0
          ? 'Esta área no tiene asignaturas activas. La idoneidad puede guardarse para toda el área.'
          : 'Opcional. Sin asignatura, la idoneidad cubre toda el área.'

  return (
    <Modal
      open={open}
      title={
        mode === 'add'
          ? 'Agregar idoneidad académica'
          : 'Editar idoneidad académica'
      }
      description={
        isEdit
          ? 'Actualiza los datos académicos y el periodo de vigencia de la idoneidad.'
          : 'Define el área, la asignatura si aplica, el tipo de idoneidad y, si existe, el título de soporte.'
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId="suitability-form"
          confirmLabel={
            loadError
              ? undefined
              : loading
                ? 'Cargando...'
                : submitting
                  ? 'Guardando...'
                  : mode === 'add'
                    ? 'Guardar idoneidad'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading}
          onCancel={onClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando idoneidad...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : (
      <form
        id="suitability-form"
        className={styles.form}
        noValidate
        onSubmit={handleSubmit}
      >
        <SelectField
          id="suitability-area"
          label="Área"
          required
          value={values.areaId}
          options={
            areaOptions.length > 0
              ? [{ value: '', label: 'Seleccione un área' }, ...areaOptions]
              : pendingOptions
          }
          disabled={areaOptions.length === 0}
          hint={areaOptions.length === 0 ? PENDING_BACKEND_LABEL : undefined}
          onChange={(areaId) =>
            setValues((current) => ({
              ...current,
              areaId,
              asignaturaId: '',
            }))
          }
        />
        <SelectField
          id="suitability-subject"
          label="Asignatura"
          value={values.asignaturaId}
          options={subjectOptions}
          hint={subjectHint}
          error={subjectsError || undefined}
          disabled={!values.areaId || subjectsLoading}
          onChange={(asignaturaId) =>
            setValues((current) => ({ ...current, asignaturaId }))
          }
        />
        <SelectField
          id="suitability-type"
          label="Tipo de idoneidad"
          required
          value={values.tipo}
          options={suitabilityTypeOptions}
          onChange={(tipo) =>
            setValues((current) => ({
              ...current,
              tipo: tipo as CrearIdoneidadForm['tipo'],
              justificacion:
                tipo === 'EXCEPCIONAL' ? current.justificacion : '',
            }))
          }
        />
        <SelectField
          id="suitability-title"
          label="Título profesional de soporte"
          value={values.tituloSoporteId}
          options={titleOptions(titles, values.tituloSoporteId, titlesError)}
          hint={
            titlesError
              ? `${titlesError} Puedes guardar la idoneidad sin cambiar el título.`
              : titles.length === 0
                ? 'Este docente no tiene títulos profesionales. La idoneidad se puede guardar sin título de soporte.'
                : undefined
          }
          disabled={Boolean(titlesError)}
          onChange={(tituloSoporteId) =>
            setValues((current) => ({ ...current, tituloSoporteId }))
          }
        />
        <div className={styles.dates}>
          <TextField
            id="suitability-from"
            label="Vigente desde"
            type="date"
            required
            value={values.vigenteDesde}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                vigenteDesde: event.target.value,
              }))
            }
          />
          <TextField
            id="suitability-to"
            label="Vigente hasta"
            type="date"
            value={values.vigenteHasta}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                vigenteHasta: event.target.value,
              }))
            }
          />
        </div>
        <TextField
          id="suitability-justification"
          label="Justificación"
          required={isExceptional}
          placeholder="Obligatoria solo para idoneidad excepcional"
          value={values.justificacion}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              justificacion: event.target.value,
            }))
          }
        />
        {error || localError ? (
          <p className={styles.error} role="alert">
            {error || localError}
          </p>
        ) : null}
      </form>
      )}
    </Modal>
  )
}
