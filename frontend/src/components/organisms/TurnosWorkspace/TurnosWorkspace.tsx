import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarTurno, listarCatalogoTurnos } from '../../../api'
import type { TurnoResponse } from '../../../api/turnos'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { TurnoFormModal } from '../TurnoFormModal'
import { TurnosTable } from '../TurnosTable'
import styles from './TurnosWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este turno ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el turno.'
}

function coincide(turno: TurnoResponse, query: string) {
  const texto = query.trim().toLowerCase()

  if (!texto) {
    return true
  }

  return turno.nombre.toLowerCase().includes(texto)
}

export function TurnosWorkspace() {
  const [turnos, setTurnos] = useState<TurnoResponse[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<
    { name: 'closed' } | { name: 'create' } | { name: 'edit'; id: string }
  >({ name: 'closed' })
  const [editing, setEditing] = useState<TurnoResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoTurnos(controller.signal)
      .then((items) => {
        setTurnos(items)
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTurnos([])
        setError(
          cause instanceof ApiError ? cause.message : 'No se pudo cargar el catálogo de turnos.',
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

  const loadTurno = (turnoId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: turnoId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarTurno(turnoId, controller.signal)
      .then((turno) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(turno)
        setTurnos((current) => current.map((item) => (item.id === turno.id ? turno : item)))
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          setTurnos((current) => current.filter((item) => item.id !== turnoId))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  const rows = turnos.filter((turno) => coincide(turno, query))

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Turnos
        </Text>
        <Text variant="body">
          Define las jornadas de la institución: horario, duración de cada clase y el descanso.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Card className={styles.section}>
        <SectionHeader
          title="Catálogo de turnos"
          description="Consulta un turno para editar su nombre, horario o la estructura de la jornada."
          action={
            <Button variant="primary" size="sm" onClick={() => setModal({ name: 'create' })}>
              + Nuevo turno
            </Button>
          }
        />

        <div className={styles.search}>
          <FormField id="turno-search" label="Buscar turno">
            <TextInput
              id="turno-search"
              name="query"
              inputSize="sm"
              placeholder="Buscar por nombre..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </FormField>
        </div>

        <div className={styles.tableHost}>
          <TurnosTable
            rows={rows}
            emptyMessage={
              loading
                ? 'Cargando turnos...'
                : turnos.length === 0
                  ? 'Aún no hay turnos registrados.'
                  : 'No se encontraron turnos con esa búsqueda.'
            }
            onEdit={(turno) => loadTurno(turno.id)}
          />
        </div>
      </Card>

      <TurnoFormModal
        open={modal.name !== 'closed'}
        mode={modal.name === 'edit' ? 'edit' : 'create'}
        turnos={turnos}
        turno={editing ?? undefined}
        loading={modal.name === 'edit' && editLoading}
        loadError={modal.name === 'edit' ? editError : ''}
        onRetry={modal.name === 'edit' ? () => loadTurno(modal.id) : undefined}
        onClose={closeModal}
        onCreated={(turno) => {
          setTurnos((current) =>
            current.some((item) => item.id === turno.id) ? current : [...current, turno],
          )
          setError('')
        }}
        onUpdated={(turno) => {
          setTurnos((current) => current.map((item) => (item.id === turno.id ? turno : item)))
          setError('')
        }}
      />
    </section>
  )
}
