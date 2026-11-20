import {
  labelNivelTitulo,
  type ProfessionalTitle,
} from '../../../content/teacherProfile'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { IconAction, IconActions } from '../../atoms/IconAction'
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
  onRetire?: (item: ProfessionalTitle) => void
  // H-04: títulos retirados y su reactivación.
  retiredItems?: ProfessionalTitle[]
  retiredLoading?: boolean
  retiredError?: string
  showRetired?: boolean
  onToggleRetired?: () => void
  onRestore?: (item: ProfessionalTitle) => void
}

export function TeacherTitlesSection({
  items,
  loading = false,
  error = '',
  onRetry,
  onAdd,
  onEdit,
  onRetire,
  retiredItems = [],
  retiredLoading = false,
  retiredError = '',
  showRetired = false,
  onToggleRetired,
  onRestore,
}: TeacherTitlesSectionProps) {
  const viewingRetired = showRetired && Boolean(onToggleRetired)
  const rows = viewingRetired ? retiredItems : items
  const isLoading = viewingRetired ? retiredLoading : loading
  const currentError = viewingRetired ? retiredError : error
  const hasActions = viewingRetired ? Boolean(onRestore) : Boolean(onEdit || onRetire)
  const emptyMessage = viewingRetired
    ? 'Este docente no tiene títulos retirados.'
    : 'No hay títulos profesionales registrados para este docente.'

  const renderActions = (item: ProfessionalTitle) => (
    <RowActions>
      {viewingRetired ? (
        onRestore ? (
          <IconAction
            tone="restore"
            label={`Reactivar ${item.name || 'título'}`}
            onClick={() => onRestore(item)}
          />
        ) : null
      ) : (
        <>
          {onEdit ? (
            <IconAction
              label={`Editar ${item.name || 'título'}`}
              onClick={() => onEdit(item)}
            />
          ) : null}
          {onRetire ? (
            <IconAction
              tone="danger"
              label={`Retirar ${item.name || 'título'}`}
              onClick={() => onRetire(item)}
            />
          ) : null}
        </>
      )}
    </RowActions>
  )

  return (
    <section className={styles.section}>
      <SectionHeader
        title={viewingRetired ? 'Títulos retirados' : 'Títulos profesionales'}
        description={
          viewingRetired
            ? 'Títulos dados de baja. Puedes reactivarlos si vuelven a ser válidos.'
            : 'Formación académica registrada como soporte del perfil profesional del docente.'
        }
        action={
          <IconActions>
            {onToggleRetired ? (
              <IconAction
                tone="archive"
                pressed={viewingRetired}
                label={viewingRetired ? 'Ver títulos activos' : 'Ver títulos retirados'}
                onClick={onToggleRetired}
              />
            ) : null}
            {onAdd && !viewingRetired ? (
              <Button size="sm" onClick={onAdd}>
                <PlusIcon />
                Agregar título
              </Button>
            ) : null}
          </IconActions>
        }
      />
      <Card padding="sm" className={styles.card}>
        {isLoading ? (
          <p className={styles.status}>Cargando títulos...</p>
        ) : currentError && rows.length === 0 ? (
          <div className={styles.feedback}>
            <p className={styles.error} role="alert">
              {currentError}
            </p>
            {onRetry && !viewingRetired ? (
              <TextLink onClick={onRetry}>Reintentar</TextLink>
            ) : null}
          </div>
        ) : rows.length === 0 ? (
          <p className={styles.empty}>{emptyMessage}</p>
        ) : (
          <>
            {currentError ? (
              <div className={styles.feedback}>
                <p className={styles.error} role="alert">
                  {currentError}
                </p>
                {onRetry && !viewingRetired ? (
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
                  {hasActions ? <th>Acciones</th> : null}
                </tr>
              </thead>
              <tbody>
                {rows.map((item) => (
                  <tr key={item.id}>
                    <td>{labelNivelTitulo(item.level)}</td>
                    <td className={styles.strong}>{item.name || 'Sin nombre'}</td>
                    <td>{item.institution || '—'}</td>
                    <td>{item.year || '—'}</td>
                    <td>{item.supportFile || 'Sin archivo'}</td>
                    {hasActions ? <td>{renderActions(item)}</td> : null}
                  </tr>
                ))}
              </tbody>
            </Table>
            <ul className={styles.cards}>
              {rows.map((item) => (
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
                  {hasActions ? renderActions(item) : null}
                </li>
              ))}
            </ul>
          </>
        )}
      </Card>
    </section>
  )
}
