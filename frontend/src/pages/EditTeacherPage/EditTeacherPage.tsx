import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { EditTeacherWorkspace } from '../../components/organisms/EditTeacherWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function EditTeacherPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Editar docente"
          subtitle="Actualice la información básica e institucional del docente."
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Inicio', to: '/dashboard' },
          { label: 'Gestión de Docentes', to: '/dashboard/docentes' },
          { label: 'Editar docente' },
        ]}
      />
      <EditTeacherWorkspace />
    </DashboardLayout>
  )
}
