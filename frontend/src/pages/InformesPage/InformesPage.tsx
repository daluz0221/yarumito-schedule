import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { InformesWorkspace } from '../../components/organisms/InformesWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function InformesPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Informes institucionales"
          subtitle="Planta docente, áreas y asignaturas, y perfiles profesionales"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[{ label: 'Planeación académica' }, { label: 'Informes' }]}
      />
      <InformesWorkspace />
    </DashboardLayout>
  )
}
