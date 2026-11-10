import { horaCorta, type TurnoResponse } from '../../../api/turnos'
import { IconAction } from '../../atoms/IconAction'
import styles from './TurnosTable.module.css'

export type TurnosTableProps = {
  rows: TurnoResponse[]
  emptyMessage: string
  onEdit: (turno: TurnoResponse) => void
}

export function TurnosTable({ rows, emptyMessage, onEdit }: TurnosTableProps) {
  if (rows.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>
  }

  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Turno</th>
              <th>Horario</th>
              <th>Clase</th>
              <th>Clases / día</th>
              <th>Antes del descanso</th>
              <th>Descanso</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((turno) => (
              <tr key={turno.id}>
                <td className={styles.name}>{turno.nombre}</td>
                <td>
                  {horaCorta(turno.horaInicio)} – {horaCorta(turno.horaFin)}
                </td>
                <td>{turno.duracionClaseMinutos} min</td>
                <td>{turno.clasesPorDia}</td>
                <td>{turno.clasesAntesDeDescanso}</td>
                <td>{turno.duracionDescansoMinutos} min</td>
                <td>
                  <IconAction
                    label={`Editar ${turno.nombre}`}
                    onClick={() => onEdit(turno)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((turno) => (
          <li key={turno.id} className={styles.card}>
            <strong>{turno.nombre}</strong>
            <p>
              {horaCorta(turno.horaInicio)} – {horaCorta(turno.horaFin)}
            </p>
            <p>
              {turno.clasesPorDia} clases de {turno.duracionClaseMinutos} min · descanso{' '}
              {turno.duracionDescansoMinutos} min
            </p>
            <IconAction
              label={`Editar ${turno.nombre}`}
              onClick={() => onEdit(turno)}
            />
          </li>
        ))}
      </ul>
    </div>
  )
}
