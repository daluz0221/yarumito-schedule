import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { AulasWorkspace } from '../../components/organisms/AulasWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function AulasPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Aulas"
          subtitle="Espacios físicos de cada sede"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb items={[{ label: 'Configuración académica' }, { label: 'Aulas' }]} />
      <AulasWorkspace />
    </DashboardLayout>
  )
}
