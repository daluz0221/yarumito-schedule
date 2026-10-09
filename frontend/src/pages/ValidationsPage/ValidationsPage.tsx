import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { ValidationsWorkspace } from '../../components/organisms/ValidationsWorkspace'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function ValidationsPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Validaciones y conflictos"
          subtitle="Revisa los errores y advertencias detectados antes de publicar el horario"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Planeación académica' },
          { label: 'Validaciones y conflictos' },
        ]}
      />
      <ValidationsWorkspace />
    </DashboardLayout>
  )
}
