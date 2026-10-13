import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { TeacherInfoPanel } from '../../components/organisms/TeacherInfoPanel'
import { TeachersWorkspace } from '../../components/organisms/TeachersWorkspace'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function TeachersPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Gestión de Docentes"
          subtitle="Administre la información general, académica y profesional de los docentes."
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Inicio', to: '/dashboard' },
          { label: 'Gestión de Docentes' },
        ]}
      />
      <TeachersWorkspace />
      <TeacherInfoPanel />
    </DashboardLayout>
  )
}
