import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarGrupo } from '../../../api'
import type { GrupoResponse } from '../../../api/grupos'
import type { StatItemData } from '../../../content/dashboard'
import type { AcademicYear, SchoolGroup } from '../../../content/academicYear'
import { StatCard } from '../../molecules/StatCard'
import { SectionHeader } from '../../molecules/SectionHeader'
import { Button } from '../../atoms/Button'
import { GroupFormModal } from '../GroupFormModal'
import { SchoolGroupsFilters } from '../SchoolGroupsFilters'
import { SchoolGroupsTable } from '../SchoolGroupsTable'
import styles from './SchoolGroupsSection.module.css'

export type SchoolGroupsSectionProps = {
  year: AcademicYear
  groups: SchoolGroup[]
  stats: StatItemData[]
  grade: string
  status: string
  query: string
  rows: SchoolGroup[]
  emptyMessage: string
  onGradeChange: (value: string) => void
  onStatusChange: (value: string) => void
  onQueryChange: (value: string) => void
  onGroupCreated?: () => void
}

type GroupModal = { name: 'closed' } | { name: 'create' } | { name: 'edit'; id: string }

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este grupo ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el grupo.'
}

export function SchoolGroupsSection({
  year,
  groups,
  stats,
  grade,
  status,
  query,
  rows,
  emptyMessage,
  onGradeChange,
  onStatusChange,
  onQueryChange,
  onGroupCreated,
}: SchoolGroupsSectionProps) {
  const [modal, setModal] = useState<GroupModal>({ name: 'closed' })
  const [editing, setEditing] = useState<GrupoResponse | null>(null)
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
    setEditing(null)
    setEditLoading(false)
    setEditError('')
  }

  const loadGrupo = (groupId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit', id: groupId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarGrupo(groupId, controller.signal)
      .then((grupo) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(grupo)
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

  return (
    <section className={styles.section}>
      <SectionHeader
        title="Grupos del año lectivo"
        description="Define los grupos existentes para cada grado y selecciona cuáles estarán activos durante el período."
        action={
          <Button variant="primary" size="sm" onClick={() => setModal({ name: 'create' })}>
            + Agregar grupo
          </Button>
        }
      />

      <div className={styles.stats} aria-label="Resumen de grupos">
        {stats.map((stat) => (
          <StatCard key={stat.id} value={stat.value} label={stat.label} />
        ))}
      </div>

      <SchoolGroupsFilters
        grade={grade}
        status={status}
        query={query}
        onGradeChange={onGradeChange}
        onStatusChange={onStatusChange}
        onQueryChange={onQueryChange}
      />

      <SchoolGroupsTable
        rows={rows}
        emptyMessage={emptyMessage}
        onEdit={(group) => loadGrupo(group.id)}
      />

      <GroupFormModal
        open={modal.name === 'create'}
        mode="create"
        year={year}
        groups={groups}
        onClose={closeModal}
        onCreated={() => {
          closeModal()
          onGroupCreated?.()
        }}
      />
      <GroupFormModal
        open={modal.name === 'edit'}
        mode="edit"
        year={year}
        groups={groups}
        grupo={editing ?? undefined}
        loading={editLoading}
        loadError={editError}
        onRetry={modal.name === 'edit' ? () => loadGrupo(modal.id) : undefined}
        onClose={closeModal}
        onUpdated={() => onGroupCreated?.()}
      />
    </section>
  )
}
