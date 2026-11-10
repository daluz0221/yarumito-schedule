import { Text } from '../../atoms/Text'
import { RegisterTeacherForm } from '../RegisterTeacherForm'
import styles from './RegisterTeacherWorkspace.module.css'

export function RegisterTeacherWorkspace() {
  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text variant="sectionTitle">Nuevo docente</Text>
        <Text variant="body">
          Ingrese la información necesaria para registrar al docente en el
          sistema.
        </Text>
      </div>
      <RegisterTeacherForm />
    </section>
  )
}
