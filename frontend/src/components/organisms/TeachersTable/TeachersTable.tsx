import {
  PENDING_BACKEND_LABEL,
  formatTeacherName,
  labelEstado,
  labelVinculacion,
  toneEstado,
  type Teacher,
} from '../../../content/teachers'
import { Card } from '../../atoms/Card'
import { TextLink } from '../../atoms/TextLink'
import { Pagination } from '../../molecules/Pagination'
import { StatusLabel } from '../../molecules/StatusLabel'
import styles from './TeachersTable.module.css'

export type TeachersTableProps = {
  rows: Teacher[]
  page: number
  pageSize: number
  total: number
  emptyMessage?: string
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}

function PendingValue() {
  return <span className={styles.pending}>{PENDING_BACKEND_LABEL}</span>
}

export function TeachersTable({
  rows,
  page,
  pageSize,
  total,
  emptyMessage,
  onPageChange,
  onPageSizeChange,
}: TeachersTableProps) {
  return (
    <Card padding="sm" className={styles.card}>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Documento</th>
              <th>Docente</th>
              <th>Área</th>
              <th>Vinculación</th>
              <th>Contacto</th>
              <th>Estado</th>
              <th>Títulos</th>
              <th>Idoneidad</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((teacher) => (
              <tr key={teacher.id}>
                <td>
                  {teacher.tipoDocumento} {teacher.numeroDocumento}
                </td>
                <td className={styles.name}>{formatTeacherName(teacher)}</td>
                {/* Próximamente: DocenteResponse solo trae areaNombramientoId. */}
                <td>
                  <PendingValue />
                </td>
                <td>{labelVinculacion(teacher.tipoVinculacion)}</td>
                <td>
                  <span className={styles.contact}>
                    <span>{teacher.correoInstitucional}</span>
                    <span>{teacher.telefono}</span>
                  </span>
                </td>
                <td>
                  <StatusLabel
                    label={labelEstado(teacher.estado)}
                    tone={toneEstado(teacher.estado)}
                  />
                </td>
                {/* Próximamente: el listado de docentes no incluye conteo de títulos. */}
                <td>
                  <PendingValue />
                </td>
                {/* Próximamente: el listado de docentes no incluye resumen de idoneidad. */}
                <td>
                  <PendingValue />
                </td>
                <td>
                  <span className={styles.rowActions}>
                    <TextLink to={`/dashboard/docentes/${teacher.id}/editar`}>
                      Editar
                    </TextLink>
                    <TextLink to={`/dashboard/docentes/${teacher.id}`}>
                      Ver perfil
                    </TextLink>
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((teacher) => (
          <li key={teacher.id} className={styles.mobileCard}>
            <div className={styles.mobileHeader}>
              <strong>{formatTeacherName(teacher)}</strong>
              <StatusLabel
                label={labelEstado(teacher.estado)}
                tone={toneEstado(teacher.estado)}
              />
            </div>
            <p>
              {teacher.tipoDocumento} {teacher.numeroDocumento}
            </p>
            <p>
              {PENDING_BACKEND_LABEL} · {labelVinculacion(teacher.tipoVinculacion)}
            </p>
            <p>
              {teacher.correoInstitucional}
              <br />
              {teacher.telefono}
            </p>
            <p>
              {PENDING_BACKEND_LABEL} · {PENDING_BACKEND_LABEL}
            </p>
            <span className={styles.rowActions}>
              <TextLink to={`/dashboard/docentes/${teacher.id}/editar`}>
                Editar
              </TextLink>
              <TextLink to={`/dashboard/docentes/${teacher.id}`}>
                Ver perfil
              </TextLink>
            </span>
          </li>
        ))}
      </ul>

      {rows.length === 0 ? (
        <p className={styles.empty}>
          {emptyMessage ?? 'No se encontraron docentes con esos filtros.'}
        </p>
      ) : null}

      <Pagination
        page={page}
        pageSize={pageSize}
        total={total}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
      />
    </Card>
  )
}
