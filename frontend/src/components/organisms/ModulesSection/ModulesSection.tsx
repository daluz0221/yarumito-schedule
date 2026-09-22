import type { ReactNode } from 'react'
import {
  dashboardModules,
  type ModuleIconName,
} from '../../../content/dashboard'
import {
  CalendarIcon,
  ClipboardIcon,
  ClockIcon,
  GridIcon,
  LayersIcon,
  SearchIcon,
  ShieldIcon,
  UsersIcon,
} from '../../atoms/icons'
import { Text } from '../../atoms/Text'
import { ModuleCard } from '../../molecules/ModuleCard'
import styles from './ModulesSection.module.css'

const moduleIcons: Record<ModuleIconName, ReactNode> = {
  users: <UsersIcon />,
  layers: <LayersIcon />,
  calendar: <CalendarIcon />,
  clock: <ClockIcon />,
  clipboard: <ClipboardIcon />,
  grid: <GridIcon />,
  shield: <ShieldIcon />,
  search: <SearchIcon />,
}

export function ModulesSection() {
  return (
    <section className={styles.section}>
      <Text variant="sectionTitle">Módulos principales</Text>
      <div className={styles.grid}>
        {dashboardModules.map((module) => (
          <ModuleCard
            key={module.id}
            icon={moduleIcons[module.icon]}
            title={module.title}
            description={module.description}
            available={module.available}
            highlighted={module.highlighted}
            to={module.to}
          />
        ))}
      </div>
    </section>
  )
}
