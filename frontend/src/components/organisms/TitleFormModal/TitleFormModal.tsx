import { useEffect, useState, type FormEvent } from 'react'
import {
  emptyCrearTituloForm,
  titleLevelOptions,
  type CrearTituloForm,
  type ProfessionalTitle,
} from '../../../content/teacherProfile'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { FileField } from '../../molecules/FileField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './TitleFormModal.module.css'

export type TitleFormModalProps = {
  open: boolean
  mode: 'add' | 'edit'
  title?: ProfessionalTitle
  loading?: boolean
  loadError?: string
  submitting?: boolean
  error?: string
  onClose: () => void
  onRetry?: () => void
  onSubmit?: (values: CrearTituloForm) => void
}

export function TitleFormModal({
  open,
  mode,
  title,
  loading = false,
  loadError = '',
  submitting = false,
  error = '',
  onClose,
  onRetry,
  onSubmit,
}: TitleFormModalProps) {
  const [values, setValues] = useState<CrearTituloForm>(emptyCrearTituloForm)
  const [localError, setLocalError] = useState('')

  useEffect(() => {
    if (!open) {
      return
    }

    setLocalError('')
    setValues(
      mode === 'edit' && title
        ? {
            level: title.level,
            name: title.name,
            institution: title.institution,
            year: title.year,
            supportFile: title.supportFile ?? '',
          }
        : emptyCrearTituloForm,
    )
  }, [mode, open, title])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (submitting || loading || loadError) {
      return
    }

    if (!onSubmit) {
      onClose()
      return
    }

    if (!values.level) {
      setLocalError('El nivel del título es obligatorio')
      return
    }

    if (!values.name.trim()) {
      setLocalError('El nombre del título es obligatorio')
      return
    }

    const year = values.year.trim()

    if (year && !/^\d{4}$/.test(year)) {
      setLocalError('El año de graduación debe tener 4 dígitos')
      return
    }

    if (year) {
      const parsedYear = Number(year)
      const maxYear = new Date().getFullYear() + 1

      if (parsedYear < 1900 || parsedYear > maxYear) {
        setLocalError(`El año de graduación debe estar entre 1900 y ${maxYear}`)
        return
      }
    }

    setLocalError('')
    onSubmit(values)
  }

  return (
    <Modal
      open={open}
      title={
        mode === 'add' ? 'Agregar título profesional' : 'Editar título profesional'
      }
      description={
        mode === 'add'
          ? 'Registra la formación académica que respalda el perfil profesional del docente.'
          : 'Corrige los datos del título profesional registrado para este docente.'
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId="title-form"
          confirmLabel={
            loadError
              ? undefined
              : loading
                ? 'Cargando...'
                : submitting
                  ? 'Guardando...'
                  : mode === 'add'
                    ? 'Guardar título'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading}
          onCancel={onClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando título profesional...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : (
      <form
        id="title-form"
        className={styles.form}
        noValidate
        onSubmit={handleSubmit}
      >
        <SelectField
          id="title-level"
          label="Nivel"
          required
          value={values.level}
          options={titleLevelOptions}
          hint="Opciones: Normalista, Licenciatura, Profesional, Especialización, Maestría y Doctorado."
          onChange={(level) => setValues((current) => ({ ...current, level }))}
        />
        <TextField
          id="title-name"
          label="Nombre del título"
          required
          placeholder="Ej. Licenciatura en Matemáticas"
          value={values.name}
          onChange={(event) =>
            setValues((current) => ({ ...current, name: event.target.value }))
          }
        />
        <TextField
          id="title-institution"
          label="Institución"
          placeholder="Ej. Universidad de Antioquia"
          value={values.institution}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              institution: event.target.value,
            }))
          }
        />
        <TextField
          id="title-year"
          label="Año de graduación"
          placeholder="Ej. 2018"
          inputMode="numeric"
          value={values.year}
          onChange={(event) =>
            setValues((current) => ({ ...current, year: event.target.value }))
          }
        />
        <FileField
          id="title-support"
          label="Soporte académico"
          fileName={values.supportFile}
          hint="Adjunta el documento que respalda el título profesional, si se encuentra disponible."
          onFileChange={(supportFile) =>
            setValues((current) => ({ ...current, supportFile }))
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
