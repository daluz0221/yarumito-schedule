import type { ReactNode } from 'react'
import { Text } from '../../atoms/Text'
import styles from './FormSection.module.css'

export type FormSectionProps = {
  title: string
  description: string
  children: ReactNode
}

export function FormSection({ title, description, children }: FormSectionProps) {
  return (
    <section className={styles.section}>
      <header className={styles.header}>
        <Text variant="nav" as="h3" className={styles.title}>
          {title}
        </Text>
        <Text variant="body">{description}</Text>
      </header>
      <div className={styles.grid}>{children}</div>
    </section>
  )
}
