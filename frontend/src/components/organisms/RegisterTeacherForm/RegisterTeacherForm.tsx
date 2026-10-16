import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  actualizarDocente,
  ApiError,
  cambiarEstadoDocente,
  listarAreas,
  registrarDocente,
  toAreaSelectOptions,
} from '../../../api'
import {
  emptyCrearDocenteForm,
  hintCambioEstado,
  opcionesEstadoDocente,
  sanitizeDocumentNumber,
  toActualizarDocenteRequest,
  toCrearDocenteRequest,
  validateActualizarDocenteForm,
  validateCrearDocenteForm,
  type CrearDocenteForm,
  type CrearDocenteFormErrors,
  type TeacherStatus,
} from '../../../content/teachers'
import type { SelectOption } from '../../atoms/Select'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TeacherInstitutionalSection } from '../TeacherInstitutionalSection'
import { TeacherPersonalSection } from '../TeacherPersonalSection'
import { TeacherStatusSection } from '../TeacherStatusSection'
import styles from './RegisterTeacherForm.module.css'

const placeholderAreaOptions: SelectOption[] = [
  { value: '', label: 'Seleccione un área' },
]

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

const MENSAJE_GENERICO = 'No se pudo completar la solicitud'

function mensajeErrorEstado(cause: unknown) {
  if (!(cause instanceof ApiError)) {
    return 'No se pudo cambiar el estado del docente. Inténtalo de nuevo.'
  }

  if (cause.status === 409) {
    return cause.message === MENSAJE_GENERICO
      ? 'No se permite ese cambio de estado.'
      : cause.message
  }

  if (cause.status === 404) {
    return cause.message === MENSAJE_GENERICO
      ? 'El docente no existe.'
      : cause.message
  }

  if (cause.status === 400) {
    return 'El estado indicado no es válido.'
  }

  return cause.message
}

function mensajeErrorActualizacion(cause: unknown) {
  if (cause instanceof ApiError) {
    return cause.message
  }

  return 'No se pudo actualizar el docente. Inténtalo de nuevo.'
}

export type TeacherFormMode = 'create' | 'edit'

export type RegisterTeacherFormProps = {
  mode?: TeacherFormMode
  teacherId?: string
  initialValues?: CrearDocenteForm
}

export function RegisterTeacherForm({
  mode = 'create',
  teacherId,
  initialValues,
}: RegisterTeacherFormProps) {
  const navigate = useNavigate()
  const isEdit = mode === 'edit'
  const [values, setValues] = useState<CrearDocenteForm>(
    initialValues ?? emptyCrearDocenteForm,
  )
  const [areaOptions, setAreaOptions] = useState<SelectOption[]>(
    placeholderAreaOptions,
  )
  const [areasLoading, setAreasLoading] = useState(true)
  const [areasError, setAreasError] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<CrearDocenteFormErrors>({})
  const [submitting, setSubmitting] = useState(false)
  const [estadoGuardado, setEstadoGuardado] = useState<TeacherStatus>(
    initialValues?.estado ?? 'ACTIVO',
  )

  useEffect(() => {
    const controller = new AbortController()

    setAreasLoading(true)
    setAreasError('')

    listarAreas(controller.signal)
      .then((areas) => {
        setAreaOptions(toAreaSelectOptions(areas))
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setAreaOptions(placeholderAreaOptions)
        setAreasError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de áreas.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setAreasLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  const updateField = (field: keyof CrearDocenteForm, value: string) => {
    setValues((current) => {
      const next = { ...current, [field]: value }

      if (field === 'tipoDocumento') {
        next.numeroDocumento = sanitizeDocumentNumber(value, current.numeroDocumento)
      }

      return next
    })
    setFieldErrors((current) => {
      if (!current[field]) {
        return current
      }

      const next = { ...current }
      delete next[field]
      return next
    })
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (areasLoading || areasError || submitting) {
      return
    }

    const nextFieldErrors = isEdit
      ? validateActualizarDocenteForm(values)
      : validateCrearDocenteForm(values)
    setFieldErrors(nextFieldErrors)

    if (Object.keys(nextFieldErrors).length > 0) {
      setError('Revisa los campos marcados e inténtalo de nuevo.')
      return
    }

    setError('')

    const estadoCambio = isEdit && values.estado !== estadoGuardado

    try {
      if (isEdit) {
        if (!teacherId) {
          setError('No se encontró el docente a actualizar.')
          return
        }

        const payload = toActualizarDocenteRequest(values)
        setSubmitting(true)

        if (estadoCambio) {
          try {
            await cambiarEstadoDocente(teacherId, values.estado)
            setEstadoGuardado(values.estado)
          } catch (cause) {
            setError(mensajeErrorEstado(cause))
            return
          }
        }

        try {
          await actualizarDocente(teacherId, payload)
        } catch (cause) {
          const detalle = mensajeErrorActualizacion(cause)
          setError(
            estadoCambio
              ? `El estado se actualizó, pero no se pudieron guardar los demás datos. ${detalle}`
              : detalle,
          )
          return
        }
      } else {
        const payload = toCrearDocenteRequest(values)
        setSubmitting(true)
        await registrarDocente(payload)
      }

      navigate('/dashboard/docentes')
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo registrar el docente. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <Card padding="lg" className={styles.card}>
        <Text variant="caption" className={styles.notice}>
          Los campos marcados con * son obligatorios.
        </Text>

        <TeacherPersonalSection
          values={values}
          errors={fieldErrors}
          onChange={updateField}
        />
        <TeacherInstitutionalSection
          values={values}
          areaOptions={areaOptions}
          areaHint={areasError || (areasLoading ? 'Cargando áreas...' : undefined)}
          areasDisabled={areasLoading || Boolean(areasError) || submitting}
          errors={fieldErrors}
          onChange={updateField}
        />
        <TeacherStatusSection
          value={values.estado}
          options={isEdit ? opcionesEstadoDocente(estadoGuardado) : undefined}
          hint={isEdit ? hintCambioEstado(estadoGuardado) : undefined}
          error={fieldErrors.estado}
          disabled={submitting || (isEdit && estadoGuardado === 'RETIRADO')}
          onChange={(estado) => updateField('estado', estado)}
        />

        {error ? (
          <p className={styles.error} role="alert">
            {error}
          </p>
        ) : null}

        <div className={styles.actions}>
          <Button
            variant="muted"
            size="sm"
            disabled={submitting}
            onClick={() => navigate('/dashboard/docentes')}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="primary"
            size="sm"
            disabled={submitting || areasLoading || Boolean(areasError)}
          >
            {submitting
              ? 'Guardando...'
              : isEdit
                ? 'Guardar cambios'
                : 'Guardar docente'}
          </Button>
        </div>
      </Card>
    </form>
  )
}
