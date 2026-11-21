import { Badge, type BadgeTone } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import styles from './ScheduleSelector.module.css'

export type ScheduleSelectorProps = {
  yearId: string
  campusId: string
  shiftId: string
  groupId: string
  gradeLabel: string
  year: number
  groupCode: string
  groupShiftLabel: string
  yearOptions: { value: string; label: string }[]
  yearDisabled?: boolean
  groupOptions: { value: string; label: string }[]
  groupsDisabled?: boolean
  campusOptions: { value: string; label: string }[]
  campusDisabled?: boolean
  shiftOptions: { value: string; label: string }[]
  shiftDisabled?: boolean
  statusLabel: string
  statusTone: BadgeTone
  onYearChange: (yearId: string) => void
  onCampusChange: (campusId: string) => void
  onShiftChange: (shiftId: string) => void
  onGroupChange: (groupId: string) => void
}

export function ScheduleSelector({
  yearId,
  campusId,
  shiftId,
  groupId,
  gradeLabel,
  year,
  groupCode,
  groupShiftLabel,
  yearOptions,
  yearDisabled = false,
  groupOptions,
  groupsDisabled = false,
  campusOptions,
  campusDisabled = false,
  shiftOptions,
  shiftDisabled = false,
  statusLabel,
  statusTone,
  onYearChange,
  onCampusChange,
  onShiftChange,
  onGroupChange,
}: ScheduleSelectorProps) {
  const emptyGroups = groupOptions.length === 0

  return (
    <Card className={styles.card}>
      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <SelectField
          id="schedule-year"
          label="Año lectivo"
          name="year"
          value={yearId}
          options={yearOptions}
          disabled={yearDisabled}
          onChange={onYearChange}
        />
        <SelectField
          id="schedule-campus"
          label="Sede"
          name="campus"
          value={campusId}
          options={campusOptions}
          disabled={campusDisabled}
          onChange={onCampusChange}
        />
        <SelectField
          id="schedule-shift"
          label="Turno"
          name="shift"
          value={shiftId}
          options={shiftOptions}
          disabled={shiftDisabled}
          onChange={onShiftChange}
        />
        <SelectField
          id="schedule-group"
          label="Grupo"
          name="group"
          value={emptyGroups ? '' : groupId}
          options={
            emptyGroups ? [{ value: '', label: 'Sin grupos activos' }] : groupOptions
          }
          disabled={groupsDisabled || emptyGroups}
          onChange={onGroupChange}
        />
      </form>

      <div className={styles.meta}>
        <div className={styles.status}>
          <Text as="span" variant="caption">
            Estado del horario
          </Text>
          <Badge tone={statusTone}>{statusLabel}</Badge>
        </div>
        {groupCode ? (
          <Text as="p" variant="nav" className={styles.context}>
            Grupo {groupCode} · Grado {gradeLabel} · Año {year}
            {groupShiftLabel ? ` · Turno ${groupShiftLabel}` : ''}
          </Text>
        ) : null}
      </div>
    </Card>
  )
}
