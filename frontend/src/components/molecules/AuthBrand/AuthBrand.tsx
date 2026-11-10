import escudoYarumito from '../../../assets/escudo-yarumito.png'
import { Logo } from '../../atoms/Logo'
import { Text } from '../../atoms/Text'
import styles from './AuthBrand.module.css'

export type AuthBrandProps = {
  title: string
  subtitle: string
  logoSrc?: string
  logoAlt?: string
}

export function AuthBrand({
  title,
  subtitle,
  logoSrc = escudoYarumito,
  logoAlt = 'Escudo de la Institución Educativa Yarumito',
}: AuthBrandProps) {
  return (
    <header className={styles.brand}>
      <Logo src={logoSrc} alt={logoAlt} />
      <div className={styles.copy}>
        <Text variant="title">{title}</Text>
        <Text variant="subtitle">{subtitle}</Text>
      </div>
    </header>
  )
}
