import type { ElementType, ReactNode } from 'react'
import styles from './Card.module.css'

export type CardPadding = 'sm' | 'md' | 'lg'

export type CardProps = {
  as?: ElementType
  children: ReactNode
  padding?: CardPadding
  highlighted?: boolean
  className?: string
}

export function Card({
  as: Component = 'section',
  children,
  padding = 'md',
  highlighted = false,
  className,
}: CardProps) {
  const classes = [
    styles.card,
    styles[padding],
    highlighted ? styles.highlighted : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ')

  return <Component className={classes}>{children}</Component>
}
