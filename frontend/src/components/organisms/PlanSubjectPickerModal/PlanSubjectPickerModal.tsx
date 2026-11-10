import type { AssignmentRow } from '../../../content/academicAssignment'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './PlanSubjectPickerModal.module.css'

export type PlanSubjectPickerModalProps = {
  open: boolean
  year: number
  groupCode: string
  gradeLabel: string
  subjects: AssignmentRow[]
  onSelect: (row: AssignmentRow) => void
  onClose: () => void
}

export function PlanSubjectPickerModal({
  open,
  year,
  groupCode,
  gradeLabel,
  subjects,
  onSelect,
  onClose,
}: PlanSubjectPickerModalProps) {
  return (
    <Modal
      open={open}
      title="Asignaturas del plan de estudios"
      description={`${year} · Grupo ${groupCode} · Grado ${gradeLabel}`}
      onClose={onClose}
      footer={
        <Button variant="muted" size="sm" onClick={onClose}>
          Cancelar
        </Button>
      }
    >
      <div className={styles.choices}>
        {subjects.map((subject) => (
          <Button
            key={subject.key}
            variant="outline"
            size="sm"
            onClick={() => onSelect(subject)}
          >
            {subject.subject} · {subject.requiredHours} h requeridas
          </Button>
        ))}
      </div>
    </Modal>
  )
}
