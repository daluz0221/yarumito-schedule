import {
  conflictDetailFacts,
  labelConflictKind,
  labelConflictSeverity,
  labelConflictStatus,
  type ReviewedConflict,
} from '../../../content/validations'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './ConflictDetailModal.module.css'

export type ConflictDetailModalProps = {
  open: boolean
  conflict: ReviewedConflict | null
  scheduleLabel: string
  detectedAt: string
  onClose: () => void
  onOpenSchedule: () => void
}

export function ConflictDetailModal({
  open,
  conflict,
  scheduleLabel,
  detectedAt,
  onClose,
  onOpenSchedule,
}: ConflictDetailModalProps) {
  if (!conflict) {
    return null
  }

  const warning = conflict.severity === 'ADVERTENCIA'

  return (
    <Modal
      open={open}
      title={labelConflictKind(conflict.kind)}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          <Button variant="muted" size="sm" onClick={onClose}>
            Cerrar
          </Button>
          <Button variant="primary" size="sm" onClick={onOpenSchedule}>
            Corregir en horario
          </Button>
        </div>
      }
    >
      <div className={styles.body}>
        <div className={styles.pills}>
          <Badge tone={warning ? 'warning' : 'danger'}>
            {labelConflictSeverity(conflict.severity)}
          </Badge>
          <Badge>{labelConflictStatus(conflict.status)}</Badge>
        </div>

        <p className={warning ? styles.warning : styles.error}>{conflict.message}</p>

        <dl className={styles.facts}>
          {conflictDetailFacts(conflict, scheduleLabel, detectedAt).map((fact) => (
            <div key={fact.label} className={styles.fact}>
              <dt>{fact.label}</dt>
              <dd>{fact.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </Modal>
  )
}
