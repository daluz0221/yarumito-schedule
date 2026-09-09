import type { ElementType, ReactNode } from 'react'
import styles from './Text.module.css'

export type TextVariant =
  | 'title'
  | 'subtitle'
  | 'display'
  | 'pageTitle'
  | 'sectionTitle'
  | 'body'
  | 'caption'
  | 'stat'
  | 'nav'
  | 'brand'

const defaultTags: Record<TextVariant, ElementType> = {
  title: 'h1',
  subtitle: 'p',
  display: 'h2',
  pageTitle: 'h1',
  sectionTitle: 'h2',
  body: 'p',
  caption: 'p',
  stat: 'p',
  nav: 'span',
  brand: 'p',
}

export type TextProps = {
  as?: ElementType
  variant: TextVariant
  children: ReactNode
  className?: string
}

export function Text({ as, variant, children, className }: TextProps) {
  const Component = as ?? defaultTags[variant]
  const classes = [styles.text, styles[variant], className ?? '']
    .filter(Boolean)
    .join(' ')

  return <Component className={classes}>{children}</Component>
}
