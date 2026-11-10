import type { StudyPlanGrade } from '../../../content/studyPlan'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './GradePlanSummary.module.css'

export type GradePlanSummaryProps = {
  grade: StudyPlanGrade
  configuredHours: number
}

export function GradePlanSummary({ grade, configuredHours }: GradePlanSummaryProps) {
  const expected = grade.weeklyHours
  const difference = Math.abs(expected - configuredHours)
  const pending = Math.max(expected - configuredHours, 0)
  const excess = Math.max(configuredHours - expected, 0)
  const progress = expected === 0 ? 0 : Math.min(100, (configuredHours / expected) * 100)
  const pendingLabel = excess
    ? `Excede ${excess} horas`
    : pending
      ? `Pendiente ${pending} horas`
      : 'Horas completas'

  return (
    <Card className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        Resumen del grado
      </Text>
      <dl className={styles.facts}>
        <Fact label="Grado" value={grade.label} />
        <Fact label="Tipo" value={grade.level} />
        <Fact label="Horas semanales esperadas" value={`${expected} h`} />
        <Fact label="Horas configuradas" value={`${configuredHours} h`} />
        <Fact label="Diferencia" value={`${difference} h`} />
      </dl>
      <div className={styles.progressCopy}>
        <p>
          {configuredHours} de {expected} h configuradas
        </p>
        <p className={excess ? styles.warning : pending ? styles.pending : styles.complete}>
          {pendingLabel}
        </p>
      </div>
      <div
        className={styles.track}
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={expected}
        aria-valuenow={Math.min(configuredHours, expected)}
        aria-label="Horas configuradas del grado"
      >
        <span className={styles.fill} style={{ width: `${progress}%` }} />
      </div>
    </Card>
  )
}

function Fact({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.fact}>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  )
}
