import type { AssignmentRow } from '../../../content/academicAssignment'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { AssignmentTable } from '../AssignmentTable'
import styles from './AssignmentSection.module.css'

export type AssignmentSectionProps = {
  title: string
  description: string
  caption: string
  rows: AssignmentRow[]
  emptyMessage: string
  canCreate: boolean
  createLabel?: string
  readOnly?: boolean
  confirmingId?: string | null
  editableAssigned?: boolean
  groupId?: string
  onCreate: () => void
  onAssign: (row: AssignmentRow) => void
  onEdit: (row: AssignmentRow) => void
  onRemove: (row: AssignmentRow) => void
  onConfirm?: (row: AssignmentRow) => void
}

export function AssignmentSection({
  title,
  description,
  caption,
  rows,
  emptyMessage,
  canCreate,
  createLabel = '+ Asignar docente',
  readOnly = false,
  confirmingId = null,
  editableAssigned = true,
  groupId,
  onCreate,
  onAssign,
  onEdit,
  onRemove,
  onConfirm,
}: AssignmentSectionProps) {
  return (
    <Card className={styles.section}>
      <SectionHeader
        title={title}
        description={description}
        action={
          readOnly ? undefined : (
            <Button variant="primary" size="sm" disabled={!canCreate} onClick={onCreate}>
              {createLabel}
            </Button>
          )
        }
      />

      <div className={styles.tableHost}>
        <AssignmentTable
          rows={rows}
          emptyMessage={emptyMessage}
          readOnly={readOnly}
          confirmingId={confirmingId}
          editableAssigned={editableAssigned}
          groupId={groupId}
          onAssign={onAssign}
          onEdit={onEdit}
          onRemove={onRemove}
          onConfirm={onConfirm}
        />
      </div>

      <Text variant="caption">{caption}</Text>
    </Card>
  )
}
