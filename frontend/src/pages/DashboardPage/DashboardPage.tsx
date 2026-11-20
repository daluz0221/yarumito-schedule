import { useResumenTablero } from '../../api/dashboardResumen'
import type { AlertItemData, StatItemData } from '../../content/dashboard'
import { AlertsCard } from '../../components/molecules/AlertsCard'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { ModulesSection } from '../../components/organisms/ModulesSection'
import { Sidebar } from '../../components/organisms/Sidebar'
import { StatsSection } from '../../components/organisms/StatsSection'
import { WelcomeSection } from '../../components/organisms/WelcomeSection'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

const statsCargando: StatItemData[] = [
  { id: 'teachers', value: '…', label: 'Docentes activos' },
  { id: 'groups', value: '…', label: 'Grupos activos' },
  { id: 'areas', value: '…', label: 'Áreas académicas' },
  { id: 'year', value: '…', label: 'Año lectivo' },
]

export function DashboardPage() {
  const { resumen, error } = useResumenTablero()

  const stats = resumen?.stats ?? statsCargando
  const alerts: AlertItemData[] = error
    ? [{ id: 'error', message: error, tone: 'alert' }]
    : (resumen?.alerts ?? [{ id: 'loading', message: 'Cargando alertas…', tone: 'neutral' }])

  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Panel principal"
          subtitle="Gestión académica y planificación de horarios"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <WelcomeSection />
      <StatsSection items={stats} />
      <ModulesSection />
      <section aria-label="Alertas">
        <AlertsCard items={alerts} />
      </section>
    </DashboardLayout>
  )
}
