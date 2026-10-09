import { useState } from 'react'
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
}

type GroupModal =
  | { name: 'closed' }
  | { name: 'create' }
  | { name: 'edit'; group: SchoolGroup }

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
}: SchoolGroupsSectionProps) {
  const [modal, setModal] = useState<GroupModal>({ name: 'closed' })
  const closeModal = () => setModal({ name: 'closed' })

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
        onEdit={(group) => setModal({ name: 'edit', group })}
      />

      <GroupFormModal
        open={modal.name === 'create'}
        mode="create"
        year={year}
        groups={groups}
        onClose={closeModal}
      />
      <GroupFormModal
        open={modal.name === 'edit'}
        mode="edit"
        year={year}
        groups={groups}
        group={modal.name === 'edit' ? modal.group : undefined}
        onClose={closeModal}
      />
    </section>
  )
}
