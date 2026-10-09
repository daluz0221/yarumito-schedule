import { academicYearOptions, academicYears, type AcademicYear } from '../../../content/academicYear'
import {
  scheduleNameForYear,
  validationCampusLabel,
  validationScheduleStatus,
} from '../../../content/validations'
import { Badge } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import styles from './ValidationScheduleCard.module.css'

export type ValidationScheduleCardProps = {
  year: AcademicYear
  version: number
  onYearChange: (yearId: string) => void
}

export function ValidationScheduleCard({
  year,
  version,
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
            El backend aún no publica versiones de horario. Se valida el
            borrador local de {year.year} con la asignación y las restricciones
            ya registradas.
          </Text>
        </div>
        <div className={styles.yearSelect}>
          <SelectField
            id="validation-year"
            label="Año lectivo"
            name="year"
            value={year.id}
            options={academicYearOptions(academicYears)}
            onChange={onYearChange}
          />
        </div>
      </div>

      <dl className={styles.facts}>
        <Fact label="Año lectivo" value={String(year.year)} />
        <Fact label="Sede" value={validationCampusLabel.replace(/^Sede\s+/i, '')} />
        <Fact label="Horario" value={scheduleNameForYear(year)} />
        <Fact label="Versión" value={String(version)} />
        <div className={styles.fact}>
          <Text as="dt" variant="caption">
            Estado
          </Text>
          <dd className={styles.badge}>
            <Badge tone="warning">{validationScheduleStatus}</Badge>
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
