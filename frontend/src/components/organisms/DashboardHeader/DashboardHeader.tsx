import { IconButton } from '../../atoms/IconButton'
import { MenuIcon } from '../../atoms/icons'
import { PageHeading } from '../../molecules/PageHeading'
import { UserChip } from '../../molecules/UserChip'
import styles from './DashboardHeader.module.css'

export type DashboardHeaderProps = {
  title: string
  subtitle: string
  onMenuClick?: () => void
}

export function DashboardHeader({
  title,
  subtitle,
  onMenuClick,
}: DashboardHeaderProps) {
  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <span className={styles.menuButton}>
          <IconButton label="Abrir menú" onClick={onMenuClick}>
            <MenuIcon />
          </IconButton>
        </span>
        <PageHeading title={title} subtitle={subtitle} />
      </div>
      <UserChip
        initials="R"
        role="Rector / Administrador"
        location="Sede Principal"
      />
    </header>
  )
}
