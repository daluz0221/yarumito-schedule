import { PENDING_BACKEND_LABEL } from '../../../content/teachers'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import styles from './TeacherTitlesSection.module.css'

export function TeacherTitlesSection() {
  return (
    <section className={styles.section}>
      <SectionHeader
        title="Títulos profesionales"
        description="Formación académica registrada como soporte del perfil profesional del docente."
      />
      <Card padding="sm" className={styles.card}>
        <p className={styles.pending}>{PENDING_BACKEND_LABEL}</p>
        <Text variant="body">
          El backend aún no expone un API de títulos profesionales. Esta
          sección se habilitará cuando exista ese catálogo.
        </Text>
      </Card>
    </section>
  )
}
