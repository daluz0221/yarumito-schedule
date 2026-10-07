import { dashboardAlerts } from '../../../content/dashboard'
import { AlertsCard } from '../../molecules/AlertsCard'
import { ProcessStatusCard } from '../../molecules/ProcessStatusCard'
import styles from './InsightsSection.module.css'

export function InsightsSection() {
  return (
    <section className={styles.section} aria-label="Estado e información">
      <ProcessStatusCard year="2026" status="En construcción" />
      <AlertsCard items={dashboardAlerts} />
    </section>
  )
}
