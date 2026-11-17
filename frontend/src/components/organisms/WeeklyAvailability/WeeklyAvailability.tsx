import { useEffect, useState } from 'react'
import {
  actualizarRestriccion,
  ApiError,
  consultarRestriccion,
  diaApi,
  aRestriccionVista,
  registrarRestriccion,
  tipoApi,
} from '../../../api'
import type { RestriccionDocenteVista } from '../../../api/restriccionesDocentes'
import {
  availabilityDays,
  labelAvailability,
  type AvailabilityStatus,
  type RestrictionKind,
  type WeekDay,
} from '../../../content/teacherRestrictions'
import { Select } from '../../atoms/Select'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { RestrictionDetailModal } from '../RestrictionDetailModal'
import { RestrictionFormModal, type RestrictionFormValues } from '../RestrictionFormModal'
import { RestrictionTypeModal } from '../RestrictionTypeModal'
import styles from './WeeklyAvailability.module.css'

type AvailabilityDialog =
  | { name: 'type'; day: WeekDay | ''; bandId: string }
  | { name: 'create'; day: WeekDay | ''; bandId: string; tipo: RestrictionKind }
  | { name: 'edit'; day: WeekDay; bandId: string }
  | { name: 'detail'; day: WeekDay; bandId: string }

export type AvailabilityBand = {
  id: string
  label: string
  kind: 'class' | 'break'
}

export type WeeklyAvailabilityProps = {
  yearId: string
  year: number
  teacherId: string
  teacherName: string
  readOnly: boolean
  yearClosed?: boolean
  shiftLabel: string
  turnos: { id: string; nombre: string }[]
  turnoId: string
  onTurnoChange: (turnoId: string) => void
  bands: AvailabilityBand[]
  bandsLoading: boolean
  bandsError: string
  restrictions: RestriccionDocenteVista[]
  loading: boolean
  error: string
  onSaved: () => void
}

const legend = [
  { status: 'DISPONIBLE', detail: 'Sin restricción' },
  { status: 'NO_DISPONIBLE', detail: 'Bloqueante' },
  { status: 'PREFERENCIA', detail: 'Advertencia' },
] as const

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback
}

function findRestriction(
  restrictions: RestriccionDocenteVista[],
  bandId: string,
  day: WeekDay,
) {
  return restrictions.find((item) => item.bandId === bandId && item.day === day)
}

