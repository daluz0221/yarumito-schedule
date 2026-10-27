import type { RestrictionKind } from '../../../content/teacherRestrictions'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RestrictionTypeModal.module.css'

export type RestrictionTypeModalProps = {
  open: boolean
  onSelect: (tipo: RestrictionKind) => void
  onClose: () => void
}

const choices: { tipo: RestrictionKind; label: string }[] = [
  { tipo: 'NO_DISPONIBLE', label: 'No disponible' },
  { tipo: 'PREFERENCIA', label: 'Preferencia' },
]

export function RestrictionTypeModal({ open, onSelect, onClose }: RestrictionTypeModalProps) {
  return (
    <Modal open={open} title="Tipo de restricción" onClose={onClose}>
      <div className={styles.choices}>
        {choices.map((choice) => (
          <Button
            key={choice.tipo}
            variant="muted"
            size="sm"
            onClick={() => onSelect(choice.tipo)}
          >
            {choice.label}
          </Button>
        ))}
        <Button variant="muted" size="sm" onClick={onClose}>
          Cancelar
        </Button>
      </div>
    </Modal>
  )
}
