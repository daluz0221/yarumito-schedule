import {
  labelAssignmentStatus,
  type AssignmentStatus,
} from '../../../content/academicAssignment'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './AssignmentSummary.module.css'

export type AssignmentSummaryProps = {
  groupCode: string
  gradeLabel: string
  requiredHours: number
  assignedHours: number
  status: AssignmentStatus
  subjectCount: number
  year: number
}

export function AssignmentSummary({
  groupCode,
  gradeLabel,
  requiredHours,
  assignedHours,
  status,
  subjectCount,
  year,
}: AssignmentSummaryProps) {
  return (
    <Card className={styles.card}>
      <Text as="p" variant="sectionTitle" className={styles.title}>
        Grupo {groupCode} · Grado {gradeLabel} · Requeridas {requiredHours} h ·
        Asignadas {assignedHours} h · Estado {labelAssignmentStatus(status)}
      </Text>
      <Text variant="caption">
        Resumen de las {subjectCount} asignaturas del plan de estudios de{' '}
        {gradeLabel} · Año {year}.
      </Text>
    </Card>
  )
}
