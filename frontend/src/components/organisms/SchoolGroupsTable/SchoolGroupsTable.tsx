import {
  labelGroupStatus,
  labelSchoolLevel,
  type SchoolGroup,
  type SchoolGroupStatus,
} from '../../../content/academicYear'
import { Card } from '../../atoms/Card'
import { TextLink } from '../../atoms/TextLink'
import styles from './SchoolGroupsTable.module.css'

export type SchoolGroupsTableProps = {
  rows: SchoolGroup[]
  emptyMessage: string
  onEdit: (group: SchoolGroup) => void
}

export function SchoolGroupsTable({
  rows,
  emptyMessage,
  onEdit,
}: SchoolGroupsTableProps) {
  return (
    <Card padding="sm" className={styles.card}>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Grupo</th>
              <th>Grado</th>
              <th>Tipo</th>
              <th>Intensidad semanal</th>
              <th>Estudiantes</th>
              <th>Director de grupo</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((item) => (
              <tr key={item.id}>
                <td className={styles.code}>{item.code}</td>
                <td>{item.grade}</td>
                <td className={styles.positive}>{labelSchoolLevel(item.level)}</td>
                <td>{item.weeklyHours} h</td>
                <td>{item.students}</td>
                <td>
                  {item.director ?? (
                    <span className={styles.unassigned}>Sin asignar</span>
                  )}
                </td>
                <td className={statusClassName(item.status)}>
                  {labelGroupStatus(item.status)}
                </td>
                <td>
                  <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((item) => (
          <li key={item.id} className={styles.mobileCard}>
            <div className={styles.mobileHeader}>
              <strong>{item.code}</strong>
              <span className={statusClassName(item.status)}>
                {labelGroupStatus(item.status)}
              </span>
            </div>
            <p>
              {item.grade} ·{' '}
              <span className={styles.positive}>{labelSchoolLevel(item.level)}</span>
            </p>
            <p>
              {item.weeklyHours} h · {item.students} estudiantes
            </p>
            <p>
              Director:{' '}
              {item.director ?? (
                <span className={styles.unassigned}>Sin asignar</span>
              )}
            </p>
            <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
          </li>
        ))}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </Card>
  )
}

function statusClassName(status: SchoolGroupStatus) {
  return status === 'ACTIVO' ? styles.positive : styles.muted
}
