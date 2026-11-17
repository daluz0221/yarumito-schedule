import type { AlertItemData } from '../../../content/dashboard'
import { AlertsCard } from '../../molecules/AlertsCard'
import { ProcessStatusCard } from '../../molecules/ProcessStatusCard'
import styles from './InsightsSection.module.css'

export type InsightsSectionProps = {
  year: string
  status: string
  alerts: AlertItemData[]
}

export function InsightsSection({ year, status, alerts }: InsightsSectionProps) {
  return (
    <section className={styles.section} aria-label="Estado e información">
      <ProcessStatusCard year={year} status={status} />
      <AlertsCard items={alerts} />
    </section>
  )
}
