import type { ReactNode } from 'react'
import { Text } from '../../atoms/Text'
import styles from './SectionHeader.module.css'

export type SectionHeaderProps = {
  title: string
  description: string
  action?: ReactNode
}

export function SectionHeader({
  title,
  description,
  action,
}: SectionHeaderProps) {
  return (
    <div className={styles.header}>
      <div className={styles.copy}>
        <Text variant="sectionTitle">{title}</Text>
        <Text variant="body">{description}</Text>
      </div>
      {action ? <div className={styles.action}>{action}</div> : null}
    </div>
  )
}
