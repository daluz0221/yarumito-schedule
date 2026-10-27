import {
  formatVigencia,
  isIdoneidadVigente,
  labelTipoIdoneidad,
  resolveTituloSoporte,
  type Idoneidad,
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
import { ValidityStatus } from '../../molecules/ValidityStatus'
import styles from './TeacherSuitabilitySection.module.css'

export type TeacherSuitabilitySectionProps = {
  items: Idoneidad[]
  titles: ProfessionalTitle[]
  titlesUnavailable?: boolean
  areaNameById: (areaId: string) => string
  subjectNameById: (asignaturaId: string) => string
  onAdd: () => void
  onEdit: (item: Idoneidad) => void
  onEnd: (item: Idoneidad) => void
}

export function TeacherSuitabilitySection({
  items,
  titles,
  titlesUnavailable = false,
  areaNameById,
  subjectNameById,
  onAdd,
  onEdit,
  onEnd,
}: TeacherSuitabilitySectionProps) {
  const titleLabel = (tituloId: string) => {
    if (titlesUnavailable && tituloId) {
      return 'No se pudieron cargar los títulos'
    }

    return resolveTituloSoporte(tituloId, titles)
  }
  const subjectLabel = (asignaturaId: string) => {
    if (!asignaturaId) {
      return 'Toda el área'
    }

    return subjectNameById(asignaturaId) || 'Cargando...'
  }
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
                <td className={item.asignaturaId ? styles.strong : styles.pending}>
                  {subjectLabel(item.asignaturaId)}
                </td>
                <td>{labelTipoIdoneidad(item.tipo)}</td>
                <td>{titleLabel(item.tituloSoporteId)}</td>
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
              <p>{subjectLabel(item.asignaturaId)}</p>
              <p>{titleLabel(item.tituloSoporteId)}</p>
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
            La asignatura es opcional. Sin asignatura, la idoneidad cubre toda
            el área. El título de soporte se toma de los títulos profesionales
            del docente.
          </Text>
        </div>
      </Card>
    </section>
  )
}
