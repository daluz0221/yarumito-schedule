import { etiquetaGrado, type GradoResponse } from '../../../api/grados'
import { Badge } from '../../atoms/Badge'
import { IconAction } from '../../atoms/IconAction'
import styles from './GradosTable.module.css'

export type GradosTableProps = {
  rows: GradoResponse[]
  emptyMessage: string
  onEdit: (grado: GradoResponse) => void
}

export function GradosTable({ rows, emptyMessage, onEdit }: GradosTableProps) {
  if (rows.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>
  }

  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Nivel</th>
              <th>Grado</th>
              <th>Tipo</th>
              <th>Prioridad</th>
              <th>Horas</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((grado) => (
              <tr key={grado.id}>
                <td className={styles.nivel}>{grado.nivel}°</td>
                <td>{etiquetaGrado(grado.nombre)}</td>
                <td>
                  {grado.esMedia ? <Badge tone="success">Media</Badge> : 'Básica'}
                </td>
                <td>{grado.prioridadAsignacion}</td>
                <td>{grado.horasSemanalesEsperadas}</td>
                <td>
                  <IconAction
                    label={`Editar ${etiquetaGrado(grado.nombre)}`}
                    onClick={() => onEdit(grado)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((grado) => (
          <li key={grado.id} className={styles.card}>
            <strong>
              {grado.nivel}° {etiquetaGrado(grado.nombre)}
            </strong>
            <p>
              {grado.esMedia ? 'Media' : 'Básica'} · Prioridad {grado.prioridadAsignacion} ·{' '}
              {grado.horasSemanalesEsperadas} h
            </p>
            <IconAction
              label={`Editar ${etiquetaGrado(grado.nombre)}`}
              onClick={() => onEdit(grado)}
            />
          </li>
        ))}
      </ul>
    </div>
  )
}
