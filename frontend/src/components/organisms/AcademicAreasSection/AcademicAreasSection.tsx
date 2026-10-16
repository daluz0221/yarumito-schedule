import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarArea } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import {
  areaStatusOptions,
  type AreaStatusFilter,
} from '../../../content/areas'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { AcademicAreasTable } from '../AcademicAreasTable'
import { AreaFormModal } from '../AreaFormModal'
import { DeactivateAreaModal } from '../DeactivateAreaModal'
import styles from './AcademicAreasSection.module.css'

function areaDetailErrorMessage(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Esta área ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el área.'
}

export type AcademicAreasSectionProps = {
  areas: AreaResponse[]
  rows: AreaResponse[]
  selectedId: string
  selectedName: string | null
  query: string
  status: AreaStatusFilter
  loading: boolean
  onQueryChange: (value: string) => void
  onStatusChange: (value: AreaStatusFilter) => void
  onSelect: (areaId: string) => void
  onAreaCreated: (area: AreaResponse) => void
  onAreaUpdated: (area: AreaResponse) => void
  onAreaMissing?: (areaId: string) => void
}

export function AcademicAreasSection({
  areas,
  rows,
  selectedId,
  selectedName,
  query,
  status,
  loading,
  onQueryChange,
  onStatusChange,
  onSelect,
  onAreaCreated,
  onAreaUpdated,
  onAreaMissing,
}: AcademicAreasSectionProps) {
  const [modal, setModal] = useState<
    | { name: 'closed' }
    | { name: 'create' }
    | { name: 'edit'; id: string }
    | { name: 'deactivate'; area: AreaResponse }
  >({ name: 'closed' })
  const [editingArea, setEditingArea] = useState<AreaResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    return () => detailRequest.current?.abort()
  }, [])

  const closeModal = () => {
    detailRequest.current?.abort()
    detailRequest.current = null
    setModal({ name: 'closed' })
    setEditingArea(null)
    setEditLoading(false)
    setEditError('')
  }

  const loadAreaDetail = (areaId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: areaId })
    setEditingArea(null)
    setEditError('')
    setEditLoading(true)

    consultarArea(areaId, controller.signal)
      .then((area) => {
        if (controller.signal.aborted) {
          return
        }

        setEditingArea(area)
        onAreaUpdated(area)
      })
      .catch((cause) => {
        if (
          controller.signal.aborted ||
          (cause instanceof DOMException && cause.name === 'AbortError')
        ) {
          return
        }

        setEditingArea(null)
        setEditError(areaDetailErrorMessage(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          onAreaMissing?.(areaId)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  return (
    <Card className={styles.section}>
      <SectionHeader
        title="Áreas académicas"
        description="Selecciona un área para consultar las asignaturas que pertenecen a ella."
        action={
          <Button
            variant="primary"
            size="sm"
            onClick={() => setModal({ name: 'create' })}
          >
            + Nueva área
          </Button>
        }
      />

      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <FormField id="area-search" label="Buscar área">
          <TextInput
            id="area-search"
            name="query"
            inputSize="sm"
            placeholder="Buscar por código o nombre..."
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
          />
        </FormField>
        <SelectField
          id="area-status"
          label="Estado"
          name="status"
          value={status}
          options={areaStatusOptions}
          onChange={(value) => onStatusChange(value as AreaStatusFilter)}
        />
      </form>

      <div className={styles.tableHost}>
      <AcademicAreasTable
        rows={rows}
        selectedId={selectedId}
        emptyMessage={
          loading
            ? 'Cargando áreas...'
            : 'No se encontraron áreas con esos filtros.'
        }
        onSelect={onSelect}
        onEdit={(area) => {
          onSelect(area.id)
          loadAreaDetail(area.id)
        }}
        onDeactivate={(area) => {
          onSelect(area.id)
          setModal({ name: 'deactivate', area })
        }}
      />
      </div>

      {selectedName ? (
        <Text variant="caption">
          Área seleccionada: {selectedName}. Las asignaturas mostradas abajo
          pertenecen únicamente a esta área.
        </Text>
      ) : null}

      {modal.name === 'create' || modal.name === 'edit' ? (
        <AreaFormModal
          key={modal.name === 'edit' ? modal.id : 'create'}
          open
          mode={modal.name}
          areas={areas}
          area={modal.name === 'edit' ? editingArea ?? undefined : undefined}
          loading={modal.name === 'edit' && editLoading}
          loadError={modal.name === 'edit' ? editError : ''}
          onRetry={
            modal.name === 'edit' ? () => loadAreaDetail(modal.id) : undefined
          }
          onClose={closeModal}
          onCreated={onAreaCreated}
          onUpdated={onAreaUpdated}
        />
      ) : null}

      {modal.name === 'deactivate' ? (
        <DeactivateAreaModal
          open
          area={modal.area}
          onClose={closeModal}
          onUpdated={onAreaUpdated}
        />
      ) : null}
    </Card>
  )
}
