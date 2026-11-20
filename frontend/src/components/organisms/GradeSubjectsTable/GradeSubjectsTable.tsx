import type { StudyPlanEntry } from '../../../content/studyPlan'
import { IconAction, IconActions } from '../../atoms/IconAction'
import styles from './GradeSubjectsTable.module.css'

export type GradeSubjectsTableProps = {
  rows: StudyPlanEntry[]
  emptyMessage: string
  onEdit: (entry: StudyPlanEntry) => void
  onRemove: (entry: StudyPlanEntry) => void
  mode?: 'active' | 'retired'
  onRestore?: (entry: StudyPlanEntry) => void
}

export function GradeSubjectsTable({
  rows,
  emptyMessage,
  onEdit,
  onRemove,
  mode = 'active',
  onRestore,
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
                  <EntryActions
            entry={entry}
            mode={mode}
            onEdit={onEdit}
            onRemove={onRemove}
            onRestore={onRestore}
          />
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
            <EntryActions
            entry={entry}
            mode={mode}
            onEdit={onEdit}
            onRemove={onRemove}
            onRestore={onRestore}
          />
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </div>
  )
}

function EntryActions({
  entry,
  mode,
  onEdit,
  onRemove,
  onRestore,
}: {
  entry: StudyPlanEntry
  mode: 'active' | 'retired'
  onEdit: (entry: StudyPlanEntry) => void
  onRemove: (entry: StudyPlanEntry) => void
  onRestore?: (entry: StudyPlanEntry) => void
}) {
  if (mode === 'retired') {
    return onRestore ? (
      <IconActions>
        <IconAction
          tone="restore"
          label={`Reactivar ${entry.subject} en el plan`}
          onClick={() => onRestore(entry)}
        />
      </IconActions>
    ) : null
  }

  return (
    <IconActions>
      <IconAction label={`Editar ${entry.subject}`} onClick={() => onEdit(entry)} />
      <IconAction
        tone="danger"
        label={`Quitar ${entry.subject} del plan`}
        onClick={() => onRemove(entry)}
      />
    </IconActions>
  )
}
