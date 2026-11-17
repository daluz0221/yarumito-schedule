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
  resolving?: boolean
  resolveError?: string
  onClose: () => void
  onOpenSchedule: () => void
  onResolve?: () => void
}

export function ConflictDetailModal({
  open,
  conflict,
  scheduleLabel,
  detectedAt,
  resolving = false,
  resolveError = '',
  onClose,
  onOpenSchedule,
  onResolve,
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
          {conflict.status === 'PENDIENTE' && onResolve ? (
            <Button variant="outline" size="sm" disabled={resolving} onClick={onResolve}>
              {resolving ? 'Guardando…' : 'Marcar como resuelto'}
            </Button>
          ) : null}
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

        {resolveError ? (
          <p className={styles.resolveError} role="alert">
            {resolveError}
          </p>
        ) : null}
      </div>
    </Modal>
  )
}
