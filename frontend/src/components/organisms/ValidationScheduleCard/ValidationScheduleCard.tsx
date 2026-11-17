import type { AcademicYear } from '../../../content/academicYear'
import { scheduleNameForYear } from '../../../content/validations'
import { Badge, type BadgeTone } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import styles from './ValidationScheduleCard.module.css'

export type ValidationScheduleCardProps = {
  year: AcademicYear | null
  yearOptions: { value: string; label: string }[]
  yearDisabled?: boolean
  versionLabel: string
  scheduleName: string
  campusLabel: string
  statusLabel: string
  statusTone: BadgeTone
  onYearChange: (yearId: string) => void
}

export function ValidationScheduleCard({
  year,
  yearOptions,
  yearDisabled = false,
  versionLabel,
  scheduleName,
  campusLabel,
  statusLabel,
  statusTone,
  onYearChange,
}: ValidationScheduleCardProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <div className={styles.top}>
        <div className={styles.copy}>
          <Text as="h3" variant="sectionTitle">
            Horario en validación
          </Text>
          <Text variant="body">
            Se consultan los conflictos guardados del horario de este año y esta sede.
            Elige el grupo para abrir su horario.
          </Text>
        </div>
        <div className={styles.yearSelect}>
          <SelectField
            id="validation-year"
            label="Año lectivo"
            name="year"
            value={year?.id ?? ''}
            options={yearOptions}
            disabled={yearDisabled}
            onChange={onYearChange}
          />
        </div>
      </div>

      <dl className={styles.facts}>
        <Fact label="Año lectivo" value={year ? String(year.year) : '—'} />
        <Fact label="Sede" value={campusLabel.replace(/^Sede\s+/i, '') || '—'} />
        <Fact label="Horario" value={scheduleName || (year ? scheduleNameForYear(year) : '—')} />
        <Fact label="Versión" value={versionLabel} />
        <div className={styles.fact}>
          <Text as="dt" variant="caption">
            Estado
          </Text>
          <dd className={styles.badge}>
            <Badge tone={statusTone}>{statusLabel}</Badge>
          </dd>
        </div>
      </dl>
    </Card>
  )
}

function Fact({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.fact}>
      <Text as="dt" variant="caption">
        {label}
      </Text>
      <dd className={styles.value}>{value}</dd>
    </div>
  )
}
