import { useEffect, useState, type FormEvent } from 'react'
import {
  academicYearStatusOptions,
  suggestedAcademicYear,
  type AcademicYear,
  type AcademicYearStatus,
} from '../../../content/academicYear'
import { Modal } from '../../atoms/Modal'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'
import styles from './AcademicYearFormModal.module.css'

export type AcademicYearFormMode = 'create' | 'edit'

export type AcademicYearFormModalProps = {
  open: boolean
  mode: AcademicYearFormMode
  years: AcademicYear[]
  year?: AcademicYear
  onClose: () => void
}

type FormValues = {
  year: string
  startDate: string
  endDate: string
  status: AcademicYearStatus
}

const hints: Record<AcademicYearFormMode, string> = {
  create:
    'El año podrá activarse cuando la configuración académica necesaria se encuentre preparada.',
  edit: 'El estado actual se conserva; esta acción modifica únicamente la información del período.',
}

export function AcademicYearFormModal({
  open,
  mode,
  years,
  year,
  onClose,
}: AcademicYearFormModalProps) {
  const [values, setValues] = useState<FormValues>(() =>
    suggestedAcademicYear(years),
  )
  const formId = `academic-year-${mode}-form`

  useEffect(() => {
    if (!open) {
      return
    }

    if (mode === 'edit' && year) {
      setValues({
        year: String(year.year),
        startDate: year.startDate,
        endDate: year.endDate,
        status: year.status,
      })
      return
    }

    setValues(suggestedAcademicYear(years))
  }, [mode, open, year, years])

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onClose()
  }

  return (
    <Modal
      open={open}
      title={
        mode === 'create'
          ? 'Crear nuevo año lectivo'
          : `Editar año lectivo ${year?.year ?? values.year}`
      }
      description={
        mode === 'create'
          ? 'Registra el período y sus fechas principales.'
          : 'Actualiza las fechas del período seleccionado.'
      }
      onClose={onClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={mode === 'create' ? 'Crear año lectivo' : 'Guardar cambios'}
          confirmType="submit"
          onCancel={onClose}
        />
      }
    >
      <form id={formId} className={styles.form} onSubmit={handleSubmit}>
        <TextField
          id={`${formId}-year`}
          label="Año"
          required
          disabled
          className={styles.locked}
          value={values.year}
          onChange={() => undefined}
        />
        <div className={styles.dates}>
          <TextField
            id={`${formId}-start`}
            label="Fecha de inicio"
            required
            value={values.startDate}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                startDate: event.target.value,
              }))
            }
          />
          <TextField
            id={`${formId}-end`}
            label="Fecha de finalización"
            required
            value={values.endDate}
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                endDate: event.target.value,
              }))
            }
          />
        </div>
        <SelectField
          id={`${formId}-status`}
          label="Estado"
          value={values.status}
          options={academicYearStatusOptions}
          hint={hints[mode]}
          onChange={(status) =>
            setValues((current) => ({
              ...current,
              status: status as AcademicYearStatus,
            }))
          }
        />
      </form>
    </Modal>
  )
}
