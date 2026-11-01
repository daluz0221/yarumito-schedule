import { Text } from '../../atoms/Text'
import styles from './WelcomeSection.module.css'

export function WelcomeSection() {
  return (
    <section className={styles.section}>
      <Text variant="display">Bienvenido al Sistema de Gestión de Horarios</Text>
      <Text variant="body">
        Administre la información académica y construya los horarios de la
        institución teniendo en cuenta las reglas de negocio y las condiciones de
        cada período lectivo.
      </Text>
    </section>
  )
}
