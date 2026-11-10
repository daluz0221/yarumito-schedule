import { useState } from 'react'
import {
  availabilityDays,
  findTeacherRestriction,
  labelAvailability,
  morningBands,
  morningShiftLabel,
  type AvailabilityStatus,
  type RestrictionKind,
  type WeekDay,
} from '../../../content/teacherRestrictions'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { RemoveRestrictionModal } from '../RemoveRestrictionModal'
import { RestrictionDetailModal } from '../RestrictionDetailModal'
import { RestrictionFormModal } from '../RestrictionFormModal'
import { RestrictionTypeModal } from '../RestrictionTypeModal'
import styles from './WeeklyAvailability.module.css'

type AvailabilityDialog =
  | { name: 'type'; day: WeekDay | ''; bandId: string }
  | { name: 'create'; day: WeekDay | ''; bandId: string; tipo: RestrictionKind }
  | { name: 'edit'; day: WeekDay; bandId: string }
  | { name: 'detail'; day: WeekDay; bandId: string }
  | { name: 'remove'; day: WeekDay; bandId: string }

export type WeeklyAvailabilityProps = {
  yearId: string
  year: number
  teacherName: string
  readOnly: boolean
  statusAt: (bandId: string, day: WeekDay) => AvailabilityStatus
}

const legend = [
  { status: 'DISPONIBLE', detail: 'Sin restricción' },
  { status: 'NO_DISPONIBLE', detail: 'Bloqueante' },
  { status: 'PREFERENCIA', detail: 'Advertencia' },
] as const

export function WeeklyAvailability({
  yearId,
  year,
  teacherName,
  readOnly,
  statusAt,
}: WeeklyAvailabilityProps) {
  const [dialog, setDialog] = useState<AvailabilityDialog | null>(null)
  const closeDialog = () => setDialog(null)
  const restriction =
    dialog && dialog.name !== 'create' && dialog.name !== 'type' && dialog.day
      ? findTeacherRestriction(yearId, teacherName, dialog.bandId, dialog.day)
      : undefined

  const openSlot = (day: WeekDay, bandId: string) => {
    const status = statusAt(bandId, day)

    if (readOnly) {
      if (status !== 'DISPONIBLE') {
        setDialog({ name: 'detail', day, bandId })
      }
      return
    }

    setDialog(
      status === 'DISPONIBLE'
        ? { name: 'type', day, bandId }
        : { name: 'detail', day, bandId },
    )
  }

  return (
    <Card className={styles.card}>
      <div className={styles.intro}>
        <Text as="h3" variant="sectionTitle">
          Disponibilidad semanal
        </Text>
        <Text variant="body">
          Turno: {morningShiftLabel} · Selecciona una franja para consultar o agregar una
          restricción.
        </Text>
      </div>

      <Button
        variant="primary"
        size="sm"
        className={styles.action}
        disabled={readOnly}
        onClick={() => setDialog({ name: 'type', day: '', bandId: '' })}
      >
        + Agregar restricción
      </Button>

      <ul className={styles.legend}>
        {legend.map((item) => (
          <li key={item.status}>
            <StatusMark status={item.status} />
            <span>
              {labelAvailability(item.status)} · {item.detail}
            </span>
          </li>
        ))}
      </ul>
      <p className={styles.note}>
        Las franjas sin restricciones se consideran disponibles para la planificación.
      </p>

      <div className={styles.grid}>
        <span className={styles.head}>Franja</span>
        {availabilityDays.map((day) => (
          <span key={day} className={styles.head}>
            {day}
          </span>
        ))}

        {morningBands.map((band) =>
          band.kind === 'break' ? (
            <p key={band.id} className={styles.break}>
              {band.label}
            </p>
          ) : (
            <div key={band.id} className={styles.row}>
              <span className={styles.time}>{band.label}</span>
              {availabilityDays.map((day) => {
                const status = statusAt(band.id, day)
                return (
                  <button
                    key={day}
                    type="button"
                    className={[styles.cell, styles[status]].join(' ')}
                    aria-label={`${day} ${band.label}: ${labelAvailability(status)}`}
                    onClick={() => openSlot(day, band.id)}
                  >
                    <span className={styles.day}>{day}</span>
                    <StatusMark status={status} />
                    {labelAvailability(status)}
                  </button>
                )
              })}
            </div>
          ),
        )}
      </div>

      {dialog?.name === 'type' ? (
        <RestrictionTypeModal
          open
          onSelect={(tipo) =>
            setDialog({ name: 'create', day: dialog.day, bandId: dialog.bandId, tipo })
          }
          onClose={closeDialog}
        />
      ) : null}

      {dialog?.name === 'create' || dialog?.name === 'edit' ? (
        <RestrictionFormModal
          key={`${dialog.name}-${dialog.day}-${dialog.bandId}-${dialog.name === 'create' ? dialog.tipo : 'edit'}`}
          open
          mode={dialog.name}
          yearId={yearId}
          year={year}
          teacherName={teacherName}
          day={dialog.day}
          bandId={dialog.bandId}
          tipo={dialog.name === 'create' ? dialog.tipo : undefined}
          restriction={dialog.name === 'edit' ? restriction : undefined}
          onClose={closeDialog}
        />
      ) : null}

      {dialog?.name === 'detail' && restriction ? (
        <RestrictionDetailModal
          open
          year={year}
          teacherName={teacherName}
          restriction={restriction}
          readOnly={readOnly}
          onEdit={() => setDialog({ name: 'edit', day: dialog.day, bandId: dialog.bandId })}
          onRemove={() => setDialog({ name: 'remove', day: dialog.day, bandId: dialog.bandId })}
          onClose={closeDialog}
        />
      ) : null}

      {dialog?.name === 'remove' ? (
        <RemoveRestrictionModal
          open
          teacherName={teacherName}
          year={year}
          day={dialog.day}
          bandId={dialog.bandId}
          onConfirm={closeDialog}
          onClose={() => setDialog({ name: 'detail', day: dialog.day, bandId: dialog.bandId })}
        />
      ) : null}
    </Card>
  )
}

function StatusMark({ status }: { status: AvailabilityStatus }) {
  if (status === 'NO_DISPONIBLE') {
    return (
      <svg className={styles.icon} viewBox="0 0 16 16" aria-hidden="true">
        <circle cx="8" cy="8" r="5.25" fill="none" stroke="currentColor" strokeWidth="1.4" />
        <path d="M4.8 11.2 11.2 4.8" fill="none" stroke="currentColor" strokeWidth="1.4" />
      </svg>
    )
  }

  if (status === 'PREFERENCIA') {
    return (
      <svg className={styles.icon} viewBox="0 0 16 16" aria-hidden="true">
        <path
          d="M8 2.2 13.2 8 8 13.8 2.8 8Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.4"
        />
      </svg>
    )
  }

  return (
    <svg className={styles.icon} viewBox="0 0 16 16" aria-hidden="true">
      <path
        d="M3.2 8.3 6.4 11.4 12.8 4.6"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
