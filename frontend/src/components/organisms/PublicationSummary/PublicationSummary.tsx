import { publicationCopy, type PublicationView } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import styles from './PublicationSummary.module.css'

export type PublicationSummaryProps = {
  yearId: string
  yearOptions: { value: string; label: string }[]
  view: PublicationView
  onYearChange: (yearId: string) => void
}

export function PublicationSummary({ yearId, yearOptions, view, onYearChange }: PublicationSummaryProps) {
  return (
    <Card as="section" padding="lg" className={styles.card}>
      <div className={styles.top}>
        <Text as="h3" variant="sectionTitle">
          {publicationCopy.summaryTitle}
        </Text>
        <div className={styles.yearSelect}>
          <SelectField
            id="publication-year"
            label="Año lectivo"
            name="year"
            value={yearId}
            options={yearOptions}
            onChange={onYearChange}
          />
        </div>
      </div>

      <dl className={styles.facts}>
        <Fact label="Horario" value={view.scheduleName} />
        <Fact label="Año" value={view.yearLabel} />
        <Fact label="Sede" value={view.campus} />
        <Fact label="Versión" value={String(view.version)} />
        <div className={styles.fact}>
          <Text as="dt" variant="caption">
            Estado
          </Text>
          <dd className={styles.badge}>
            <Badge tone={view.status === 'PUBLICADO' ? 'success' : 'warning'}>{view.status}</Badge>
          </dd>
        </div>
        <Fact label="Grupos programados" value={String(view.groups)} />
        <Fact label="Clases programadas" value={String(view.classes)} />
        <Fact
          label="Errores pendientes"
          value={String(view.errors)}
          tone={view.errors > 0 ? 'danger' : 'success'}
        />
        <Fact
          label="Advertencias"
          value={String(view.warnings)}
          tone={view.warnings > 0 ? 'warning' : 'success'}
        />
        <Fact label="Horas sin programar" value={String(view.pendingHours)} tone={view.pendingHours > 0 ? 'warning' : 'success'} />
      </dl>
    </Card>
  )
}

function Fact({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone?: 'success' | 'warning' | 'danger'
}) {
  return (
    <div className={styles.fact}>
      <Text as="dt" variant="caption">
        {label}
      </Text>
      <dd className={[styles.value, tone ? styles[tone] : ''].filter(Boolean).join(' ')}>{value}</dd>
    </div>
  )
}
