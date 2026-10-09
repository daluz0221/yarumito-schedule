import { useState } from 'react'
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
}: AcademicAreasSectionProps) {
  const [modal, setModal] = useState<
    | { name: 'closed' }
    | { name: 'create' }
    | { name: 'edit'; area: AreaResponse }
    | { name: 'deactivate'; area: AreaResponse }
  >({ name: 'closed' })
  const closeModal = () => setModal({ name: 'closed' })

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
          setModal({ name: 'edit', area })
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
          key={modal.name === 'edit' ? modal.area.id : 'create'}
          open
          mode={modal.name}
          areas={areas}
          area={modal.name === 'edit' ? modal.area : undefined}
          onClose={closeModal}
        />
      ) : null}

      {modal.name === 'deactivate' ? (
        <DeactivateAreaModal open area={modal.area} onClose={closeModal} />
      ) : null}
    </Card>
  )
}
