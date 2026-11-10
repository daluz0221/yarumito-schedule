import { StatCard } from '../../molecules/StatCard'
import styles from './ScheduleMetrics.module.css'

export type ScheduleMetricsProps = {
  programmed: number
  assignedCount: number
  placedHours: number
  assignedHours: number
  pendingHours: number
  conflicts: number
  warnings: number
}

export function ScheduleMetrics({
  programmed,
  assignedCount,
  placedHours,
  assignedHours,
  pendingHours,
  conflicts,
  warnings,
}: ScheduleMetricsProps) {
  return (
    <div className={styles.row} aria-label="Resumen del horario">
      <StatCard
        value={`${programmed} / ${assignedCount}`}
        label="Asignaciones programadas"
      />
      <StatCard value={`${placedHours} / ${assignedHours}`} label="Horas programadas" />
      <StatCard value={`${pendingHours} h`} label="Pendientes" />
      <StatCard value={String(conflicts)} label="Conflictos" />
      <StatCard value={String(warnings)} label="Advertencias" />
    </div>
  )
}
