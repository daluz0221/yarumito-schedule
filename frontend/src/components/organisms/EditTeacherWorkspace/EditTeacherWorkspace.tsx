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
  const [loadedId, setLoadedId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const loading = Boolean(teacherId) && loadedId !== teacherId
  const visibleTeacher = teacher?.id === teacherId ? teacher : null
  const visibleError = teacherId ? (loadedId === teacherId ? error : '') : 'No se indicó el docente a editar.'

  useEffect(() => {
    if (!teacherId) {
      return
    }

    const controller = new AbortController()
    const requestedId = teacherId

    consultarDocente(requestedId, controller.signal)
      .then((nextTeacher) => {
        setTeacher(nextTeacher)
        setError('')
        setLoadedId(requestedId)
      })
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
        setLoadedId(requestedId)
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
      ) : visibleError || !visibleTeacher ? (
        <p className={styles.error} role="alert">
          {visibleError || 'El docente no existe.'}{' '}
          <TextLink to="/dashboard/docentes">Volver al listado</TextLink>
        </p>
      ) : (
        <RegisterTeacherForm
          mode="edit"
          teacherId={visibleTeacher.id}
          initialValues={teacherToForm(visibleTeacher)}
        />
      )}
    </section>
  )
}
