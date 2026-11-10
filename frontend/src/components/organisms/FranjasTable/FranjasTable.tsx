import { horaCorta } from '../../../api/turnos'
import type { FranjaHorariaResponse } from '../../../api/franjasHorarias'
import { Badge } from '../../atoms/Badge'
import { IconAction } from '../../atoms/IconAction'
import styles from './FranjasTable.module.css'

export type FranjasTableProps = {
  rows: FranjaHorariaResponse[]
  emptyMessage: string
  onEdit: (franja: FranjaHorariaResponse) => void
}

function etiquetaHoras(value: number) {
  if (!Number.isFinite(value)) {
    return '—'
  }

  return Number.isInteger(value) ? String(value) : String(value)
}

export function FranjasTable({ rows, emptyMessage, onEdit }: FranjasTableProps) {
  if (rows.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>
  }

  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Orden</th>
              <th>Número</th>
              <th>Horario</th>
              <th>Tipo</th>
              <th>Horas</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((franja) => (
              <tr key={franja.id}>
                <td className={styles.order}>{franja.orden}</td>
                <td>{franja.numero}</td>
                <td>
                  {horaCorta(franja.horaInicio)} – {horaCorta(franja.horaFin)}
                </td>
                <td>
                  <Badge tone={franja.esDescanso ? 'warning' : 'success'}>
                    {franja.esDescanso ? 'Descanso' : 'Académica'}
                  </Badge>
                </td>
                <td>{etiquetaHoras(franja.horasAcademicasEquivalentes)}</td>
                <td>
                  <IconAction
                    label={`Editar franja ${franja.orden}`}
                    onClick={() => onEdit(franja)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((franja) => (
          <li key={franja.id} className={styles.card}>
            <strong>
              Orden {franja.orden}
              {franja.esDescanso ? ' · Descanso' : ` · Clase ${franja.numero}`}
            </strong>
            <p>
              {horaCorta(franja.horaInicio)} – {horaCorta(franja.horaFin)}
            </p>
            <p>{etiquetaHoras(franja.horasAcademicasEquivalentes)} h académicas</p>
            <IconAction
              label={`Editar franja ${franja.orden}`}
              onClick={() => onEdit(franja)}
            />
          </li>
        ))}
      </ul>
    </div>
  )
}
