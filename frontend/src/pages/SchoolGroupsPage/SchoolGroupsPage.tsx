import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { SchoolGroupsWorkspace } from '../../components/organisms/SchoolGroupsWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function SchoolGroupsPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Grupos"
          subtitle="Grupos de cada grado por año lectivo"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Configuración académica' },
          { label: 'Grupos' },
        ]}
      />
      <SchoolGroupsWorkspace />
    </DashboardLayout>
  )
}
