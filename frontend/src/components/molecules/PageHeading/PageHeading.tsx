import { Text } from '../../atoms/Text'
import styles from './PageHeading.module.css'

export type PageHeadingProps = {
  title: string
  subtitle: string
}

export function PageHeading({ title, subtitle }: PageHeadingProps) {
  return (
    <div className={styles.heading}>
      <Text variant="pageTitle">{title}</Text>
      <Text variant="caption">{subtitle}</Text>
    </div>
  )
}
