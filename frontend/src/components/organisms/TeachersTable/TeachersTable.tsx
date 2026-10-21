import {
  labelConteoTitulos,
  labelResumenIdoneidad,
  type ResumenAcademicoDocente,
} from '../../../content/teacherProfile'
import {
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

export type CatalogoAreasTabla =
  | { estado: 'cargando' }
  | { estado: 'error' }
  | { estado: 'listo'; porId: Record<string, string> }

export type TeachersTableProps = {
  rows: Teacher[]
  summaries: Record<string, ResumenAcademicoDocente>
  areas: CatalogoAreasTabla
  page: number
  pageSize: number
  total: number
  emptyMessage?: string
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}

function UnavailableValue() {
  return <span className={styles.pending}>No disponible</span>
}

function LoadingValue() {
  return <span className={styles.pending}>Cargando...</span>
}

function TitlesValue({ summary }: { summary?: ResumenAcademicoDocente }) {
  if (!summary) {
    return <LoadingValue />
  }

  if (summary.titulos.estado === 'error') {
    return <UnavailableValue />
  }

  return labelConteoTitulos(summary.titulos.cantidad)
}

function SuitabilityValue({ summary }: { summary?: ResumenAcademicoDocente }) {
  if (!summary) {
    return <LoadingValue />
  }

  if (summary.idoneidad.estado === 'error') {
    return <UnavailableValue />
  }

  return labelResumenIdoneidad(summary.idoneidad)
}

function AreaValue({
  teacher,
  areas,
}: {
  teacher: Teacher
  areas: CatalogoAreasTabla
}) {
  if (areas.estado === 'cargando') {
    return <LoadingValue />
  }

  if (areas.estado === 'error') {
    return <UnavailableValue />
  }

  const nombre = areas.porId[teacher.areaNombramientoId]

  if (!nombre) {
    return teacher.areaNombramientoId ? <UnavailableValue /> : 'Sin área'
  }

  return nombre
}

export function TeachersTable({
  rows,
  summaries,
  areas,
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
                <td>
                  <AreaValue teacher={teacher} areas={areas} />
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
                <td>
                  <TitlesValue summary={summaries[teacher.id]} />
                </td>
                <td>
                  <SuitabilityValue summary={summaries[teacher.id]} />
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
              <AreaValue teacher={teacher} areas={areas} />
              {' · '}
              {labelVinculacion(teacher.tipoVinculacion)}
            </p>
            <p>
              {teacher.correoInstitucional}
              <br />
              {teacher.telefono}
            </p>
            <p>
              <TitlesValue summary={summaries[teacher.id]} />
              {' · '}
              <SuitabilityValue summary={summaries[teacher.id]} />
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
