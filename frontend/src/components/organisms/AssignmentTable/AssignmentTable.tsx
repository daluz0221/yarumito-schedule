import type { ReactNode } from 'react'
import {
  labelAssignmentStatus,
  type AssignmentRow,
} from '../../../content/academicAssignment'
import { rutaHistorialEntidad } from '../../../content/auditoria'
import { Button } from '../../atoms/Button'
import { IconAction, IconActions } from '../../atoms/IconAction'
import { TextLink } from '../../atoms/TextLink'
import styles from './AssignmentTable.module.css'

export type AssignmentTableProps = {
  rows: AssignmentRow[]
  emptyMessage: string
  readOnly?: boolean
  confirmingId?: string | null
  editableAssigned?: boolean
  groupId?: string
  onAssign: (row: AssignmentRow) => void
  onEdit: (row: AssignmentRow) => void
  onRemove: (row: AssignmentRow) => void
  onConfirm?: (row: AssignmentRow) => void
}

export function AssignmentTable({
  rows,
  emptyMessage,
  readOnly = false,
  confirmingId = null,
  editableAssigned = true,
  groupId,
  onAssign,
  onEdit,
  onRemove,
  onConfirm,
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
                  <StatusChip row={row} />
                </td>
                <td>
                  <RowActions
                    row={row}
                    readOnly={readOnly}
                    confirmingId={confirmingId}
                    editableAssigned={editableAssigned}
                    groupId={groupId}
                    onAssign={onAssign}
                    onEdit={onEdit}
                    onRemove={onRemove}
                    onConfirm={onConfirm}
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
              <StatusChip row={row} />
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
              confirmingId={confirmingId}
              editableAssigned={editableAssigned}
              groupId={groupId}
              onAssign={onAssign}
              onEdit={onEdit}
              onRemove={onRemove}
              onConfirm={onConfirm}
            />
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </div>
  )
}

function StatusChip({ row }: { row: AssignmentRow }) {
  const persistence =
    row.estado === 'BORRADOR' ? ' · Borrador' : row.estado === 'CONFIRMADA' ? ' · Confirmada' : ''

  return (
    <span className={`${styles.status} ${styles[row.status.toLowerCase()]}`}>
      {labelAssignmentStatus(row.status)}
      {persistence}
    </span>
  )
}

function HistorialAsignacion({ row, groupId }: { row: AssignmentRow; groupId?: string }) {
  if (!row.assignmentId || !groupId) {
    return null
  }

  return (
    <TextLink
      className={styles.history}
      to={rutaHistorialEntidad('AsignacionAcademica', row.assignmentId, groupId)}
    >
      Ver historial
    </TextLink>
  )
}

function RowActions({
  row,
  readOnly,
  confirmingId,
  editableAssigned,
  groupId,
  onAssign,
  onEdit,
  onRemove,
  onConfirm,
}: {
  row: AssignmentRow
  readOnly: boolean
  confirmingId: string | null
  editableAssigned: boolean
  groupId?: string
  onAssign: (row: AssignmentRow) => void
  onEdit: (row: AssignmentRow) => void
  onRemove: (row: AssignmentRow) => void
  onConfirm?: (row: AssignmentRow) => void
}) {
  const historial = <HistorialAsignacion row={row} groupId={groupId} />
  let accion: ReactNode = null

  if (readOnly) {
    accion = <span className={styles.muted}>Solo consulta</span>
  } else if (row.estado === 'CONFIRMADA') {
    accion = <span className={styles.muted}>Confirmada</span>
  } else if (row.estado === 'BORRADOR' && onConfirm) {
    const pending = confirmingId === row.assignmentId

    accion = (
      <Button
        variant="outline"
        size="sm"
        disabled={pending || Boolean(confirmingId)}
        onClick={() => onConfirm(row)}
      >
        {pending ? 'Confirmando…' : 'Confirmar'}
      </Button>
    )
  } else if (!row.assigned) {
    accion = (
      <Button variant="outline" size="sm" onClick={() => onAssign(row)}>
        Asignar docente
      </Button>
    )
  } else if (!editableAssigned) {
    accion = <span className={styles.muted}>Asignada</span>
  } else {
    const subject = row.subject || 'asignación'

    accion = (
      <IconActions>
        <IconAction label={`Editar ${subject}`} onClick={() => onEdit(row)} />
        <IconAction
          tone="danger"
          label={`Quitar asignación de ${subject}`}
          onClick={() => onRemove(row)}
        />
      </IconActions>
    )
  }

  if (!accion && !historial) {
    return null
  }

  return (
    <span className={styles.actions}>
      {accion}
      {historial}
    </span>
  )
}
