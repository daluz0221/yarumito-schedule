import {
  labelNivelTitulo,
  type ProfessionalTitle,
} from '../../../content/teacherProfile'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { PlusIcon } from '../../atoms/icons'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { RowActions } from '../../molecules/RowActions'
import { SectionHeader } from '../../molecules/SectionHeader'
import styles from './TeacherTitlesSection.module.css'

export type TeacherTitlesSectionProps = {
  items: ProfessionalTitle[]
  loading?: boolean
  error?: string
  onRetry?: () => void
  onAdd?: () => void
  onEdit?: (item: ProfessionalTitle) => void
}

export function TeacherTitlesSection({
  items,
  loading = false,
  error = '',
  onRetry,
  onAdd,
  onEdit,
}: TeacherTitlesSectionProps) {
  return (
    <section className={styles.section}>
      <SectionHeader
        title="Títulos profesionales"
        description="Formación académica registrada como soporte del perfil profesional del docente."
        action={
          onAdd ? (
            <Button size="sm" onClick={onAdd}>
              <PlusIcon />
              Agregar título
            </Button>
          ) : null
        }
      />
      <Card padding="sm" className={styles.card}>
        {loading ? (
          <p className={styles.status}>Cargando títulos...</p>
        ) : error && items.length === 0 ? (
          <div className={styles.feedback}>
            <p className={styles.error} role="alert">
              {error}
            </p>
            {onRetry ? (
              <TextLink onClick={onRetry}>Reintentar</TextLink>
            ) : null}
          </div>
        ) : items.length === 0 ? (
          <p className={styles.empty}>
            No hay títulos profesionales registrados para este docente.
          </p>
        ) : (
          <>
            {error ? (
              <div className={styles.feedback}>
                <p className={styles.error} role="alert">
                  {error}
                </p>
                {onRetry ? (
                  <TextLink onClick={onRetry}>Reintentar</TextLink>
                ) : null}
              </div>
            ) : null}
            <Table minWidth={860} className={styles.desktopOnly}>
              <thead>
                <tr>
                  <th>Nivel</th>
                  <th>Título</th>
                  <th>Institución</th>
                  <th>Año</th>
                  <th>Soporte</th>
                  {onEdit ? <th>Acciones</th> : null}
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td>{labelNivelTitulo(item.level)}</td>
                    <td className={styles.strong}>{item.name || 'Sin nombre'}</td>
                    <td>{item.institution || '—'}</td>
                    <td>{item.year || '—'}</td>
                    <td>{item.supportFile || 'Sin archivo'}</td>
                    {onEdit ? (
                      <td>
                        <RowActions>
                          <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
                        </RowActions>
                      </td>
                    ) : null}
                  </tr>
                ))}
              </tbody>
            </Table>
            <ul className={styles.cards}>
              {items.map((item) => (
                <li key={item.id} className={styles.mobileCard}>
                  <strong>
                    {labelNivelTitulo(item.level)} · {item.name || 'Sin nombre'}
                  </strong>
                  <p>
                    {item.institution || 'Sin institución'}
                    {item.year ? ` · ${item.year}` : ''}
                  </p>
                  <Text variant="caption">
                    {item.supportFile || 'Sin archivo de soporte'}
                  </Text>
                  {onEdit ? (
                    <RowActions>
                      <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
                    </RowActions>
                  ) : null}
                </li>
              ))}
            </ul>
          </>
        )}
      </Card>
    </section>
  )
}
