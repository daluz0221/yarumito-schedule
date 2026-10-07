import { TextLink } from '../../atoms/TextLink'
import styles from './Breadcrumb.module.css'

export type BreadcrumbItem = {
  label: string
  to?: string
}

export type BreadcrumbProps = {
  items: BreadcrumbItem[]
}

export function Breadcrumb({ items }: BreadcrumbProps) {
  return (
    <nav className={styles.nav} aria-label="Migas de pan">
      <ol className={styles.list}>
        {items.map((item, index) => {
          const isLast = index === items.length - 1

          return (
            <li key={`${item.label}-${index}`} className={styles.item}>
              {item.to && !isLast ? (
                <TextLink to={item.to}>{item.label}</TextLink>
              ) : (
                <span className={isLast ? styles.current : undefined}>
                  {item.label}
                </span>
              )}
              {isLast ? null : <span className={styles.separator}>/</span>}
            </li>
          )
        })}
      </ol>
    </nav>
  )
}
