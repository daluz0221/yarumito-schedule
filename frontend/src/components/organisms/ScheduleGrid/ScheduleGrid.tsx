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
  lectura?: boolean
  bands: TimeBand[]
  blocks: ReadonlyMap<string, ScheduleBlock | readonly ScheduleBlock[]>
  onProgram?: (day: WeekDay, bandId: string) => void
  onEdit?: (placementId: string) => void
}

export function ScheduleGrid({
  groupCode,
  groupId,
  shiftLabel,
  readOnly,
  lectura = false,
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
                  <span className={styles.slotIndex}>{index}</span>
                  <span className={styles.slotRange}>{band.label}</span>
                </div>
                {availabilityDays.map((day) => (
                  <Slot
                    key={`${day}-${band.id}`}
                    day={day}
                    bandId={band.id}
                    block={blocks.get(blockKey(day, band.id))}
                    readOnly={readOnly}
                    lectura={lectura}
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

      {pause && !lectura ? (
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
  block?: ScheduleBlock | readonly ScheduleBlock[]
  readOnly: boolean
  lectura: boolean
  groupId: string
  onProgram?: (day: WeekDay, bandId: string) => void
  onEdit?: (placementId: string) => void
}

function clasesDelSlot(block: ScheduleBlock | readonly ScheduleBlock[] | undefined) {
  if (!block) {
    return []
  }

  return Array.isArray(block) ? block : [block]
}

function Slot({ day, bandId, block, readOnly, lectura, groupId, onProgram, onEdit }: SlotProps) {
  const clases = clasesDelSlot(block)

  if (clases.length === 0) {
    if (lectura) {
      return (
        <div className={styles.cell} role="gridcell">
          <div className={styles.vacia} />
        </div>
      )
    }

    return (
      <div className={styles.cell} role="gridcell">
        <button
          type="button"
          className={styles.program}
          disabled={readOnly}
          onClick={() => onProgram?.(day, bandId)}
        >
          Programar
        </button>
      </div>
    )
  }

  return (
    <div className={styles.cell} role="gridcell">
      <div className={styles.occupied}>
        {clases.map((clase) => (
          <Clase
            key={clase.id}
            clase={clase}
            lectura={lectura}
            readOnly={readOnly}
            groupId={groupId}
            onEdit={onEdit}
          />
        ))}
      </div>
    </div>
  )
}

function Clase({
  clase,
  lectura,
  readOnly,
  groupId,
  onEdit,
}: {
  clase: ScheduleBlock
  lectura: boolean
  readOnly: boolean
  groupId: string
  onEdit?: (placementId: string) => void
}) {
  const label = [clase.subject, clase.teacherName, clase.classroom, clase.reason]
    .filter(Boolean)
    .join('. ')
  const contenido = (
    <>
      <span className={styles.subject}>{clase.subject}</span>
      <span>{clase.teacherName}</span>
      <span>{clase.classroom}</span>
      {lectura && clase.reason ? <span>{clase.reason}</span> : null}
    </>
  )

  if (lectura) {
    return (
      <div className={`${styles.block} ${styles[clase.tone]} ${styles.lectura}`} aria-label={label}>
        {contenido}
      </div>
    )
  }

  return (
    <>
      <button
        type="button"
        className={`${styles.block} ${styles[clase.tone]}`}
        title={clase.reason || undefined}
        disabled={readOnly || Boolean(clase.locked)}
        aria-label={clase.locked ? `Clase bloqueada. ${label}` : `Editar clase. ${label}`}
        onClick={() => onEdit?.(clase.id)}
      >
        {contenido}
      </button>
      <TextLink
        className={styles.history}
        to={rutaHistorialEntidad('ClaseProgramada', clase.id, groupId)}
      >
        Ver historial
      </TextLink>
    </>
  )
}
