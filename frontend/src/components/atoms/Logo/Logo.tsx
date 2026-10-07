import styles from './Logo.module.css'

export type LogoProps = {
  src: string
  alt: string
  width?: number
}

export function Logo({ src, alt, width = 92 }: LogoProps) {
  return <img className={styles.logo} src={src} alt={alt} width={width} />
}
