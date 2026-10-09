import { StatCard } from '../../molecules/StatCard'
import styles from './ValidationMetrics.module.css'

export type ValidationMetricsProps = {
  total: number
  errors: number
  warnings: number
  resolved: number
  pending: number
}

export function ValidationMetrics({
  total,
  errors,
  warnings,
  resolved,
  pending,
}: ValidationMetricsProps) {
  return (
    <div className={styles.row} aria-label="Resumen de conflictos">
      <StatCard value={String(total)} label="Total conflictos" />
      <StatCard value={String(errors)} label="Errores" tone="danger" />
      <StatCard value={String(warnings)} label="Advertencias" tone="warning" />
      <StatCard value={String(resolved)} label="Resueltos" tone="success" />
      <StatCard value={String(pending)} label="Pendientes" />
    </div>
  )
}
