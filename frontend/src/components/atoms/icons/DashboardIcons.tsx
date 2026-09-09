import { IconFrame } from './IconFrame'

const stroke = {
  stroke: 'currentColor',
  strokeWidth: 1.7,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
}

export function HomeIcon() {
  return (
    <IconFrame>
      <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9.5Z" {...stroke} />
    </IconFrame>
  )
}

export function UsersIcon() {
  return (
    <IconFrame>
      <path d="M16 20v-1.2A3.8 3.8 0 0 0 12.2 15H7.8A3.8 3.8 0 0 0 4 18.8V20" {...stroke} />
      <circle cx="10" cy="8.5" r="3" {...stroke} />
      <path d="M20 20v-1.1A3.2 3.2 0 0 0 17.4 16" {...stroke} />
      <path d="M16.2 5.4a3 3 0 0 1 0 6.1" {...stroke} />
    </IconFrame>
  )
}

export function BookOpenIcon() {
  return (
    <IconFrame>
      <path d="M12 6.5c-2-1.4-4.6-2-7-2v13c2.4 0 5 .6 7 2 2-1.4 4.6-2 7-2v-13c-2.4 0-5 .6-7 2Z" {...stroke} />
      <path d="M12 6.5v13" {...stroke} />
    </IconFrame>
  )
}

export function CalendarIcon() {
  return (
    <IconFrame>
      <rect x="4" y="5.5" width="16" height="14.5" rx="2" {...stroke} />
      <path d="M4 10h16M8 4v3M16 4v3" {...stroke} />
    </IconFrame>
  )
}

export function BookIcon() {
  return (
    <IconFrame>
      <path d="M5 5.5A2.5 2.5 0 0 1 7.5 3H19v16.5H7.5A2.5 2.5 0 0 0 5 22V5.5Z" {...stroke} />
      <path d="M5 18.5h14" {...stroke} />
    </IconFrame>
  )
}

export function ListIcon() {
  return (
    <IconFrame>
      <path d="M9 7h11M9 12h11M9 17h11M5 7h.01M5 12h.01M5 17h.01" {...stroke} />
    </IconFrame>
  )
}

export function ClockIcon() {
  return (
    <IconFrame>
      <circle cx="12" cy="12" r="8" {...stroke} />
      <path d="M12 8v4.2L15 15" {...stroke} />
    </IconFrame>
  )
}

export function ClipboardIcon() {
  return (
    <IconFrame>
      <rect x="6" y="5" width="12" height="15" rx="2" {...stroke} />
      <path d="M9 5.2V4h6v1.2" {...stroke} />
      <path d="M9 11h6M9 15h4" {...stroke} />
    </IconFrame>
  )
}

export function GridIcon() {
  return (
    <IconFrame>
      <rect x="4" y="4" width="7" height="7" rx="1.2" {...stroke} />
      <rect x="13" y="4" width="7" height="7" rx="1.2" {...stroke} />
      <rect x="4" y="13" width="7" height="7" rx="1.2" {...stroke} />
      <rect x="13" y="13" width="7" height="7" rx="1.2" {...stroke} />
    </IconFrame>
  )
}

export function ShieldIcon() {
  return (
    <IconFrame>
      <path d="M12 3.5 19 6.5v6.2c0 4.3-2.9 6.7-7 8.3-4.1-1.6-7-4-7-8.3V6.5L12 3.5Z" {...stroke} />
    </IconFrame>
  )
}

export function SearchIcon() {
  return (
    <IconFrame>
      <circle cx="11" cy="11" r="6.2" {...stroke} />
      <path d="M16 16.2 20 20" {...stroke} />
    </IconFrame>
  )
}

export function UserIcon() {
  return (
    <IconFrame>
      <circle cx="12" cy="8.2" r="3.2" {...stroke} />
      <path d="M5.5 19.2c.8-3 3.2-4.6 6.5-4.6s5.7 1.6 6.5 4.6" {...stroke} />
    </IconFrame>
  )
}

export function LogoutIcon() {
  return (
    <IconFrame>
      <path d="M15 7.5V6a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2v-1.5" {...stroke} />
      <path d="M10 12h11M18 8.5 21.5 12 18 15.5" {...stroke} />
    </IconFrame>
  )
}

export function ChevronRightIcon() {
  return (
    <IconFrame size={18}>
      <path d="m9 6 6 6-6 6" {...stroke} />
    </IconFrame>
  )
}

export function MenuIcon() {
  return (
    <IconFrame>
      <path d="M4 7h16M4 12h16M4 17h16" {...stroke} />
    </IconFrame>
  )
}

export function CloseIcon() {
  return (
    <IconFrame>
      <path d="m6 6 12 12M18 6 6 18" {...stroke} />
    </IconFrame>
  )
}

export function LayersIcon() {
  return (
    <IconFrame>
      <path d="m12 4 8 4-8 4-8-4 8-4Z" {...stroke} />
      <path d="m4 12 8 4 8-4M4 16l8 4 8-4" {...stroke} />
    </IconFrame>
  )
}
