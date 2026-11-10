import { Avatar } from '../../atoms/Avatar'
import { ChevronRightIcon } from '../../atoms/icons'
import { Text } from '../../atoms/Text'
import styles from './UserChip.module.css'

export type UserChipProps = {
  initials: string
  role: string
  location: string
}

export function UserChip({ initials, role, location }: UserChipProps) {
  return (
    <button type="button" className={styles.chip}>
      <Avatar initials={initials} />
      <span className={styles.copy}>
        <Text variant="nav">{role}</Text>
        <Text variant="caption">{location}</Text>
      </span>
      <span className={styles.chevron}>
        <ChevronRightIcon />
      </span>
    </button>
  )
}
