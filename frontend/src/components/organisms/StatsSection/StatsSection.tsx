import type { StatItemData } from '../../../content/dashboard'
import { StatCard } from '../../molecules/StatCard'
import styles from './StatsSection.module.css'

export type StatsSectionProps = {
  items: StatItemData[]
  label?: string
}

export function StatsSection({ items, label = 'Resumen' }: StatsSectionProps) {
  return (
    <section className={styles.section} aria-label={label}>
      {items.map((stat) => (
        <StatCard key={stat.id} value={stat.value} label={stat.label} />
      ))}
    </section>
  )
}
