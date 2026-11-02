import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { SedesWorkspace } from '../../components/organisms/SedesWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function SedesPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Sedes"
          subtitle="Administración de las sedes de la institución"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[{ label: 'Configuración académica' }, { label: 'Sedes' }]}
      />
      <SedesWorkspace />
    </DashboardLayout>
  )
}
