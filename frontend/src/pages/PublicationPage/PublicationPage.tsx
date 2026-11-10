import { Breadcrumb } from '../../components/molecules/Breadcrumb'
import { DashboardHeader } from '../../components/organisms/DashboardHeader'
import { PublicationWorkspace } from '../../components/organisms/PublicationWorkspace'
import { Sidebar } from '../../components/organisms/Sidebar'
import { DashboardLayout } from '../../components/templates/DashboardLayout'
import { publicationCopy } from '../../content/publication'

export function PublicationPage() {
  return (
    <DashboardLayout
      sidebar={({ onNavigate }) => <Sidebar onNavigate={onNavigate} />}
      header={({ onMenuClick }) => (
        <DashboardHeader
          title={publicationCopy.pageTitle}
          subtitle={publicationCopy.pageSubtitle}
          onMenuClick={onMenuClick}
        />
      )}
    >
      <Breadcrumb
        items={[
          { label: 'Planeación académica' },
          { label: publicationCopy.pageTitle },
        ]}
      />
      <PublicationWorkspace />
    </DashboardLayout>
  )
}
