import { labelAvailability, restrictionEffect } from '../../../content/teacherRestrictions'
import type { RestriccionDocenteVista } from '../../../api/restriccionesDocentes'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './RestrictionDetailModal.module.css'

export type RestrictionDetailModalProps = {
  open: boolean
  year: number
  teacherName: string
  bandLabel: string
  restriction: RestriccionDocenteVista | null
  loading?: boolean
  error?: string
  readOnly?: boolean
  yearClosed?: boolean
  onEdit: () => void
  onClose: () => void
}

export function RestrictionDetailModal({
  open,
  year,
  teacherName,
  bandLabel,
  restriction,
  loading = false,
  error = '',
  readOnly = false,
  yearClosed = false,
  onEdit,
  onClose,
}: RestrictionDetailModalProps) {
  const canEdit = !readOnly && Boolean(restriction) && !loading && !error

  return (
    <Modal
      open={open}
      title="Detalle de restricción"
      description={
        yearClosed
          ? `${teacherName} · Año lectivo ${year} — Cerrado · Solo lectura`
          : `${teacherName} · Año lectivo ${year}`
      }
      onClose={onClose}
      footer={
        canEdit ? (
          <div className={styles.actions}>
            <Button variant="primary" size="sm" onClick={onEdit}>
              Editar
            </Button>
            <Button variant="muted" size="sm" onClick={onClose}>
              Cerrar
            </Button>
          </div>
        ) : (
          <div className={styles.readOnlyActions}>
            <Button variant="muted" size="sm" onClick={onClose}>
              Cerrar
            </Button>
          </div>
        )
      }
    >
      <div className={styles.body}>
        {loading ? <p className={styles.effect}>Cargando restricción...</p> : null}
        {error ? (
          <p className={styles.error} role="alert">
            {error}
          </p>
        ) : null}
        {restriction ? (
          <>
            <p className={styles.slot}>
              {restriction.day} · {bandLabel}
            </p>
            <p className={styles.type}>Tipo: {labelAvailability(restriction.status)}</p>
            <Detail label="Motivo" value={restriction.motivo || '—'} />
            <Detail
              label="Documento soporte"
              value={restriction.documentoSoporte ? 'Documento adjunto' : 'Sin adjunto'}
            />
            <Detail
              label="Aprobada por:"
              value={
                restriction.aprobadaPorId
                  ? 'Aprobación registrada'
                  : 'Sin aprobación registrada'
              }
            />
            <p className={styles.effect}>{restrictionEffect(restriction.status)}</p>
          </>
        ) : null}
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
