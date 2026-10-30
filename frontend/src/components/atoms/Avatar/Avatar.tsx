import styles from './Avatar.module.css'

export type AvatarProps = {
  initials: string
  size?: 'sm' | 'md'
}

export function Avatar({ initials, size = 'md' }: AvatarProps) {
  const classes = [styles.avatar, styles[size]].join(' ')

  return <span className={classes}>{initials}</span>
}
