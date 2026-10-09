import { academicYearOptions, academicYears } from '../../../content/academicYear'
import {
  scheduleCampusOptions,
  scheduleShiftOptions,
  scheduleStatusLabel,
  scheduleViewOptions,
} from '../../../content/scheduleConstruction'
import { Badge } from '../../atoms/Badge'
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
  groupOptions: { value: string; label: string }[]
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
  groupOptions,
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
          options={academicYearOptions(academicYears)}
          onChange={onYearChange}
        />
        <SelectField
          id="schedule-campus"
          label="Sede"
          name="campus"
          value={campusId}
          options={scheduleCampusOptions}
          onChange={onCampusChange}
        />
        <SelectField
          id="schedule-shift"
          label="Turno"
          name="shift"
          value={shiftId}
          options={scheduleShiftOptions}
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
          disabled={emptyGroups}
          onChange={onGroupChange}
        />
      </form>

      <div className={styles.meta}>
        <div className={styles.view}>
          <SelectField
            id="schedule-view"
            label="Vista"
            name="view"
            value={scheduleViewOptions[0].value}
            options={scheduleViewOptions}
            disabled
            onChange={() => undefined}
          />
        </div>
        <div className={styles.status}>
          <Text as="span" variant="caption">
            Estado del horario
          </Text>
          <Badge tone="warning">{scheduleStatusLabel}</Badge>
        </div>
        {groupCode ? (
          <Text as="p" variant="nav" className={styles.context}>
            Grupo {groupCode} · Grado {gradeLabel} · Año {year}
          </Text>
        ) : null}
      </div>
    </Card>
  )
}
