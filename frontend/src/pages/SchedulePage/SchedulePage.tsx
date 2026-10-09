import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { ScheduleWorkspace } from '../../components/organisms/ScheduleWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function SchedulePage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Construcción de horario"
          subtitle="Programa asignaciones por día, franja horaria y aula"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Planeación académica' },
          { label: 'Construcción de horario' },
        ]}
      />
      <ScheduleWorkspace />
    </DashboardLayout>
  )
}
