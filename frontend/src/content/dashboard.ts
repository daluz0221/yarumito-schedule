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

export type StatItemData = {
  id: string
  value: string
  label: string
}

export type AlertItemData = {
  id: string
  message: string
  tone: StatusDotTone
}

export const sidebarPrimaryNav: NavItemData[] = [
  { id: 'home', label: 'Inicio', icon: 'home', to: '/dashboard', end: true },
  { id: 'teachers', label: 'Docentes', icon: 'users', to: '/dashboard/docentes' },
  { id: 'academic', label: 'Gestión académica', icon: 'bookOpen' },
  {
    id: 'year',
    label: 'Año lectivo y grupos',
    icon: 'calendar',
    to: '/dashboard/anio-lectivo',
  },
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
]

export const sidebarSecondaryNav: NavItemData[] = [
  {
    id: 'restrictions',
    label: 'Restricciones docentes',
    icon: 'clock',
    to: '/dashboard/restricciones',
  },
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
  {
    id: 'validations',
    label: 'Validaciones',
    icon: 'shield',
    to: '/dashboard/validaciones',
    indented: true,
  },
  {
    id: 'publication',
    label: 'Publicación',
    icon: 'publish',
    to: '/dashboard/publicacion',
    indented: true,
  },
  { id: 'queries', label: 'Consultas', icon: 'search', disabled: true, indented: true },
]

export const sidebarAccountNav: NavItemData[] = [
  { id: 'profile', label: 'Perfil', icon: 'user' },
  { id: 'logout', label: 'Cerrar sesión', icon: 'logout' },
]

export const dashboardStats: StatItemData[] = [
  { id: 'teachers', value: '12', label: 'Docentes activos' },
  { id: 'groups', value: '10', label: 'Grupos activos' },
  { id: 'areas', value: '9', label: 'Áreas académicas' },
  { id: 'year', value: '2026', label: 'Año lectivo' },
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
    title: 'Año lectivo y grupos',
    description: 'Defina el año lectivo activo y cree los grupos de cada grado.',
    icon: 'calendar',
    available: true,
    to: '/dashboard/anio-lectivo',
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
    id: 'queries',
    title: 'Consultas',
    description: 'Consulte horarios por grupo, docente o jornada cuando el proceso esté listo.',
    icon: 'search',
    available: false,
  },
]

export const dashboardAlerts: AlertItemData[] = [
  {
    id: 'teachers-open',
    message: '2 docentes sin restricciones: disponibles',
    tone: 'warning',
  },
  {
    id: 'plan-pending',
    message: '1 grado con plan de estudios pendiente',
    tone: 'alert',
  },
  {
    id: 'validation-pending',
    message: 'Validación horaria pendiente',
    tone: 'success',
  },
]
