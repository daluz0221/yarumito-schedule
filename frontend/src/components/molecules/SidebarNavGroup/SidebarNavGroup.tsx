import type { NavItemData } from '../../../content/dashboard'
import { NavItem } from '../NavItem'
import { navIcons } from './navIcons'
import styles from './SidebarNavGroup.module.css'

export type SidebarNavGroupProps = {
  items: NavItemData[]
  label: string
  onNavigate?: () => void
}

export function SidebarNavGroup({
  items,
  label,
  onNavigate,
}: SidebarNavGroupProps) {
  return (
    <nav className={styles.group} aria-label={label}>
      {items.map((item) => (
        <NavItem
          key={item.id}
          icon={navIcons[item.icon]}
          label={item.label}
          to={item.to}
          end={item.end}
          disabled={item.disabled}
          indented={item.indented}
          onClick={item.onClick}
          onNavigate={onNavigate}
        />
      ))}
    </nav>
  )
}
