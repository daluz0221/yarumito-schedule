import { labelYearStatus, type AcademicYear } from '../../../content/academicYear'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RestrictionYearModal.module.css'

export type RestrictionYearModalProps = {
  open: boolean
  years: AcademicYear[]
  onSelect: (yearId: string) => void
  onClose: () => void
}

function yearChoiceLabel(year: AcademicYear) {
  if (year.status === 'CERRADO') {
    return `${year.year} — Cerrado · Solo lectura`
  }

  return `${year.year} — ${labelYearStatus(year.status)}`
}

function orderedYears(years: AcademicYear[]) {
  const current = years.find((year) => year.current)
  const rest = years.filter((year) => year.id !== current?.id)
  return current ? [current, ...rest] : years
}

export function RestrictionYearModal({ open, years, onSelect, onClose }: RestrictionYearModalProps) {
  const choices = orderedYears(years)

  return (
    <Modal open={open} title="Seleccionar año lectivo" onClose={onClose}>
      <div className={styles.choices}>
        {choices.length === 0 ? (
          <p className={styles.empty}>No hay años lectivos registrados.</p>
        ) : (
          choices.map((year) => (
            <Button
              key={year.id}
              variant="muted"
              size="sm"
              onClick={() => onSelect(year.id)}
            >
              {yearChoiceLabel(year)}
            </Button>
          ))
        )}
        <Button variant="muted" size="sm" onClick={onClose}>
          Cancelar
        </Button>
      </div>
    </Modal>
  )
}
