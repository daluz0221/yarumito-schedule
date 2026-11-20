import type { ReactNode } from 'react'
import type { NavIconName } from '../../../content/dashboard'
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
  PublishIcon,
  SearchIcon,
  ShieldIcon,
  UserIcon,
  UsersIcon,
} from '../../atoms/icons'

export const navIcons: Record<NavIconName, ReactNode> = {
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
  publish: <PublishIcon />,
  search: <SearchIcon />,
  user: <UserIcon />,
  logout: <LogoutIcon />,
}
