import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { AssignmentWorkspace } from '../../components/organisms/AssignmentWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function AssignmentPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Asignación académica"
          subtitle="Docentes, asignaturas y grupos del año lectivo"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Planeación académica' },
          { label: 'Asignación académica' },
        ]}
      />
      <AssignmentWorkspace />
    </DashboardLayout>
  )
}
