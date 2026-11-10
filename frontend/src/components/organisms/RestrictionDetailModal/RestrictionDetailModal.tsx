import {
  labelAvailability,
  restrictionEffect,
  timeBandLabel,
  type TeacherRestriction,
} from '../../../content/teacherRestrictions'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RestrictionDetailModal.module.css'

export type RestrictionDetailModalProps = {
  open: boolean
  year: number
  teacherName: string
  restriction: TeacherRestriction
  readOnly?: boolean
  onEdit: () => void
  onRemove: () => void
  onClose: () => void
}

export function RestrictionDetailModal({
  open,
  year,
  teacherName,
  restriction,
  readOnly = false,
  onEdit,
  onRemove,
  onClose,
}: RestrictionDetailModalProps) {
  return (
    <Modal
      open={open}
      title="Detalle de restricción"
      description={
        readOnly
          ? `${teacherName} · Año lectivo ${year} — Cerrado · Solo lectura`
          : `${teacherName} · Año lectivo ${year}`
      }
      onClose={onClose}
      footer={
        readOnly ? (
          <div className={styles.readOnlyActions}>
            <Button variant="muted" size="sm" onClick={onClose}>
              Cerrar
            </Button>
          </div>
        ) : (
          <div className={styles.actions}>
            <Button variant="primary" size="sm" onClick={onEdit}>
              Editar
            </Button>
            <Button variant="outline" size="sm" onClick={onRemove}>
              Quitar restricción
            </Button>
            <Button variant="muted" size="sm" onClick={onClose}>
              Cerrar
            </Button>
          </div>
        )
      }
    >
      <div className={styles.body}>
        <p className={styles.slot}>
          {restriction.day} · {timeBandLabel(restriction.bandId)}
        </p>
        <p className={styles.type}>Tipo: {labelAvailability(restriction.status)}</p>
        <Detail label="Motivo" value={restriction.motivo || '—'} />
        <Detail label="Documento soporte" value={restriction.fileName ?? 'Sin adjunto'} />
        <Detail
          label="Aprobada por:"
          value={restriction.approvedBy ?? 'Sin aprobación registrada'}
        />
        <p className={styles.effect}>{restrictionEffect(restriction.status)}</p>
      </div>
    </Modal>
  )
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.detail}>
      <p>{label}</p>
      <strong>{value}</strong>
    </div>
  )
}
