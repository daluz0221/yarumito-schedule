import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { RegisterTeacherWorkspace } from '../../components/organisms/RegisterTeacherWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function RegisterTeacherPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Registrar docente"
          subtitle="Registre la información básica e institucional del docente."
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Inicio', to: '/dashboard' },
          { label: 'Gestión de Docentes', to: '/dashboard/docentes' },
          { label: 'Registrar docente' },
        ]}
      />
      <RegisterTeacherWorkspace />
    </DashboardLayout>
  )
}
