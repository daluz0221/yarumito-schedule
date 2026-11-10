import type { SedeResponse } from '../../../api/sedes'
import { Badge } from '../../atoms/Badge'
import { IconAction } from '../../atoms/IconAction'
import styles from './SedesTable.module.css'

export type SedesTableProps = {
  rows: SedeResponse[]
  emptyMessage: string
  onEdit: (sede: SedeResponse) => void
}

export function SedesTable({ rows, emptyMessage, onEdit }: SedesTableProps) {
  if (rows.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>
  }

  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Código</th>
              <th>Sede</th>
              <th>Dirección</th>
              <th>Principal</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((sede) => (
              <tr key={sede.id}>
                <td className={styles.code}>{sede.codigo}</td>
                <td>{sede.nombre}</td>
                <td>{sede.direccion?.trim() || '—'}</td>
                <td>
                  {sede.esPrincipal ? (
                    <Badge tone="success">Principal</Badge>
                  ) : (
                    '—'
                  )}
                </td>
                <td>
                  <IconAction
                    label={`Editar ${sede.nombre}`}
                    onClick={() => onEdit(sede)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((sede) => (
          <li key={sede.id} className={styles.card}>
            <strong>{sede.nombre}</strong>
            <p>
              {sede.codigo}
              {sede.esPrincipal ? ' · Principal' : ''}
            </p>
            <p>{sede.direccion?.trim() || 'Sin dirección'}</p>
            <IconAction
              label={`Editar ${sede.nombre}`}
              onClick={() => onEdit(sede)}
            />
          </li>
        ))}
      </ul>
    </div>
  )
}
