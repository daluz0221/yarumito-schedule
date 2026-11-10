import { etiquetaTipoAula, type AulaResponse } from '../../../api/aulas'
import { Badge } from '../../atoms/Badge'
import { IconAction, IconActions } from '../../atoms/IconAction'
import { TextLink } from '../../atoms/TextLink'
import styles from './AulasTable.module.css'

export type AulasTableProps = {
  rows: AulaResponse[]
  emptyMessage: string
  onEdit: (aula: AulaResponse) => void
  onVersion: (aula: AulaResponse) => void
  onDelete: (aula: AulaResponse) => void
}

function capacidad(aula: AulaResponse) {
  return aula.capacidad == null ? '—' : String(aula.capacidad)
}

export function AulasTable({ rows, emptyMessage, onEdit, onVersion, onDelete }: AulasTableProps) {
  if (rows.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>
  }

  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Tipo</th>
              <th>Capacidad</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((aula) => (
              <tr key={aula.id}>
                <td className={styles.name}>{aula.nombre}</td>
                <td>{etiquetaTipoAula(aula.tipo)}</td>
                <td>{capacidad(aula)}</td>
                <td>
                  {aula.activa ? (
                    <Badge tone="success">Activa</Badge>
                  ) : (
                    <Badge tone="neutral">Inactiva</Badge>
                  )}
                </td>
                <td>
                  <div className={styles.actions}>
                    <IconActions>
                      <IconAction
                        label={`Editar ${aula.nombre}`}
                        onClick={() => onEdit(aula)}
                      />
                      <IconAction
                        tone="danger"
                        label={`Eliminar ${aula.nombre}`}
                        onClick={() => onDelete(aula)}
                      />
                    </IconActions>
                    <TextLink onClick={() => onVersion(aula)}>Nueva versión</TextLink>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((aula) => (
          <li key={aula.id} className={styles.card}>
            <strong>{aula.nombre}</strong>
            <p>
              {etiquetaTipoAula(aula.tipo)} · {aula.activa ? 'Activa' : 'Inactiva'}
            </p>
            <p>Capacidad: {capacidad(aula)}</p>
            <div className={styles.actions}>
              <IconActions>
                <IconAction
                  label={`Editar ${aula.nombre}`}
                  onClick={() => onEdit(aula)}
                />
                <IconAction
                  tone="danger"
                  label={`Eliminar ${aula.nombre}`}
                  onClick={() => onDelete(aula)}
                />
              </IconActions>
              <TextLink onClick={() => onVersion(aula)}>Nueva versión</TextLink>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
