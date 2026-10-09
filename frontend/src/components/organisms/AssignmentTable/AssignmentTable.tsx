import {
  labelAssignmentStatus,
  type AssignmentRow,
} from '../../../content/academicAssignment'
import { Button } from '../../atoms/Button'
import styles from './AssignmentTable.module.css'

export type AssignmentTableProps = {
  rows: AssignmentRow[]
  emptyMessage: string
  readOnly?: boolean
  onAssign: (row: AssignmentRow) => void
  onEdit: (row: AssignmentRow) => void
  onRemove: (row: AssignmentRow) => void
}

export function AssignmentTable({
  rows,
  emptyMessage,
  readOnly = false,
  onAssign,
  onEdit,
  onRemove,
}: AssignmentTableProps) {
  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Área</th>
              <th>Asignatura</th>
              <th>Horas requeridas</th>
              <th>Horas asignadas</th>
              <th>Docente</th>
              <th>Tipo de asignación</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.key}>
                <td>{row.area}</td>
                <td>{row.subject}</td>
                <td>{row.requiredHours}</td>
                <td>{row.assignedHours}</td>
                <td>{row.teacherName || 'Sin docente'}</td>
                <td>{row.kindLabel}</td>
                <td>
                  <StatusChip status={row.status} />
                </td>
                <td>
                  <RowActions
                    row={row}
                    readOnly={readOnly}
                    onAssign={onAssign}
                    onEdit={onEdit}
                    onRemove={onRemove}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((row) => (
          <li key={row.key} className={styles.card}>
            <div className={styles.cardHeader}>
              <strong>{row.subject}</strong>
              <StatusChip status={row.status} />
            </div>
            <p>
              {row.area} · {row.assignedHours}/{row.requiredHours} h
            </p>
            <p>
              {row.teacherName || 'Sin docente'} · {row.kindLabel}
            </p>
            <RowActions
              row={row}
              readOnly={readOnly}
              onAssign={onAssign}
              onEdit={onEdit}
              onRemove={onRemove}
            />
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </div>
  )
}

function StatusChip({ status }: { status: AssignmentRow['status'] }) {
  return (
    <span className={`${styles.status} ${styles[status.toLowerCase()]}`}>
      {labelAssignmentStatus(status)}
    </span>
  )
}

function RowActions({
  row,
  readOnly,
  onAssign,
  onEdit,
  onRemove,
}: {
  row: AssignmentRow
  readOnly: boolean
  onAssign: (row: AssignmentRow) => void
  onEdit: (row: AssignmentRow) => void
  onRemove: (row: AssignmentRow) => void
}) {
  if (readOnly) {
    return <span className={styles.muted}>Solo consulta</span>
  }

  if (!row.assigned) {
    return (
      <span className={styles.actions}>
        <Button variant="outline" size="sm" onClick={() => onAssign(row)}>
          Asignar docente
        </Button>
      </span>
    )
  }

  return (
    <span className={styles.actions}>
      <Button variant="outline" size="sm" onClick={() => onEdit(row)}>
        Editar
      </Button>
      <Button variant="outline" size="sm" onClick={() => onRemove(row)}>
        Quitar asignación
      </Button>
    </span>
  )
}
