import {
  labelConflictKind,
  labelConflictSeverity,
  labelConflictStatus,
  type ReviewedConflict,
} from '../../../content/validations'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { SectionHeader } from '../../molecules/SectionHeader'
import styles from './ConflictsTable.module.css'

export type ConflictsTableProps = {
  rows: ReviewedConflict[]
  emptyMessage: string
  onDetail: (conflict: ReviewedConflict) => void
}

export function ConflictsTable({ rows, emptyMessage, onDetail }: ConflictsTableProps) {
  return (
    <Card className={styles.card}>
      <div className={styles.header}>
        <SectionHeader
          title="Conflictos detectados"
          description="Errores y advertencias calculados sobre el horario en construcción."
        />
      </div>

      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Severidad</th>
              <th>Tipo</th>
              <th>Mensaje</th>
              <th>Día / Franja</th>
              <th>Elemento afectado</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>
                  <span className={row.severity === 'ERROR' ? styles.error : styles.warning}>
                    {labelConflictSeverity(row.severity)}
                  </span>
                </td>
                <td>{labelConflictKind(row.kind)}</td>
                <td className={styles.message}>{row.message}</td>
                <td>{row.slotLabel}</td>
                <td>{row.affected}</td>
                <td>{labelConflictStatus(row.status)}</td>
                <td>
                  <Button variant="outline" size="sm" onClick={() => onDetail(row)}>
                    Ver detalle
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((row) => (
          <li key={row.id} className={styles.mobileCard}>
            <div className={styles.mobileHeader}>
              <strong>{labelConflictKind(row.kind)}</strong>
              <span className={row.severity === 'ERROR' ? styles.error : styles.warning}>
                {labelConflictSeverity(row.severity)}
              </span>
            </div>
            <p>{row.message}</p>
            <p>
              {row.slotLabel} · {row.affected}
            </p>
            <p>{labelConflictStatus(row.status)}</p>
            <Button variant="outline" size="sm" onClick={() => onDetail(row)}>
              Ver detalle
            </Button>
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </Card>
  )
}
