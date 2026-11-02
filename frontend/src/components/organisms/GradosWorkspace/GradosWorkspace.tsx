import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarGrado, listarCatalogoGrados } from '../../../api'
import type { GradoResponse } from '../../../api/grados'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { GradoFormModal } from '../GradoFormModal'
import { GradosTable } from '../GradosTable'
import styles from './GradosWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este grado ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el grado.'
}

export function GradosWorkspace() {
  const [grados, setGrados] = useState<GradoResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<
    { name: 'closed' } | { name: 'create' } | { name: 'edit'; id: string }
  >({ name: 'closed' })
  const [editing, setEditing] = useState<GradoResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoGrados(controller.signal)
      .then(setGrados)
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setGrados([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de grados.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    return () => detailRequest.current?.abort()
  }, [])

  const closeModal = () => {
    detailRequest.current?.abort()
    detailRequest.current = null
    setModal({ name: 'closed' })
    setEditing(null)
    setEditLoading(false)
    setEditError('')
  }

  const loadGrado = (gradoId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: gradoId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarGrado(gradoId, controller.signal)
      .then((grado) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(grado)
        setGrados((current) =>
          current.some((item) => item.id === grado.id)
            ? current.map((item) => (item.id === grado.id ? grado : item))
            : [...current, grado],
        )
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          setGrados((current) => current.filter((item) => item.id !== gradoId))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  const guardar = (grado: GradoResponse) => {
    setGrados((current) =>
      current.some((item) => item.id === grado.id)
        ? current.map((item) => (item.id === grado.id ? grado : item))
        : [...current, grado],
    )
    setError('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Grados
        </Text>
        <Text variant="body">
          Registra los grados de 6° a 11°. El nombre, el tipo de educación, la
          prioridad y las horas semanales se calculan al guardar.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Card className={styles.section}>
        <SectionHeader
          title="Catálogo de grados"
          description="Consulta un grado para cambiar su nivel. El servidor recalcula los datos asociados."
          action={
            <Button variant="primary" size="sm" onClick={() => setModal({ name: 'create' })}>
              + Nuevo grado
            </Button>
          }
        />

        <div className={styles.tableHost}>
          <GradosTable
            rows={[...grados].sort((left, right) => left.nivel - right.nivel)}
            emptyMessage={
              loading ? 'Cargando grados...' : 'Aún no hay grados registrados.'
            }
            onEdit={(grado) => loadGrado(grado.id)}
          />
        </div>
      </Card>

      <GradoFormModal
        open={modal.name !== 'closed'}
        mode={modal.name === 'edit' ? 'edit' : 'create'}
        grados={grados}
        grado={editing ?? undefined}
        loading={modal.name === 'edit' && editLoading}
        loadError={modal.name === 'edit' ? editError : ''}
        onRetry={modal.name === 'edit' ? () => loadGrado(modal.id) : undefined}
        onClose={closeModal}
        onCreated={guardar}
        onUpdated={guardar}
      />
    </section>
  )
}
