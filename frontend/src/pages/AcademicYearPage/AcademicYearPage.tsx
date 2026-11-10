import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { AcademicYearWorkspace } from '../../components/organisms/AcademicYearWorkspace'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'

export function AcademicYearPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title="Año lectivo y grupos"
          subtitle="Configuración del período académico y sus grupos activos"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Configuración académica' },
          { label: 'Año lectivo y grupos' },
        ]}
      />
      <AcademicYearWorkspace />
    </DashboardLayout>
  )
}
