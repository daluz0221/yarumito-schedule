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
          title="Año lectivo"
          subtitle="Configuración del período académico"
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Configuración académica' },
          { label: 'Año lectivo' },
        ]}
      />
      <AcademicYearWorkspace />
    </DashboardLayout>
  )
}
