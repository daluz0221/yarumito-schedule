import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { AreasWorkspace } from '../../components/organisms/AreasWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function AreasPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Áreas y asignaturas"
          subtitle="Administración del catálogo académico institucional"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Gestión académica' },
          { label: 'Áreas y asignaturas' },
        ]}
      />
      <AreasWorkspace />
    </DashboardLayout>
  )
}
