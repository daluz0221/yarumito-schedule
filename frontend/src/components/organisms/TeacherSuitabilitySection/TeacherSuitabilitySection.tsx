import {
  PENDING_BACKEND_LABEL,
  formatVigencia,
  isIdoneidadVigente,
  labelTipoIdoneidad,
  type Idoneidad,
} from '../../../content/teacherProfile'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { PlusIcon } from '../../atoms/icons'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { RowActions } from '../../molecules/RowActions'
import { SectionHeader } from '../../molecules/SectionHeader'
import { ValidityStatus } from '../../molecules/ValidityStatus'
import styles from './TeacherSuitabilitySection.module.css'

export type TeacherSuitabilitySectionProps = {
  items: Idoneidad[]
  areaNameById: (areaId: string) => string
  onAdd: () => void
  onEdit: (item: Idoneidad) => void
  onEnd: (item: Idoneidad) => void
}

export function TeacherSuitabilitySection({
  items,
  areaNameById,
  onAdd,
  onEdit,
  onEnd,
}: TeacherSuitabilitySectionProps) {
  return (
    <section className={styles.section}>
      <SectionHeader
        title="Idoneidad académica"
        description="Define las áreas y asignaturas para las que el docente se encuentra habilitado de acuerdo con su perfil profesional."
        action={
          <Button size="sm" onClick={onAdd}>
            <PlusIcon />
            Agregar idoneidad
          </Button>
        }
      />
      <Card padding="sm" className={styles.card}>
        <Table minWidth={880} className={styles.desktopOnly}>
          <thead>
            <tr>
              <th>Área</th>
              <th>Asignatura</th>
              <th>Tipo</th>
              <th>Título soporte</th>
              <th>Vigencia</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{areaNameById(item.areaId)}</td>
                <td className={styles.strong}>
                  <span className={styles.pending}>{PENDING_BACKEND_LABEL}</span>
                </td>
                <td>{labelTipoIdoneidad(item.tipo)}</td>
                <td>
                  <span className={styles.pending}>{PENDING_BACKEND_LABEL}</span>
                </td>
                <td>
                  <ValidityStatus
                    current={isIdoneidadVigente(item)}
                    since={formatVigencia(item)}
                  />
                </td>
                <td>
                  <RowActions>
                    <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
                    {isIdoneidadVigente(item) ? (
                      <TextLink onClick={() => onEnd(item)}>
                        Finalizar vigencia
                      </TextLink>
                    ) : null}
                  </RowActions>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>

        <ul className={styles.cards}>
          {items.map((item) => (
            <li key={item.id} className={styles.mobileCard}>
              <strong>
                {areaNameById(item.areaId)} · {labelTipoIdoneidad(item.tipo)}
              </strong>
              <p>{PENDING_BACKEND_LABEL}</p>
              <ValidityStatus
                current={isIdoneidadVigente(item)}
                since={formatVigencia(item)}
              />
              <RowActions>
                <TextLink onClick={() => onEdit(item)}>Editar</TextLink>
                {isIdoneidadVigente(item) ? (
                  <TextLink onClick={() => onEnd(item)}>
                    Finalizar vigencia
                  </TextLink>
                ) : null}
              </RowActions>
            </li>
          ))}
        </ul>

        {items.length === 0 ? (
          <p className={styles.empty}>
            No hay idoneidades registradas para este docente.
          </p>
        ) : null}

        <div className={styles.note}>
          <Text variant="body">
            Asignatura y título de soporte aparecen como “Próximamente”:
            no hay catálogo de asignaturas ni de títulos profesionales.
          </Text>
        </div>
      </Card>
    </section>
  )
}
