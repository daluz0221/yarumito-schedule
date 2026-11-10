import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { ApiError, consultarDocente } from '../../../api'
import { teacherToForm, type Teacher } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { RegisterTeacherForm } from '../RegisterTeacherForm'
import styles from './EditTeacherWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function EditTeacherWorkspace() {
  const { teacherId } = useParams()
  const [teacher, setTeacher] = useState<Teacher | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!teacherId) {
      setTeacher(null)
      setError('No se indicó el docente a editar.')
      setLoading(false)
      return
    }

    const controller = new AbortController()

    setLoading(true)
    setError('')

    consultarDocente(teacherId, controller.signal)
      .then(setTeacher)
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTeacher(null)
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el docente.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [teacherId])

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text variant="sectionTitle">Editar docente</Text>
        <Text variant="body">
          Actualice la información básica e institucional del docente.
        </Text>
      </div>

      {loading ? (
        <p className={styles.status}>Cargando docente...</p>
      ) : error || !teacher ? (
        <p className={styles.error} role="alert">
          {error || 'El docente no existe.'}{' '}
          <TextLink to="/dashboard/docentes">Volver al listado</TextLink>
        </p>
      ) : (
        <RegisterTeacherForm
          mode="edit"
          teacherId={teacher.id}
          initialValues={teacherToForm(teacher)}
        />
      )}
    </section>
  )
}
