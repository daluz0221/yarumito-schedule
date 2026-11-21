import {
  availabilityDays,
  type TimeBand,
  type WeekDay,
} from '../../../content/teacherRestrictions'
import { rutaHistorialEntidad } from '../../../content/auditoria'
import { blockKey, type ScheduleBlock } from '../../../content/scheduleConstruction'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import styles from './ScheduleGrid.module.css'

export type ScheduleGridProps = {
  groupCode: string
  groupId: string
  shiftLabel: string
  readOnly: boolean
  bands: TimeBand[]
  blocks: Map<string, ScheduleBlock>
  onProgram: (day: WeekDay, bandId: string) => void
  onEdit: (placementId: string) => void
}

export function ScheduleGrid({
  groupCode,
  groupId,
  shiftLabel,
  readOnly,
  bands,
  blocks,
  onProgram,
  onEdit,
}: ScheduleGridProps) {
  const pause = bands.some((band) => band.kind === 'break')
  const slots = bands.filter((band) => band.kind === 'class')

  return (
    <Card className={styles.card}>
      <div className={styles.heading}>
        <Text as="h3" variant="sectionTitle">
          Horario por grupo · {groupCode}
        </Text>
        <Text variant="caption">Turno: {shiftLabel}</Text>
      </div>

      <div className={styles.scroll}>
        <div className={styles.grid} role="grid" aria-label={`Horario del grupo ${groupCode}`}>
          <div className={styles.corner} role="columnheader">
            Franja
          </div>
          {availabilityDays.map((day) => (
            <div key={day} className={styles.head} role="columnheader">
              {day}
            </div>
          ))}

          {bands.map((band) => {
            if (band.kind === 'break') {
              return (
                <div key={band.id} className={styles.break} role="row">
                  {band.label}
                </div>
              )
            }

            const index = slots.findIndex((item) => item.id === band.id) + 1

            return (
              <div key={band.id} className={styles.row} role="row">
                <div className={styles.time} role="rowheader">
                  <span>{index}</span>
                  <span>{band.label}</span>
                </div>
                {availabilityDays.map((day) => (
                  <Slot
                    key={`${day}-${band.id}`}
                    day={day}
                    bandId={band.id}
                    block={blocks.get(blockKey(day, band.id))}
                    readOnly={readOnly}
                    groupId={groupId}
                    onProgram={onProgram}
                    onEdit={onEdit}
                  />
                ))}
              </div>
            )
          })}
        </div>
      </div>

      {pause ? (
        <Text variant="caption">
          Verde sin observaciones · Rosa conflicto · Amarillo advertencia. El descanso no se programa.
        </Text>
      ) : null}
    </Card>
  )
}

type SlotProps = {
  day: WeekDay
  bandId: string
  block?: ScheduleBlock
  readOnly: boolean
  groupId: string
  onProgram: (day: WeekDay, bandId: string) => void
  onEdit: (placementId: string) => void
}

function Slot({ day, bandId, block, readOnly, groupId, onProgram, onEdit }: SlotProps) {
  if (!block) {
    return (
      <div className={styles.cell} role="gridcell">
        <button
          type="button"
          className={styles.program}
          disabled={readOnly}
          onClick={() => onProgram(day, bandId)}
        >
          Programar
        </button>
      </div>
    )
  }

  const label = [block.subject, block.teacherName, block.classroom, block.reason]
    .filter(Boolean)
    .join('. ')

  return (
    <div className={styles.cell} role="gridcell">
      <div className={styles.occupied}>
        <button
          type="button"
          className={`${styles.block} ${styles[block.tone]}`}
          title={block.reason || undefined}
          disabled={readOnly || Boolean(block.locked)}
          aria-label={block.locked ? `Clase bloqueada. ${label}` : `Editar clase. ${label}`}
          onClick={() => onEdit(block.id)}
        >
          <span className={styles.subject}>{block.subject}</span>
          <span>{block.teacherName}</span>
          <span>{block.classroom}</span>
        </button>
        <TextLink
          className={styles.history}
          to={rutaHistorialEntidad('ClaseProgramada', block.id, groupId)}
        >
          Ver historial
        </TextLink>
      </div>
    </div>
  )
}
