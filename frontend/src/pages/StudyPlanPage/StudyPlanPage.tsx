import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { StudyPlanWorkspace } from '../../components/organisms/StudyPlanWorkspace'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function StudyPlanPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Plan de estudios"
          subtitle="Configuración curricular por año lectivo y grado"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Gestión académica' },
          { label: 'Plan de estudios' },
        ]}
      />
      <StudyPlanWorkspace />
    </DashboardLayout>
  )
}
