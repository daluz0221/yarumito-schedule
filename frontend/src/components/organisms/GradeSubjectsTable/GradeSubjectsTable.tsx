import type { StudyPlanEntry } from '../../../content/studyPlan'
import { TextLink } from '../../atoms/TextLink'
import styles from './GradeSubjectsTable.module.css'

export type GradeSubjectsTableProps = {
  rows: StudyPlanEntry[]
  emptyMessage: string
  onEdit: (entry: StudyPlanEntry) => void
  onRemove: (entry: StudyPlanEntry) => void
}

export function GradeSubjectsTable({
  rows,
  emptyMessage,
  onEdit,
  onRemove,
}: GradeSubjectsTableProps) {
  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Área</th>
              <th>Asignatura</th>
              <th>Horas semanales</th>
              <th>Turno</th>
              <th>Observación</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((entry) => (
              <tr key={entry.id}>
                <td>{entry.area}</td>
                <td>{entry.subject}</td>
                <td>{entry.weeklyHours}</td>
                <td>{entry.shift}</td>
                <td>{entry.note ?? '—'}</td>
                <td>
                  <EntryActions entry={entry} onEdit={onEdit} onRemove={onRemove} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((entry) => (
          <li key={entry.id} className={styles.card}>
            <div className={styles.cardHeader}>
              <strong>{entry.subject}</strong>
              <span>{entry.weeklyHours} h</span>
            </div>
            <p>
              {entry.area} · {entry.shift}
            </p>
            <p>{entry.note ?? '—'}</p>
            <EntryActions entry={entry} onEdit={onEdit} onRemove={onRemove} />
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </div>
  )
}

function EntryActions({
  entry,
  onEdit,
  onRemove,
}: {
  entry: StudyPlanEntry
  onEdit: (entry: StudyPlanEntry) => void
  onRemove: (entry: StudyPlanEntry) => void
}) {
  return (
    <span className={styles.actions}>
      <TextLink onClick={() => onEdit(entry)}>Editar</TextLink>
      <TextLink onClick={() => onRemove(entry)}>Quitar del plan</TextLink>
    </span>
  )
}
