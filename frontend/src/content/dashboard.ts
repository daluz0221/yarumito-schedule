import type { StatusDotTone } from '../components/atoms/StatusDot'

export type NavIconName =
  | 'home'
  | 'users'
  | 'bookOpen'
  | 'calendar'
  | 'book'
  | 'list'
  | 'clock'
  | 'clipboard'
  | 'grid'
  | 'shield'
  | 'publish'
  | 'search'
  | 'user'
  | 'logout'

export type NavItemData = {
  id: string
  label: string
  icon: NavIconName
  to?: string
  end?: boolean
  disabled?: boolean
  indented?: boolean
  onClick?: () => void
}

export type ModuleIconName =
  | 'users'
  | 'layers'
  | 'calendar'
  | 'clock'
  | 'clipboard'
  | 'grid'
  | 'shield'
  | 'publish'
  | 'search'

export type ModuleItemData = {
  id: string
  title: string
  description: string
  icon: ModuleIconName
  available: boolean
  highlighted?: boolean
  to?: string
}

export type StatWash =
  | 'total'
  | 'active'
  | 'license'
  | 'retired'
  | 'danger'
  | 'warning'
  | 'success'
  | 'neutral'

export type StatBadgeTone = 'neutral' | 'success' | 'warning' | 'muted'

export type StatItemData = {
  id: string
  value: string
  label: string
  wash?: StatWash
  badge?: string
  badgeTone?: StatBadgeTone
}

export type AlertItemData = {
  id: string
  message: string
  tone: StatusDotTone
}

export type SidebarSection = {
  id: string
  label: string
  items: NavItemData[]
}

// Acceso directo al tablero, sin encabezado de grupo.
export const sidebarHomeNav: NavItemData[] = [
  { id: 'home', label: 'Inicio', icon: 'home', to: '/dashboard', end: true },
]

// Grupos plegables, ordenados según el flujo de trabajo:
// configurar la estructura, cargar el personal, planificar y controlar.
export const sidebarSections: SidebarSection[] = [
  {
    id: 'institutional',
    label: 'Estructura institucional',
    items: [
      { id: 'sedes', label: 'Sedes', icon: 'bookOpen', to: '/dashboard/sedes' },
      { id: 'aulas', label: 'Aulas', icon: 'grid', to: '/dashboard/aulas' },
      { id: 'turnos', label: 'Turnos', icon: 'clock', to: '/dashboard/turnos' },
      {
        id: 'franjas',
        label: 'Franjas horarias',
        icon: 'list',
        to: '/dashboard/franjas',
      },
    ],
  },
  {
    id: 'academic',
    label: 'Estructura académica',
    items: [
      {
        id: 'year',
        label: 'Año lectivo',
        icon: 'calendar',
        to: '/dashboard/anio-lectivo',
      },
      { id: 'grados', label: 'Grados', icon: 'list', to: '/dashboard/grados' },
      { id: 'groups', label: 'Grupos', icon: 'users', to: '/dashboard/grupos' },
      {
        id: 'areas',
        label: 'Áreas y asignaturas',
        icon: 'book',
        to: '/dashboard/areas',
      },
      {
        id: 'plan',
        label: 'Plan de estudios',
        icon: 'list',
        to: '/dashboard/plan-estudios',
      },
    ],
  },
  {
    id: 'staff',
    label: 'Personal docente',
    items: [
      { id: 'teachers', label: 'Docentes', icon: 'users', to: '/dashboard/docentes' },
      {
        id: 'restrictions',
        label: 'Restricciones docentes',
        icon: 'clock',
        to: '/dashboard/restricciones',
      },
    ],
  },
  {
    id: 'planning',
    label: 'Planificación',
    items: [
      {
        id: 'assignment',
        label: 'Asignación académica',
        icon: 'clipboard',
        to: '/dashboard/asignacion',
      },
      {
        id: 'schedule',
        label: 'Construcción de horario',
        icon: 'grid',
        to: '/dashboard/horario',
      },
    ],
  },
  {
    id: 'control',
    label: 'Control y salida',
    items: [
      {
        id: 'validations',
        label: 'Validaciones',
        icon: 'shield',
        to: '/dashboard/validaciones',
      },
      {
        id: 'publication',
        label: 'Publicación',
        icon: 'publish',
        to: '/dashboard/publicacion',
      },
      { id: 'audit', label: 'Auditoría', icon: 'search', to: '/dashboard/auditoria' },
      { id: 'queries', label: 'Consultas', icon: 'search', to: '/dashboard/consultas' },
      {
        id: 'reports',
        label: 'Informes',
        icon: 'clipboard',
        to: '/dashboard/informes',
      },
    ],
  },
]

export const sidebarAccountNav: NavItemData[] = [
  { id: 'profile', label: 'Perfil', icon: 'user' },
  { id: 'logout', label: 'Cerrar sesión', icon: 'logout' },
]

export const dashboardModules: ModuleItemData[] = [
  {
    id: 'teachers',
    title: 'Gestión de docentes',
    description: 'Registre y actualice la información de los docentes de la institución.',
    icon: 'users',
    available: true,
    to: '/dashboard/docentes',
  },
  {
    id: 'academic',
    title: 'Gestión académica',
    description: 'Configure grados, áreas, asignaturas y el plan de estudios.',
    icon: 'layers',
    available: true,
    to: '/dashboard/areas',
  },
  {
    id: 'year',
    title: 'Año lectivo',
    description: 'Defina el año lectivo activo y sus parámetros institucionales.',
    icon: 'calendar',
    available: true,
    to: '/dashboard/anio-lectivo',
  },
  {
    id: 'groups',
    title: 'Grupos',
    description: 'Cree y consulte los grupos de cada grado por año lectivo.',
    icon: 'users',
    available: true,
    to: '/dashboard/grupos',
  },
  {
    id: 'restrictions',
    title: 'Restricciones docentes',
    description:
      'Indique disponibilidad, horas máximas y materias que puede dictar cada docente.',
    icon: 'clock',
    available: true,
    to: '/dashboard/restricciones',
  },
  {
    id: 'assignment',
    title: 'Asignación académica',
    description: 'Asigne docentes a grupos y materias según el plan de estudios.',
    icon: 'clipboard',
    available: true,
    to: '/dashboard/asignacion',
  },
  {
    id: 'schedule',
    title: 'Construcción del horario',
    description: 'Genere y ajuste la grilla semanal aplicando las reglas institucionales.',
    icon: 'grid',
    available: true,
    to: '/dashboard/horario',
  },
  {
    id: 'validations',
    title: 'Validaciones',
    description: 'Revise conflictos, horas incompletas y reglas incumplidas del horario.',
    icon: 'shield',
    available: true,
    to: '/dashboard/validaciones',
  },
  {
    id: 'publication',
    title: 'Publicación del horario',
    description: 'Revisa el resultado final y publica la versión del horario académico.',
    icon: 'publish',
    available: true,
    to: '/dashboard/publicacion',
  },
  {
    id: 'audit',
    title: 'Auditoría',
    description: 'Consulta el historial de cambios por entidad o por usuario.',
    icon: 'search',
    available: true,
    to: '/dashboard/auditoria',
  },
  {
    id: 'queries',
    title: 'Consultas',
    description: 'Consulte el horario institucional o publicado de un grupo y descárguelo.',
    icon: 'search',
    available: true,
    to: '/dashboard/consultas',
  },
  {
    id: 'reports',
    title: 'Informes institucionales',
    description:
      'Consulta la planta docente, las áreas y asignaturas, y los perfiles profesionales.',
    icon: 'clipboard',
    available: true,
    to: '/dashboard/informes',
  },
]
