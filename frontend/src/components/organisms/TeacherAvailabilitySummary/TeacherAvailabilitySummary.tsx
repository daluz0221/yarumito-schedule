import { restrictionCountLabel } from '../../../content/teacherRestrictions'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './TeacherAvailabilitySummary.module.css'

export type TeacherAvailabilitySummaryProps = {
  name: string
  areaName: string
  statusLabel: string
  year: number
  restrictionCount: number
}

export function TeacherAvailabilitySummary({
  name,
  areaName,
  statusLabel,
  year,
  restrictionCount,
}: TeacherAvailabilitySummaryProps) {
  return (
    <Card className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {name}
      </Text>
      <p className={styles.meta}>
        <span>Área de nombramiento: {areaName}</span>
        <span>Estado: {statusLabel}</span>
        <span>Año: {year}</span>
        <span>{restrictionCountLabel(restrictionCount)}</span>
      </p>
    </Card>
  )
}
