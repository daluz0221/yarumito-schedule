import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { ConsultasWorkspace } from '../../components/organisms/ConsultasWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function ConsultasPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Consultas"
          subtitle="Informes de horario por grupo, docente o jornada"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb items={[{ label: 'Planeación académica' }, { label: 'Consultas' }]} />
      <ConsultasWorkspace />
    </DashboardLayout>
  )
}
