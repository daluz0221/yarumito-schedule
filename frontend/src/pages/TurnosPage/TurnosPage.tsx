import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { TurnosWorkspace } from '../../components/organisms/TurnosWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function TurnosPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Turnos"
          subtitle="Jornadas y estructura del horario"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb items={[{ label: 'Configuración académica' }, { label: 'Turnos' }]} />
      <TurnosWorkspace />
    </DashboardLayout>
  )
}
