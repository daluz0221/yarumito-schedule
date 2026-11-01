import { Text } from '../../atoms/Text'
import styles from './SidebarBrand.module.css'

export type SidebarBrandProps = {
  title: string
  school: string
  campus: string
}

export function SidebarBrand({ title, school, campus }: SidebarBrandProps) {
  return (
    <div className={styles.brand}>
      <Text variant="brand">{title}</Text>
      <Text variant="caption">{school}</Text>
      <Text variant="caption" className={styles.campus}>
        {campus}
      </Text>
    </div>
  )
}
