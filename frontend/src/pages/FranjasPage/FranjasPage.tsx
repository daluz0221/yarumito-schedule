import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { FranjasWorkspace } from '../../components/organisms/FranjasWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function FranjasPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Franjas horarias"
          subtitle="Clases y descansos de cada turno"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb items={[{ label: 'Configuración académica' }, { label: 'Franjas horarias' }]} />
      <FranjasWorkspace />
    </DashboardLayout>
  )
}
