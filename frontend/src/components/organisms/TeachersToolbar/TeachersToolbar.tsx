import { useNavigate } from 'react-router-dom'
import { Button } from '../../atoms/Button'
import { Text } from '../../atoms/Text'
import styles from './TeachersToolbar.module.css'

export type TeachersToolbarProps = {
  onRefresh: () => void
}

export function TeachersToolbar({ onRefresh }: TeachersToolbarProps) {
  const navigate = useNavigate()

  return (
    <section className={styles.toolbar}>
      <div className={styles.copy}>
        <Text variant="sectionTitle">Docentes registrados</Text>
        <Text variant="body">
          Consulte, busque y administre la información de los docentes de la
          institución.
        </Text>
      </div>
      <div className={styles.actions}>
        <Button variant="muted" size="sm" onClick={onRefresh}>
          Actualizar listado
        </Button>
        <Button
          variant="primary"
          size="sm"
          onClick={() => navigate('/dashboard/docentes/registrar')}
        >
          Registrar docente
        </Button>
      </div>
    </section>
  )
}
