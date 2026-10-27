import { academicYears, labelYearStatus, type AcademicYear } from '../../../content/academicYear'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RestrictionYearModal.module.css'

export type RestrictionYearModalProps = {
  open: boolean
  onSelect: (yearId: string) => void
  onClose: () => void
}

function yearChoiceLabel(year: AcademicYear) {
  if (year.status === 'CERRADO') {
    return `${year.year} — Cerrado · Solo lectura`
  }

  return `${year.year} — ${labelYearStatus(year.status)}`
}

function orderedYears() {
  const current = academicYears.find((year) => year.current)
  const rest = academicYears.filter((year) => year.id !== current?.id)
  return current ? [current, ...rest] : academicYears
}

export function RestrictionYearModal({ open, onSelect, onClose }: RestrictionYearModalProps) {
  return (
    <Modal open={open} title="Seleccionar año lectivo" onClose={onClose}>
      <div className={styles.choices}>
        {orderedYears().map((year) => (
          <Button
            key={year.id}
            variant="muted"
            size="sm"
            onClick={() => onSelect(year.id)}
          >
            {yearChoiceLabel(year)}
          </Button>
        ))}
        <Button variant="muted" size="sm" onClick={onClose}>
          Cancelar
        </Button>
      </div>
    </Modal>
  )
}
