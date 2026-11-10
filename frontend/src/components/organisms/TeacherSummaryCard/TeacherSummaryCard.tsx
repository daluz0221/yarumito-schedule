import {
  PENDING_BACKEND_LABEL,
  formatTeacherName,
  labelEstado,
  toneEstado,
  type Teacher,
} from '../../../content/teachers'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { MetaField } from '../../molecules/MetaField'
import { StatusLabel } from '../../molecules/StatusLabel'
import styles from './TeacherSummaryCard.module.css'
export type TeacherSummaryCardProps = {
  teacher: Teacher
  documentLabel: string
  areaNombre?: string
}

export function TeacherSummaryCard({
  teacher,
  documentLabel,
  areaNombre,
}: TeacherSummaryCardProps) {
  return (
    <Card className={styles.card}>
      <div className={styles.top}>
        <Text variant="sectionTitle">{formatTeacherName(teacher)}</Text>
        <div className={styles.topActions}>
          <StatusLabel
            label={labelEstado(teacher.estado)}
            tone={toneEstado(teacher.estado)}
          />
          <TextLink to={`/dashboard/docentes/${teacher.id}/editar`}>
            Editar datos
          </TextLink>
        </div>
      </div>
      <div className={styles.meta}>
        <MetaField label="Documento" value={documentLabel} />
        <MetaField
          label="Área de nombramiento"
          value={areaNombre || PENDING_BACKEND_LABEL}
        />
        <MetaField label="Estado" value={labelEstado(teacher.estado)} />
      </div>
    </Card>
  )
}
