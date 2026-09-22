import { dashboardStats } from '../../content/dashboard'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { InsightsSection } from '../../components/organisms/InsightsSection'
import { ModulesSection } from '../../components/organisms/ModulesSection'
import { Sidebar } from '../../components/organisms/Sidebar'
import { StatsSection } from '../../components/organisms/StatsSection'
import { WelcomeSection } from '../../components/organisms/WelcomeSection'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function DashboardPage() {
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
      <StatsSection items={dashboardStats} />
      <ModulesSection />
      <InsightsSection />
    </DashboardLayout>
  )
}
