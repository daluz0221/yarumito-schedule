import { useEffect, useState, type FormEvent } from 'react'
import type { ProfessionalTitle } from '../../../content/teacherProfile'
import { titleLevelOptions } from '../../../content/teacherProfile'
import { Modal } from '../../atoms/Modal'
import { FileField } from '../../molecules/FileField'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './TitleFormModal.module.css'

export type TitleFormModalProps = {
  open: boolean
  mode: 'add' | 'edit'
  title?: ProfessionalTitle
  onClose: () => void
}

const emptyValues = {
  level: '',
  name: '',
  institution: '',
  year: '',
  supportFile: '',
}

export function TitleFormModal({
  open,
  mode,
  title,
  onClose,
}: TitleFormModalProps) {
  const [values, setValues] = useState(emptyValues)

  useEffect(() => {
    if (!open) {
      return
    }

    setValues(
      mode === 'edit' && title
        ? {
            level: title.level,
            name: title.name,
            institution: title.institution,
            year: title.year,
            supportFile: title.supportFile ?? '',
          }
        : emptyValues,
    )
  }, [mode, open, title])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onClose()
  }

  return (
    <Modal
      open={open}
      title={
        mode === 'add' ? 'Agregar título profesional' : 'Editar título profesional'
      }
      description="Registra la formación académica que respalda el perfil profesional del docente."
      onClose={onClose}
      footer={
        <ModalActions
          formId="title-form"
          confirmLabel={mode === 'add' ? 'Guardar título' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id="title-form" className={styles.form} onSubmit={handleSubmit}>
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
      </form>
    </Modal>
  )
}