export function WeeklyAvailability({
  yearId,
  year,
  teacherId,
  teacherName,
  readOnly,
  yearClosed = false,
  shiftLabel,
  turnos,
  turnoId,
  onTurnoChange,
  bands,
  bandsLoading,
  bandsError,
  restrictions,
  loading,
  error,
  onSaved,
}: WeeklyAvailabilityProps) {
  const [dialog, setDialog] = useState<AvailabilityDialog | null>(null)
  const [consultaDetalle, setConsultaDetalle] = useState<{
    key: string
    consulted: RestriccionDocenteVista | null
    error: string
  } | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const closeDialog = () => {
    setDialog(null)
    setSaveError('')
  }
  const detailKey =
    dialog && dialog.name !== 'create' && dialog.name !== 'type' && dialog.day
      ? `${dialog.day}-${dialog.bandId}`
      : ''
  const listed =
    dialog && dialog.name !== 'create' && dialog.name !== 'type' && dialog.day
      ? findRestriction(restrictions, dialog.bandId, dialog.day)
      : undefined
  const consulted = consultaDetalle?.key === detailKey ? consultaDetalle.consulted : null
  const consultError = consultaDetalle?.key === detailKey ? consultaDetalle.error : ''
  const restriction = consulted ?? listed
  const puedeConsultar = Boolean(
    dialog?.name === 'detail' && dialog.day && findRestriction(restrictions, dialog.bandId, dialog.day),
  )
  const consulting = puedeConsultar && consultaDetalle?.key !== detailKey
  const classBands = bands.filter((band) => band.kind === 'class')
  const bandLabel = (bandId: string) => bands.find((band) => band.id === bandId)?.label ?? ''

  useEffect(() => {
    if (dialog?.name !== 'detail' || !dialog.day) {
      return
    }

    const current = findRestriction(restrictions, dialog.bandId, dialog.day)
    if (!current) {
      return
    }

    const controller = new AbortController()
    const key = `${dialog.day}-${dialog.bandId}`

    consultarRestriccion(current.id, controller.signal)
      .then((dto) => {
        if (controller.signal.aborted) {
          return
        }

        const vista = aRestriccionVista(dto)
        if (!vista) {
          setConsultaDetalle({ key, consulted: null, error: 'No se pudo leer la restricción.' })
          return
        }

        setConsultaDetalle({ key, consulted: vista, error: '' })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setConsultaDetalle({
          key,
          consulted: null,
          error: mensajeError(cause, 'No se pudo consultar la restricción.'),
        })
      })

    return () => controller.abort()
  }, [dialog, restrictions])

  const statusAt = (bandId: string, day: WeekDay): AvailabilityStatus =>
    findRestriction(restrictions, bandId, day)?.status ?? 'DISPONIBLE'

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

  const saveRestriction = async (values: RestrictionFormValues) => {
    setSaving(true)
    setSaveError('')

    try {
      if (dialog?.name === 'edit') {
        if (!restriction) {
          setSaveError('No se pudo identificar la restricción.')
          return
        }

        await actualizarRestriccion(restriction.id, {
          tipo: tipoApi(values.tipo),
          motivo: values.motivo,
          documentoSoporte: values.documentoSoporte,
        })
      } else {
        await registrarRestriccion({
          docenteId: teacherId,
          anioEscolarId: yearId,
          dia: diaApi(values.day),
          franjaHorariaId: values.bandId,
          tipo: tipoApi(values.tipo),
          motivo: values.motivo,
          documentoSoporte: values.documentoSoporte,
        })
      }

      closeDialog()
      onSaved()
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo guardar la restricción.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card className={styles.card}>
      <div className={styles.intro}>
        <Text as="h3" variant="sectionTitle">
          Disponibilidad semanal
        </Text>
        {turnos.length > 1 ? (
          <div className={styles.shift}>
            <label className={styles.shiftLabel} htmlFor="restriction-shift">
              Turno
            </label>
            <Select
              id="restriction-shift"
              selectSize="sm"
              className={styles.shiftSelect}
              value={turnoId}
              options={turnos.map((turno) => ({ value: turno.id, label: turno.nombre }))}
              onChange={(event) => onTurnoChange(event.target.value)}
            />
          </div>
        ) : (
          <Text variant="body">Turno: {shiftLabel}</Text>
        )}
        <Text variant="body">
          Selecciona una franja para consultar o agregar una restricción.
        </Text>
      </div>

      <Button
        variant="primary"
        size="sm"
        className={styles.action}
        disabled={readOnly || classBands.length === 0}
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
      {bandsLoading ? <p className={styles.note}>Cargando franjas horarias...</p> : null}
      {bandsError ? (
        <p className={styles.error} role="alert">
          {bandsError}
        </p>
      ) : null}
      {loading ? <p className={styles.note}>Cargando restricciones...</p> : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {!bandsLoading && !bandsError && bands.length === 0 ? (
        <p className={styles.note}>Este turno no tiene franjas horarias.</p>
      ) : null}

      {bands.length > 0 ? (
        <div className={styles.grid}>
          <span className={styles.head}>Franja</span>
          {availabilityDays.map((day) => (
            <span key={day} className={styles.head}>
              {day}
            </span>
          ))}

          {bands.map((band) =>
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
      ) : null}

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
          key={`${dialog.name}-${dialog.day}-${dialog.bandId}-${dialog.name === 'create' ? dialog.tipo : restriction?.id ?? 'edit'}`}
          open
          mode={dialog.name}
          year={year}
          teacherName={teacherName}
          day={dialog.day}
          bandId={dialog.bandId}
          bandLabel={bandLabel(dialog.bandId)}
          tipo={dialog.name === 'create' ? dialog.tipo : undefined}
          restriction={dialog.name === 'edit' ? restriction : undefined}
          bandOptions={classBands.map((band) => ({ value: band.id, label: band.label }))}
          isTaken={(day, bandId) =>
            restrictions.some((item) => item.day === day && item.bandId === bandId)
          }
          submitting={saving}
          error={saveError}
          onClose={closeDialog}
          onSubmit={(values) => {
            void saveRestriction(values)
          }}
        />
      ) : null}

      {dialog?.name === 'detail' ? (
        <RestrictionDetailModal
          open
          year={year}
          teacherName={teacherName}
          bandLabel={bandLabel(dialog.bandId)}
          restriction={restriction ?? null}
          loading={consulting && !restriction}
          error={restriction ? '' : consultError}
          readOnly={readOnly}
          yearClosed={yearClosed}
          onEdit={() => {
            setSaveError('')
            setDialog({ name: 'edit', day: dialog.day, bandId: dialog.bandId })
          }}
          onClose={closeDialog}
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
