import type { NavItemData } from '../../../content/dashboard'
import { ChevronRightIcon } from '../../atoms/icons'
import { NavItem } from '../NavItem'
import { navIcons } from '../SidebarNavGroup/navIcons'
import styles from './SidebarCollapsibleGroup.module.css'

export type SidebarCollapsibleGroupProps = {
  id: string
  label: string
  items: NavItemData[]
  open: boolean
  // Algún ítem del grupo corresponde a la página actual.
  hasActive?: boolean
  onToggle: () => void
  onNavigate?: () => void
}

export function SidebarCollapsibleGroup({
  id,
  label,
  items,
  open,
  hasActive = false,
  onToggle,
  onNavigate,
}: SidebarCollapsibleGroupProps) {
  const panelId = `sidebar-group-${id}`
  const classes = [styles.header, hasActive ? styles.current : ''].filter(Boolean).join(' ')

  return (
    <div className={styles.group}>
      <button
        type="button"
        className={classes}
        aria-expanded={open}
        aria-controls={panelId}
        onClick={onToggle}
      >
        <span className={styles.label}>{label}</span>
        <span className={open ? `${styles.chevron} ${styles.chevronOpen}` : styles.chevron}>
          <ChevronRightIcon />
        </span>
      </button>
      <nav id={panelId} className={styles.items} aria-label={label} hidden={!open}>
        {items.map((item) => (
          <NavItem
            key={item.id}
            icon={navIcons[item.icon]}
            label={item.label}
            to={item.to}
            end={item.end}
            disabled={item.disabled}
            onClick={item.onClick}
            onNavigate={onNavigate}
          />
        ))}
      </nav>
    </div>
  )
}
