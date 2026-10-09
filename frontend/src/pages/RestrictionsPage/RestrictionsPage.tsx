import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { RestrictionsWorkspace } from '../../components/organisms/RestrictionsWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function RestrictionsPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Restricciones docentes"
          subtitle="Disponibilidad para la planificación del año lectivo"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Docentes', to: '/dashboard/docentes' },
          { label: 'Restricciones y disponibilidad' },
        ]}
      />
      <RestrictionsWorkspace />
    </DashboardLayout>
  )
}
