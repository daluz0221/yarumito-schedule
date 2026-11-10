import { useEffect, useRef, useState } from 'react'
import {
  ApiError,
  consultarFranjaHoraria,
  listarCatalogoTurnos,
  listarFranjasPorTurno,
} from '../../../api'
import type { FranjaHorariaResponse } from '../../../api/franjasHorarias'
import type { TurnoResponse } from '../../../api/turnos'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { FranjaFormModal } from '../FranjaFormModal'
import { FranjasTable } from '../FranjasTable'
import styles from './FranjasWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Esta franja ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar la franja horaria.'
}

function coincide(franja: FranjaHorariaResponse, query: string) {
  const texto = query.trim().toLowerCase()

  if (!texto) {
    return true
  }

  const tipo = franja.esDescanso ? 'descanso' : 'académica academica'
  return [String(franja.orden), String(franja.numero), tipo].some((valor) =>
    valor.toLowerCase().includes(texto),
  )
}

export function FranjasWorkspace() {
  const [turnos, setTurnos] = useState<TurnoResponse[]>([])
  const [turnosError, setTurnosError] = useState('')
  const [turnoId, setTurnoId] = useState('')
  const [franjas, setFranjas] = useState<FranjaHorariaResponse[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<
    { name: 'closed' } | { name: 'create' } | { name: 'edit'; id: string }
  >({ name: 'closed' })
  const [editing, setEditing] = useState<FranjaHorariaResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarCatalogoTurnos(controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setTurnos(items)
          setTurnosError('')
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setTurnos([])
        setTurnosError(
          cause instanceof ApiError ? cause.message : 'No se pudo cargar el catálogo de turnos.',
        )
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (turnoId || turnos.length === 0) {
      return
    }

    setTurnoId(turnos[0].id)
  }, [turnos, turnoId])

  useEffect(() => {
    if (!turnoId) {
      setFranjas([])
      setLoading(false)
      return
    }

    const controller = new AbortController()
    setLoading(true)
    setError('')

    listarFranjasPorTurno(turnoId, controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setFranjas(items ?? [])
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setFranjas([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudieron cargar las franjas del turno.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [turnoId])

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

  const loadFranja = (franjaId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: franjaId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarFranjaHoraria(franjaId, controller.signal)
      .then((franja) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(franja)
        setFranjas((current) =>
          franja.turnoId === turnoId
            ? current.map((item) => (item.id === franja.id ? franja : item))
            : current.filter((item) => item.id !== franja.id),
        )
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          setFranjas((current) => current.filter((item) => item.id !== franjaId))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  const rows = [...franjas]
    .filter((franja) => coincide(franja, query))
    .sort((left, right) => left.orden - right.orden)

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Franjas horarias
        </Text>
        <Text variant="body">
          Arma la secuencia de clases y descansos de cada turno. La duración de cada franja debe
          coincidir con la del turno.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Card className={styles.section}>
        <SectionHeader
          title="Franjas del turno"
          description="Elige un turno para ver sus franjas, corregirlas o agregar una nueva."
          action={
            <Button
              variant="primary"
              size="sm"
              disabled={!turnoId}
              onClick={() => setModal({ name: 'create' })}
            >
              + Nueva franja
            </Button>
          }
        />

        <div className={styles.filters}>
          <SelectField
            id="franja-turno"
            label="Turno"
            value={turnos.length ? turnoId : ''}
            options={
              turnos.length
                ? turnos.map((turno) => ({ value: turno.id, label: turno.nombre }))
                : [{ value: '', label: turnosError ? 'No se pudieron cargar los turnos' : 'Cargando turnos...' }]
            }
            disabled={!turnos.length}
            error={turnosError || undefined}
            onChange={(next) => {
              setQuery('')
              setTurnoId(next)
            }}
          />
          <FormField id="franja-search" label="Buscar franja">
            <TextInput
              id="franja-search"
              name="query"
              inputSize="sm"
              placeholder="Buscar por orden, número o tipo..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </FormField>
        </div>

        <div className={styles.tableHost}>
          <FranjasTable
            rows={rows}
            emptyMessage={
              !turnoId
                ? 'Selecciona un turno para ver sus franjas.'
                : loading
                  ? 'Cargando franjas...'
                  : franjas.length === 0
                    ? 'Este turno todavía no tiene franjas.'
                    : 'No se encontraron franjas con esa búsqueda.'
            }
            onEdit={(franja) => loadFranja(franja.id)}
          />
        </div>
      </Card>

      <FranjaFormModal
        open={modal.name !== 'closed'}
        mode={modal.name === 'edit' ? 'edit' : 'create'}
        turnos={turnos}
        franjas={franjas}
        turnoInicialId={turnoId}
        franja={editing ?? undefined}
        loading={modal.name === 'edit' && editLoading}
        loadError={modal.name === 'edit' ? editError : ''}
        onRetry={modal.name === 'edit' ? () => loadFranja(modal.id) : undefined}
        onClose={closeModal}
        onCreated={(franja) => {
          if (franja.turnoId === turnoId) {
            setFranjas((current) =>
              current.some((item) => item.id === franja.id) ? current : [...current, franja],
            )
          }
          setError('')
        }}
        onUpdated={(franja) => {
          setFranjas((current) =>
            franja.turnoId === turnoId
              ? current.map((item) => (item.id === franja.id ? franja : item))
              : current.filter((item) => item.id !== franja.id),
          )
          setError('')
        }}
      />
    </section>
  )
}
