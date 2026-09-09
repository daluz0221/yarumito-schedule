import { dashboardStats } from '../../../content/dashboard'
import { StatCard } from '../../molecules/StatCard'
import styles from './StatsSection.module.css'

export function StatsSection() {
  return (
    <section className={styles.section} aria-label="Resumen">
      {dashboardStats.map((stat) => (
        <StatCard key={stat.id} value={stat.value} label={stat.label} />
      ))}
    </section>
  )
}
