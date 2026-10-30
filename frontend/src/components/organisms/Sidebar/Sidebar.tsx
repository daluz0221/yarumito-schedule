import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../../auth'
import {
  sidebarAccountNav,
  sidebarPrimaryNav,
  sidebarSecondaryNav,
} from '../../../content/dashboard'
import { SidebarBrand } from '../../molecules/SidebarBrand'
import { SidebarNavGroup } from '../../molecules/SidebarNavGroup'
import styles from './Sidebar.module.css'

export type SidebarProps = {
  onNavigate?: () => void
}

export function Sidebar({ onNavigate }: SidebarProps) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const accountItems = sidebarAccountNav.map((item) =>
    item.id === 'logout'
      ? {
          ...item,
          onClick: () => {
            logout()
            navigate('/admin', { replace: true })
          },
        }
      : item,
  )

  return (
    <aside className={styles.sidebar}>
      <SidebarBrand
        title="Gestión de Horarios"
        school="I.E. Rural Yarumito"
        campus="Sede Principal"
      />

      <div className={styles.nav}>
        <SidebarNavGroup
          label="Módulos principales"
          items={sidebarPrimaryNav}
          onNavigate={onNavigate}
        />
        <div className={styles.divider} />
        <SidebarNavGroup
          label="Planificación"
          items={sidebarSecondaryNav}
          onNavigate={onNavigate}
        />
      </div>

      <div className={styles.account}>
        <SidebarNavGroup
          label="Cuenta"
          items={accountItems}
          onNavigate={onNavigate}
        />
      </div>
    </aside>
  )
}
