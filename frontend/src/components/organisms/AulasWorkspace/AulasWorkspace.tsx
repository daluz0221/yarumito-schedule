import { useEffect, useRef, useState } from 'react'
import {
  ApiError,
  consultarAula,
  eliminarAula,
  listarAulasPorSede,
  opcionesCatalogoSedes,
  useCatalogoSedes,
} from '../../../api'
import type { AulaResponse } from '../../../api/aulas'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Modal } from '../../atoms/Modal'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { ModalActions } from '../../molecules/ModalActions'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { AulaFormModal } from '../AulaFormModal'
import { AulasTable } from '../AulasTable'
import styles from './AulasWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Esta aula ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el aula.'
}

function coincide(aula: AulaResponse, query: string) {
  const texto = query.trim().toLowerCase()

  if (!texto) {
    return true
  }

  return aula.nombre.toLowerCase().includes(texto)
}

type ModalState =
  | { name: 'closed' }
  | { name: 'create' }
  | { name: 'edit' | 'version'; id: string }

export function AulasWorkspace() {
  const { sedes, error: sedesError } = useCatalogoSedes()
  const [chosenSedeId, setSedeId] = useState('')
  const sedeId =
    chosenSedeId ||
    (sedes?.length
      ? (sedes.find((sede) => sede.esPrincipal)?.id ?? sedes[0].id)
      : '')
  const [aulas, setAulas] = useState<AulaResponse[]>([])
  const [loadedSedeId, setLoadedSedeId] = useState('')
  const [query, setQuery] = useState('')
  const [error, setError] = useState('')
  const loading = Boolean(sedeId) && loadedSedeId !== sedeId
  const [modal, setModal] = useState<ModalState>({ name: 'closed' })
  const [editing, setEditing] = useState<AulaResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [removing, setRemoving] = useState<AulaResponse | null>(null)
  const [removeError, setRemoveError] = useState('')
  const [removingBusy, setRemovingBusy] = useState(false)
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    if (!sedeId) {
      return
    }

    const controller = new AbortController()
    const requestedSedeId = sedeId

    listarAulasPorSede(requestedSedeId, controller.signal)
      .then((items) => {
        setAulas(items)
        setError('')
        setLoadedSedeId(requestedSedeId)
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setAulas([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de aulas.',
        )
        setLoadedSedeId(requestedSedeId)
      })

    return () => controller.abort()
  }, [sedeId])

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

  const loadAula = (aulaId: string, mode: 'edit' | 'version') => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: mode, id: aulaId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarAula(aulaId, controller.signal)
      .then((aula) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(aula)
        setAulas((current) => current.map((item) => (item.id === aula.id ? aula : item)))
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          setAulas((current) => current.filter((item) => item.id !== aulaId))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  const confirmDelete = async () => {
    if (!removing || removingBusy) {
      return
    }

    setRemovingBusy(true)
    setRemoveError('')

    try {
      await eliminarAula(removing.id)
      setAulas((current) => current.filter((item) => item.id !== removing.id))
      setRemoving(null)
    } catch (cause) {
      setRemoveError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo eliminar el aula.',
      )
    } finally {
      setRemovingBusy(false)
    }
  }

  const rows = aulas
    .filter((aula) => coincide(aula, query))
    .sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'))
  const formMode = modal.name === 'closed' ? 'create' : modal.name
  const sedeOptions = opcionesCatalogoSedes(sedes, sedesError)

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Aulas
        </Text>
        <Text variant="body">
          Registra los espacios de cada sede. Una nueva versión conserva el aula anterior como
          inactiva cuando cambia su nombre, su tipo o su sede.
        </Text>
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Card className={styles.section}>
        <SectionHeader
          title="Catálogo de aulas"
          description="Elige una sede para ver sus aulas, corregirlas o registrar una nueva versión."
          action={
            <Button
              variant="primary"
              size="sm"
              disabled={!sedeId}
              onClick={() => setModal({ name: 'create' })}
            >
              + Nueva aula
            </Button>
          }
        />

        <div className={styles.filters}>
          <SelectField
            id="aula-sede"
            label="Sede"
            value={sedes?.length ? sedeId : ''}
            options={sedeOptions}
            disabled={!sedes?.length}
            error={sedesError || undefined}
            onChange={setSedeId}
          />
          <FormField id="aula-search" label="Buscar aula">
            <TextInput
              id="aula-search"
              name="query"
              inputSize="sm"
              placeholder="Buscar por nombre..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </FormField>
        </div>

        <div className={styles.tableHost}>
          <AulasTable
            rows={rows}
            emptyMessage={
              !sedeId
                ? 'Selecciona una sede para ver sus aulas.'
                : loading
                  ? 'Cargando aulas...'
                  : aulas.length === 0
                    ? 'Esta sede aún no tiene aulas registradas.'
                    : 'No se encontraron aulas con esa búsqueda.'
            }
            onEdit={(aula) => loadAula(aula.id, 'edit')}
            onVersion={(aula) => loadAula(aula.id, 'version')}
            onDelete={(aula) => {
              setRemoveError('')
              setRemoving(aula)
            }}
          />
        </div>
      </Card>

      <AulaFormModal
        open={modal.name !== 'closed'}
        mode={formMode}
        sedes={sedes}
        sedesError={sedesError}
        aulas={aulas}
        aula={editing ?? undefined}
        sedeInicial={sedeId}
        loading={modal.name !== 'closed' && modal.name !== 'create' && editLoading}
        loadError={modal.name === 'edit' || modal.name === 'version' ? editError : ''}
        onRetry={
          modal.name === 'edit' || modal.name === 'version'
            ? () => loadAula(modal.id, modal.name)
            : undefined
        }
        onClose={closeModal}
        onCreated={(aula) => {
          if (aula.sedeId === sedeId) {
            setAulas((current) =>
              current.some((item) => item.id === aula.id) ? current : [...current, aula],
            )
          }
          setError('')
        }}
        onUpdated={(aula) => {
          setAulas((current) =>
            aula.sedeId === sedeId
              ? current.map((item) => (item.id === aula.id ? aula : item))
              : current.filter((item) => item.id !== aula.id),
          )
          setError('')
        }}
        onVersioned={(anteriorId, aula) => {
          setAulas((current) => {
            const actualizadas = current.map((item) =>
              item.id === anteriorId ? { ...item, activa: false } : item,
            )

            if (aula.sedeId !== sedeId || actualizadas.some((item) => item.id === aula.id)) {
              return actualizadas
            }

            return [...actualizadas, aula]
          })
          setError('')
        }}
      />

      <Modal
        open={Boolean(removing)}
        title="Eliminar aula"
        description={removing ? removing.nombre : undefined}
        onClose={() => {
          if (!removingBusy) {
            setRemoving(null)
          }
        }}
        footer={
          <ModalActions
            confirmLabel={removingBusy ? 'Eliminando...' : 'Eliminar'}
            disabled={removingBusy}
            onCancel={() => setRemoving(null)}
            onConfirm={confirmDelete}
          />
        }
      >
        <p className={styles.deleteCopy}>
          El sistema solo elimina un aula si puede garantizar que nunca se usó. Si no puede
          demostrarlo, el aula se conserva.
        </p>
        {removeError ? (
          <p className={styles.error} role="alert">
            {removeError}
          </p>
        ) : null}
      </Modal>
    </section>
  )
}
