import { useEffect, useState, type ReactNode } from 'react'
import { CloseIcon } from '../../atoms/icons'
import { IconButton } from '../../atoms/IconButton'
import styles from './DashboardLayout.module.css'

export type DashboardLayoutProps = {
  sidebar: (helpers: { onNavigate: () => void }) => ReactNode
  header: (helpers: { onMenuClick: () => void }) => ReactNode
  children: ReactNode
}

export function DashboardLayout({
  sidebar,
  header,
  children,
}: DashboardLayoutProps) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  const closeSidebar = () => setIsSidebarOpen(false)
  const openSidebar = () => setIsSidebarOpen(true)

  useEffect(() => {
    if (!isSidebarOpen) {
      return
    }

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        closeSidebar()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', onKeyDown)

    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [isSidebarOpen])

  return (
    <div className={styles.shell}>
      <div
        className={[styles.sidebarSlot, isSidebarOpen ? styles.open : '']
          .filter(Boolean)
          .join(' ')}
      >
        <div className={styles.sidebarClose}>
          <IconButton label="Cerrar menú" onClick={closeSidebar}>
            <CloseIcon />
          </IconButton>
        </div>
        {sidebar({ onNavigate: closeSidebar })}
      </div>

      {isSidebarOpen ? (
        <button
          type="button"
          className={styles.backdrop}
          aria-label="Cerrar menú"
          onClick={closeSidebar}
        />
      ) : null}

      <div className={styles.main}>
        {header({ onMenuClick: openSidebar })}
        <div className={styles.body}>{children}</div>
      </div>
    </div>
  )
}
