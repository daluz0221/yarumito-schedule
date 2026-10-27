import { useState } from 'react'
import {
  academicYearOptions,
  formatYearTimeline,
  labelYearStatus,
  nextPlannedYear,
  type AcademicYear,
  type AcademicYearStatus,
} from '../../../content/academicYear'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import { AcademicYearFormModal } from '../AcademicYearFormModal'
import { ActivateAcademicYearModal } from '../ActivateAcademicYearModal'
import { CloseAcademicYearModal } from '../CloseAcademicYearModal'
import styles from './AcademicYearCard.module.css'

export type AcademicYearCardProps = {
  years: AcademicYear[]
  year: AcademicYear
  onYearChange: (yearId: string) => void
}

type YearModal =
  | { name: 'closed' }
  | { name: 'create' }
  | { name: 'edit' }
  | { name: 'activate'; year: AcademicYear }
  | { name: 'close' }

export function AcademicYearCard({
  years,
  year,
  onYearChange,
}: AcademicYearCardProps) {
  const [modal, setModal] = useState<YearModal>({ name: 'closed' })
  const plannedYear =
    year.status === 'ACTIVO' && year.current
      ? nextPlannedYear(years, year)
      : null
  const closeModal = () => setModal({ name: 'closed' })

  return (
    <>
    <Card as="section" padding="lg" className={styles.card}>
      <div className={styles.top}>
        <div className={styles.copy}>
          <Text as="h3" variant="sectionTitle">
            Año lectivo
          </Text>
          <Text variant="body">
            Consulta el período actual y administra su ciclo de vida.
          </Text>
        </div>

        <div className={styles.yearControls}>
          <div className={styles.yearSelect}>
            <SelectField
              id="academic-year"
              label="Año lectivo"
              name="academicYear"
              value={year.id}
              options={academicYearOptions(years)}
              onChange={onYearChange}
            />
          </div>
          <Button variant="primary" size="sm" onClick={() => setModal({ name: 'create' })}>
            + Nuevo año lectivo
          </Button>
        </div>
      </div>

      <dl className={styles.facts}>
        <Fact label="Año" value={String(year.year)} />
        <Fact label="Fecha de inicio" value={year.startDate} />
        <Fact label="Fecha de finalización" value={year.endDate} />
        <Fact
          label="Estado"
          value={labelYearStatus(year.status)}
          tone={statusTone(year.status)}
        />
        <Fact label="Año actual" value={year.current ? 'Sí' : 'No'} />
      </dl>

      <div className={styles.row}>
        <Text variant="body" className={styles.note}>
          Acciones disponibles para {year.year} — {labelYearStatus(year.status)}
        </Text>
        <div className={styles.actions}>
          <Button variant="outline" size="sm" onClick={() => setModal({ name: 'edit' })}>
            Editar fechas
          </Button>
          {year.status === 'ACTIVO' ? (
            <Button variant="primary" size="sm" onClick={() => setModal({ name: 'close' })}>
              Cerrar año
            </Button>
          ) : null}
          {year.status === 'PLANEACION' ? (
            <Button
              variant="primary"
              size="sm"
              onClick={() => setModal({ name: 'activate', year })}
            >
              Activar {year.year}
            </Button>
          ) : null}
        </div>
      </div>

      <div className={styles.row}>
        <Text variant="caption" className={styles.note}>
          {formatYearTimeline(years)}
        </Text>
        {plannedYear ? (
          <div className={styles.actions}>
            <Button
              variant="primary"
              size="sm"
              onClick={() => setModal({ name: 'activate', year: plannedYear })}
            >
              Activar {plannedYear.year}
            </Button>
          </div>
        ) : null}
      </div>
    </Card>
      <AcademicYearFormModal
        open={modal.name === 'create'}
        mode="create"
        years={years}
        onClose={closeModal}
      />
      <AcademicYearFormModal
        open={modal.name === 'edit'}
        mode="edit"
        years={years}
        year={year}
        onClose={closeModal}
      />
      <ActivateAcademicYearModal
        open={modal.name === 'activate'}
        year={modal.name === 'activate' ? modal.year : undefined}
        onClose={closeModal}
      />
      <CloseAcademicYearModal
        open={modal.name === 'close'}
        year={year}
        onClose={closeModal}
      />
    </>
  )
}

function Fact({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone?: 'success' | 'warning'
}) {
  return (
    <div className={styles.fact}>
      <Text as="dt" variant="caption">
        {label}
      </Text>
      <dd className={[styles.value, tone ? styles[tone] : ''].filter(Boolean).join(' ')}>
        {value}
      </dd>
    </div>
  )
}

function statusTone(status: AcademicYearStatus) {
  if (status === 'ACTIVO') {
    return 'success' as const
  }

  if (status === 'PLANEACION') {
    return 'warning' as const
  }

  return undefined
}
