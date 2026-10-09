import {
  assignmentKindDetails,
  assignmentKindModalHint,
  assignmentKindModalNote,
  type CurricularKind,
} from '../../../content/academicAssignment'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import styles from './AssignmentKindModal.module.css'

export type AssignmentKindModalProps = {
  open: boolean
  onSelect: (kind: CurricularKind) => void
  onClose: () => void
}

export function AssignmentKindModal({
  open,
  onSelect,
  onClose,
}: AssignmentKindModalProps) {
  return (
    <Modal
      open={open}
      title="Tipo de asignación curricular"
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          {assignmentKindDetails.map((item) => (
            <Button
              key={item.value}
              variant="outline"
              size="sm"
              onClick={() => onSelect(item.value)}
            >
              {item.label.split(' · ')[0]}
            </Button>
          ))}
        </div>
      }
    >
      <div className={styles.list}>
        {assignmentKindDetails.map((item) => (
          <article key={item.value} className={styles.item}>
            <Text as="h3" variant="sectionTitle">
              {item.label}
            </Text>
            <Text variant="body">{item.description}</Text>
          </article>
        ))}
      </div>
      <p className={styles.note}>{assignmentKindModalNote}</p>
      <Button variant="muted" size="sm" onClick={onClose}>
        Cerrar
      </Button>
      <p className={styles.hint}>{assignmentKindModalHint}</p>
    </Modal>
  )
}
