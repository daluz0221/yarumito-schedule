import { publicationCopy, type PublicationRecord, type PublicationView } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './PublishSuccessModal.module.css'

export type PublishSuccessModalProps = {
  open: boolean
  view: PublicationView
  record: PublicationRecord | null
  onClose: () => void
  onSeeSchedule: () => void
}

export function PublishSuccessModal({
  open,
  view,
  record,
  onClose,
  onSeeSchedule,
}: PublishSuccessModalProps) {
  return (
    <Modal
      open={open}
      title={publicationCopy.successTitle}
      onClose={onClose}
      footer={
        <div className={styles.actions}>
          <Button variant="muted" size="sm" onClick={onSeeSchedule}>
            {publicationCopy.successSeeSchedule}
          </Button>
          <Button variant="primary" size="sm" onClick={onClose}>
            {publicationCopy.successClose}
          </Button>
        </div>
      }
    >
      <div className={styles.body}>
        <div className={styles.pills}>
          <Badge tone="success">PUBLICADO</Badge>
        </div>
        <p className={styles.lead}>{publicationCopy.successLead(view.version, view.scheduleName)}</p>
        <dl className={styles.facts}>
          <Fact label="Estado" value="PUBLICADO" />
          <Fact label="Fecha de publicación" value={record?.at ?? '—'} />
          <Fact label="Publicado por" value={record?.actor ?? view.author} />
        </dl>
      </div>
    </Modal>
  )
}

function Fact({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.fact}>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  )
}
