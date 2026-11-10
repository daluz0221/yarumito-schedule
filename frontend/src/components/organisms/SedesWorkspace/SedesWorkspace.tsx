import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarSede, listarCatalogoSedes } from '../../../api'
import type { SedeResponse } from '../../../api/sedes'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SedeFormModal } from '../SedeFormModal'
import { SedesTable } from '../SedesTable'
import styles from './SedesWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Esta sede ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar la sede.'
}

function coincide(sede: SedeResponse, query: string) {
  const texto = query.trim().toLowerCase()

  if (!texto) {
    return true
  }

  return [sede.nombre, sede.codigo, sede.direccion]
    .filter(Boolean)
    .some((valor) => valor!.toLowerCase().includes(texto))
}

export function SedesWorkspace() {
  const [sedes, setSedes] = useState<SedeResponse[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<
    { name: 'closed' } | { name: 'create' } | { name: 'edit'; id: string }
  >({ name: 'closed' })
  const [editing, setEditing] = useState<SedeResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoSedes(controller.signal)
      .then((items) => {
        setSedes(items)
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setSedes([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de sedes.',
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

  const loadSede = (sedeId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: sedeId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarSede(sedeId, controller.signal)
      .then((sede) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(sede)
        setSedes((current) =>
          current.map((item) => (item.id === sede.id ? sede : item)),
        )
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          setSedes((current) => current.filter((item) => item.id !== sedeId))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  const rows = sedes
    .filter((sede) => coincide(sede, query))
    .sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'))

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Sedes
        </Text>
        <Text variant="body">
          Registra y actualiza las sedes de la institución. La sede principal se
          usa como referencia en horarios, validaciones y publicación.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Card className={styles.section}>
        <SectionHeader
          title="Catálogo de sedes"
          description="Consulta una sede para editar su nombre, código, dirección o si es la principal."
          action={
            <Button variant="primary" size="sm" onClick={() => setModal({ name: 'create' })}>
              + Nueva sede
            </Button>
          }
        />

        <div className={styles.search}>
          <FormField id="sede-search" label="Buscar sede">
            <TextInput
              id="sede-search"
              name="query"
              inputSize="sm"
              placeholder="Buscar por código, nombre o dirección..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </FormField>
        </div>

        <div className={styles.tableHost}>
          <SedesTable
            rows={rows}
            emptyMessage={
              loading
                ? 'Cargando sedes...'
                : sedes.length === 0
                  ? 'Aún no hay sedes registradas.'
                  : 'No se encontraron sedes con esa búsqueda.'
            }
            onEdit={(sede) => loadSede(sede.id)}
          />
        </div>
      </Card>

      <SedeFormModal
        open={modal.name !== 'closed'}
        mode={modal.name === 'edit' ? 'edit' : 'create'}
        sedes={sedes}
        sede={editing ?? undefined}
        loading={modal.name === 'edit' && editLoading}
        loadError={modal.name === 'edit' ? editError : ''}
        onRetry={modal.name === 'edit' ? () => loadSede(modal.id) : undefined}
        onClose={closeModal}
        onCreated={(sede) => {
          setSedes((current) =>
            current.some((item) => item.id === sede.id) ? current : [...current, sede],
          )
          setError('')
        }}
        onUpdated={(sede) => {
          setSedes((current) =>
            current.map((item) => (item.id === sede.id ? sede : item)),
          )
          setError('')
        }}
      />
    </section>
  )
}
