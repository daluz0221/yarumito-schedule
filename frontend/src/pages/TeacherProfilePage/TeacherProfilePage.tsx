import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { TeacherProfileWorkspace } from '../../components/organisms/TeacherProfileWorkspace'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function TeacherProfilePage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Perfil académico del docente"
          subtitle="Gestione las áreas o asignaturas para las que el docente se encuentra habilitado."
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Volver a docentes', to: '/dashboard/docentes' },
          { label: 'Perfil académico' },
        ]}
      />
      <TeacherProfileWorkspace />
    </DashboardLayout>
  )
}
