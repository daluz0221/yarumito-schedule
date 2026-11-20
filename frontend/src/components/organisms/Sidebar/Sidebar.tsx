import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../../auth'
import {
  sidebarAccountNav,
  sidebarHomeNav,
  sidebarSections,
  type NavItemData,
} from '../../../content/dashboard'
import { SidebarCollapsibleGroup } from '../../molecules/SidebarCollapsibleGroup'
import { SidebarBrand } from '../../molecules/SidebarBrand'
import { SidebarNavGroup } from '../../molecules/SidebarNavGroup'
import styles from './Sidebar.module.css'

export type SidebarProps = {
  onNavigate?: () => void
}

const STORAGE_KEY = 'yarumito.sidebar.groups'

// Estado elegido por el usuario por grupo: true abierto, false plegado.
type GroupOverrides = Record<string, boolean>

function readOverrides(): GroupOverrides {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    const parsed: unknown = raw ? JSON.parse(raw) : {}

    return parsed && typeof parsed === 'object' ? (parsed as GroupOverrides) : {}
  } catch {
    return {}
  }
}

function writeOverrides(value: GroupOverrides) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  } catch {
    // Sin almacenamiento disponible: el estado vive solo en esta sesión.
  }
}

function isCurrent(item: NavItemData, pathname: string) {
  if (!item.to) {
    return false
  }

  return item.end
    ? pathname === item.to
    : pathname === item.to || pathname.startsWith(`${item.to}/`)
}

export function Sidebar({ onNavigate }: SidebarProps) {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [overrides, setOverrides] = useState<GroupOverrides>(readOverrides)
  const [lastPath, setLastPath] = useState(pathname)

  const activeGroupId =
    sidebarSections.find((section) => section.items.some((item) => isCurrent(item, pathname)))
      ?.id ?? null

  // Al navegar, el grupo de la página abierta vuelve a mostrarse expandido.
  if (pathname !== lastPath) {
    setLastPath(pathname)

    if (activeGroupId && overrides[activeGroupId] === false) {
      const next = { ...overrides, [activeGroupId]: true }

      setOverrides(next)
      writeOverrides(next)
    }
  }

  const isOpen = (id: string) => overrides[id] ?? id === activeGroupId

  const toggleGroup = (id: string) => {
    const next = { ...overrides, [id]: !isOpen(id) }

    setOverrides(next)
    writeOverrides(next)
  }

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
        <SidebarNavGroup label="Inicio" items={sidebarHomeNav} onNavigate={onNavigate} />
        {sidebarSections.map((section) => (
          <SidebarCollapsibleGroup
            key={section.id}
            id={section.id}
            label={section.label}
            items={section.items}
            open={isOpen(section.id)}
            hasActive={section.id === activeGroupId}
            onToggle={() => toggleGroup(section.id)}
            onNavigate={onNavigate}
          />
        ))}
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
