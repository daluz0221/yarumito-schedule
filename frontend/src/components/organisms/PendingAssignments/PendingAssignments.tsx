import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import type { PendingPlacement } from '../../../content/scheduleConstruction'
import styles from './PendingAssignments.module.css'

export type PendingAssignmentsProps = {
  items: PendingPlacement[]
  readOnly: boolean
  onProgram: (subjectKey: string) => void
}

export function PendingAssignments({
  items,
  readOnly,
  onProgram,
}: PendingAssignmentsProps) {
  return (
    <Card className={styles.card}>
      <div className={styles.heading}>
        <Text as="h3" variant="sectionTitle">
          Asignaciones pendientes
        </Text>
        <Text variant="body">Horas aún no ubicadas en la rejilla.</Text>
      </div>

      {items.length === 0 ? (
        <p className={styles.empty}>No hay horas pendientes por ubicar en este grupo.</p>
      ) : (
        <ul className={styles.list}>
          {items.map((item) => (
            <li key={item.key} className={styles.item}>
              <p className={styles.subject}>{item.subject}</p>
              <p className={styles.teacher}>{item.teacherName}</p>
              <p className={styles.hours}>Pendiente: {item.pendingHours} h</p>
              <Button
                variant="primary"
                size="sm"
                fullWidth
                disabled={readOnly}
                onClick={() => onProgram(item.key)}
              >
                Programar
              </Button>
            </li>
          ))}
        </ul>
      )}

      <Text variant="caption">
        Las asignaciones provienen de la asignación académica. Aquí solo se ubican en día, franja y aula.
      </Text>
    </Card>
  )
}
