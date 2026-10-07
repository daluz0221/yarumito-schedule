import { Badge } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './TeacherInfoPanel.module.css'

const associatedTopics = [
  'Datos generales',
  'Títulos profesionales',
  'Idoneidad profesional',
  'Asignación académica',
]

export function TeacherInfoPanel() {
  return (
    <Card className={styles.panel}>
      <Text variant="sectionTitle">Información asociada al docente</Text>
      <div className={styles.content}>
        <div className={styles.chips}>
          {associatedTopics.map((topic) => (
            <Badge key={topic} tone="outline">
              {topic}
            </Badge>
          ))}
        </div>
        <Text variant="body">
          La información del docente será utilizada posteriormente en los
          procesos de asignación académica, validación de idoneidad y
          construcción del horario.
        </Text>
      </div>
    </Card>
  )
}
