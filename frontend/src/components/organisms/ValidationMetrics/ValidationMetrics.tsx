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
      <StatCard value={String(total)} label="Total conflictos" wash="total" />
      <StatCard value={String(errors)} label="Errores" wash="danger" />
      <StatCard value={String(warnings)} label="Advertencias" wash="warning" />
      <StatCard value={String(resolved)} label="Resueltos" wash="success" />
      <StatCard value={String(pending)} label="Pendientes" wash="neutral" />
    </div>
  )
}
