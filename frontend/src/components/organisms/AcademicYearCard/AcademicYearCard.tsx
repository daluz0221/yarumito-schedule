import { useEffect, useRef, useState } from 'react'
import {
  aAcademicYear,
  ApiError,
  consultarAnioEscolar,
} from '../../../api'
import type { AnioEscolarResponse } from '../../../api/aniosEscolares'
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
  onSaved: (year: AcademicYear) => void
  onMissing?: (yearId: string) => void
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este año escolar ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el año escolar.'
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
  onSaved,
  onMissing,
}: AcademicYearCardProps) {
  const [modal, setModal] = useState<YearModal>({ name: 'closed' })
  const [editing, setEditing] = useState<AnioEscolarResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)
  const plannedYear =
    year.status === 'ACTIVO' && year.current
      ? nextPlannedYear(years, year)
      : null

  useEffect(() => {
    return () => detailRequest.current?.abort()
  }, [])

  const closeModal = () => {
    detailRequest.current?.abort()
    detailRequest.current = null
    setModal({ name: 'closed' })
    setEditing(null)
    setEditLoading(false)
    setEditError('')
  }

  const publicar = (anio: AnioEscolarResponse) => {
    const mapped = aAcademicYear(anio)

    if (mapped) {
      onSaved(mapped)
    }
  }

  const loadAnio = (anioId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setModal({ name: 'edit' })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarAnioEscolar(anioId, controller.signal)
      .then((anio) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(anio)
        const mapped = aAcademicYear(anio)

        if (mapped) {
          onSaved(mapped)
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          onMissing?.(anioId)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }

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
          <Button variant="outline" size="sm" onClick={() => loadAnio(year.id)}>
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
        onCreated={publicar}
      />
      <AcademicYearFormModal
        open={modal.name === 'edit'}
        mode="edit"
        years={years}
        anio={editing ?? undefined}
        loading={editLoading}
        loadError={editError}
        onRetry={() => loadAnio(year.id)}
        onClose={closeModal}
        onUpdated={publicar}
      />
      <ActivateAcademicYearModal
        open={modal.name === 'activate'}
        year={modal.name === 'activate' ? modal.year : undefined}
        otroActual={years.some(
          (item) =>
            item.current &&
            item.id !== (modal.name === 'activate' ? modal.year.id : ''),
        )}
        onClose={closeModal}
        onActivated={publicar}
      />
      <CloseAcademicYearModal
        open={modal.name === 'close'}
        year={year}
        onClose={closeModal}
        onClosed={publicar}
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
