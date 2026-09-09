import type { ReactNode } from 'react'
import type { NavIconName, NavItemData } from '../../../content/dashboard'
import {
  BookIcon,
  BookOpenIcon,
  CalendarIcon,
  ClipboardIcon,
  ClockIcon,
  GridIcon,
  HomeIcon,
  ListIcon,
  LogoutIcon,
  SearchIcon,
  ShieldIcon,
  UserIcon,
  UsersIcon,
} from '../../atoms/icons'
import { NavItem } from '../NavItem'
import styles from './SidebarNavGroup.module.css'

const navIcons: Record<NavIconName, ReactNode> = {
  home: <HomeIcon />,
  users: <UsersIcon />,
  bookOpen: <BookOpenIcon />,
  calendar: <CalendarIcon />,
  book: <BookIcon />,
  list: <ListIcon />,
  clock: <ClockIcon />,
  clipboard: <ClipboardIcon />,
  grid: <GridIcon />,
  shield: <ShieldIcon />,
  search: <SearchIcon />,
  user: <UserIcon />,
  logout: <LogoutIcon />,
}

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
          disabled={item.disabled}
          indented={item.indented}
          onNavigate={onNavigate}
        />
      ))}
    </nav>
  )
}
