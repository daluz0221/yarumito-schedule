import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { AuditoriaWorkspace } from '../../components/organisms/AuditoriaWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function AuditoriaPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Auditoría"
          subtitle="Consulta cambios de asignaciones y clases por grupo o por cuenta"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[{ label: 'Planeación académica' }, { label: 'Auditoría' }]}
      />
      <AuditoriaWorkspace />
    </DashboardLayout>
  )
}
