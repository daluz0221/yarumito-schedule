import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { GradosWorkspace } from '../../components/organisms/GradosWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function GradosPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Grados"
          subtitle="Administración de los grados de la institución"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[{ label: 'Configuración académica' }, { label: 'Grados' }]}
      />
      <GradosWorkspace />
    </DashboardLayout>
  )
}
