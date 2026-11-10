import type { ScheduleAlert } from '../../../content/scheduleConstruction'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import styles from './ScheduleAlertModal.module.css'

export type ScheduleAlertModalProps = {
  alert: ScheduleAlert | null
  onClose: () => void
}

export function ScheduleAlertModal({ alert, onClose }: ScheduleAlertModalProps) {
  const content = alert ? alertCopy(alert) : null

  return (
    <Modal open={Boolean(alert)} title={content?.title ?? 'Aviso'} onClose={onClose}>
      {content ? (
        <div className={styles.panel}>
          <p className={content.blocking ? styles.blocking : styles.warning}>
            <strong>{content.kicker}</strong>
            {content.message}
          </p>
          <Button variant="primary" size="sm" onClick={onClose}>
            Cerrar
          </Button>
        </div>
      ) : null}
    </Modal>
  )
}

function alertCopy(alert: ScheduleAlert) {
  if (alert.kind === 'teacher-busy') {
    return {
      title: 'Docente ocupado',
      kicker: 'ERROR BLOQUEANTE',
      blocking: true,
      message: `${alert.teacherName} ya tiene una clase programada en esta misma franja. No es posible guardar mientras exista el conflicto.`,
    }
  }

  if (alert.kind === 'classroom-busy') {
    return {
      title: 'Aula ocupada',
      kicker: 'ERROR BLOQUEANTE',
      blocking: true,
      message: `${alert.classroom} ya está ocupada en la franja seleccionada. Debe elegirse otra aula o franja.`,
    }
  }

  if (alert.kind === 'teacher-unavailable') {
    return {
      title: 'Docente no disponible',
      kicker: 'ERROR BLOQUEANTE',
      blocking: true,
      message: `${alert.teacherName} no está disponible en esta franja por una restricción registrada. No es posible guardar mientras exista el conflicto.`,
    }
  }

  if (alert.kind === 'slot-taken') {
    return {
      title: 'Franja ocupada',
      kicker: 'ERROR BLOQUEANTE',
      blocking: true,
      message:
        'El grupo ya tiene una clase programada en esta franja. Debe elegirse otro día u otra franja.',
    }
  }

  return {
    title: 'Capacidad del aula',
    kicker: 'ADVERTENCIA',
    blocking: false,
    message:
      'La cantidad de estudiantes del grupo supera la capacidad registrada del aula. La decisión final puede quedar en manos del Rector.',
  }
}
