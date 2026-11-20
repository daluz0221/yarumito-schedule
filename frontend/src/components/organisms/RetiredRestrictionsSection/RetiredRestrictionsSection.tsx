import { useEffect, useState } from 'react'
import {
  ApiError,
  aRestriccionVista,
  listarHistoricoRestriccionesPorDocenteYAnio,
  listarRestriccionesRetiradasPorDocenteYAnio,
  reactivarRestriccion,
} from '../../../api'
import type { RestriccionDocenteResponse } from '../../../api/restriccionesDocentes'
import { labelAvailability } from '../../../content/teacherRestrictions'
import { Card } from '../../atoms/Card'
import { IconAction, IconActions } from '../../atoms/IconAction'
import { Table } from '../../atoms/Table'
import { SectionHeader } from '../../molecules/SectionHeader'
import styles from './RetiredRestrictionsSection.module.css'

type Mode = 'retiradas' | 'historico'

export type RetiredRestrictionsSectionProps = {
  teacherId: string
  yearId: string
  bands: { id: string; label: string }[]
  readOnly: boolean
  // Cambia cuando el listado activo se modifica, para refrescar la vista abierta.
  revision: number
  onRestored: () => void
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback
}

// H-04: restricciones retiradas e histórico completo de un docente en el año.
export function RetiredRestrictionsSection({
  teacherId,
  yearId,
  bands,
  readOnly,
  revision,
  onRestored,
}: RetiredRestrictionsSectionProps) {
  const [mode, setMode] = useState<Mode | null>(null)
  const [result, setResult] = useState<{
    key: string
    items: RestriccionDocenteResponse[]
    error: string
  } | null>(null)
  const [actionError, setActionError] = useState('')
  const [reload, setReload] = useState(0)
  const [restoringId, setRestoringId] = useState<string | null>(null)
  const requestKey = `${mode}|${teacherId}|${yearId}|${revision}|${reload}`
  const loading = Boolean(mode) && result?.key !== requestKey
  const items = result?.key === requestKey ? result.items : []
  const error = actionError || (result?.key === requestKey ? result.error : '')

  useEffect(() => {
    if (!mode) {
      return
    }

    const controller = new AbortController()
    const load =
      mode === 'retiradas'
        ? listarRestriccionesRetiradasPorDocenteYAnio
        : listarHistoricoRestriccionesPorDocenteYAnio

    load(teacherId, yearId, controller.signal)
      .then((loaded) => {
        if (!controller.signal.aborted) {
          setResult({ key: requestKey, items: loaded, error: '' })
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setResult({
          key: requestKey,
          items: [],
          error: mensajeError(cause, 'No se pudieron cargar las restricciones.'),
        })
      })

    return () => controller.abort()
  }, [mode, teacherId, yearId, requestKey])

  const toggle = (next: Mode) => {
    setActionError('')
    setMode((current) => (current === next ? null : next))
  }

  const restore = async (id: string) => {
    setRestoringId(id)
    setActionError('')

    try {
      await reactivarRestriccion(id)
      setReload((current) => current + 1)
      onRestored()
    } catch (cause) {
      setActionError(mensajeError(cause, 'No se pudo reactivar la restricción.'))
    } finally {
      setRestoringId(null)
    }
  }

  const bandLabel = (bandId: string) => bands.find((band) => band.id === bandId)?.label ?? '—'
  const rows = items.flatMap((dto) => {
    const vista = aRestriccionVista(dto)

    return vista ? [{ dto, vista, activa: dto.activo !== false }] : []
  })

  const restoreAction = (id: string, day: string) => (
    <IconAction
      tone="restore"
      label={`Reactivar restricción de ${day}`}
      disabled={restoringId === id}
      onClick={() => {
        void restore(id)
      }}
    />
  )

  return (
    <Card className={styles.card}>
      <SectionHeader
        title={mode === 'historico' ? 'Histórico de restricciones' : 'Restricciones retiradas'}
        description={
          mode === 'historico'
            ? 'Todas las restricciones del año, activas y retiradas.'
            : mode === 'retiradas'
              ? 'Restricciones dadas de baja. Puedes reactivarlas si la franja sigue libre.'
              : 'Consulta las restricciones retiradas y el histórico del docente en este año.'
        }
        action={
          <IconActions>
            <IconAction
              tone="archive"
              pressed={mode === 'retiradas'}
              label={
                mode === 'retiradas'
                  ? 'Ocultar restricciones retiradas'
                  : 'Ver restricciones retiradas'
              }
              onClick={() => toggle('retiradas')}
            />
            <IconAction
              tone="history"
              pressed={mode === 'historico'}
              label={mode === 'historico' ? 'Ocultar histórico' : 'Ver histórico completo'}
              onClick={() => toggle('historico')}
            />
          </IconActions>
        }
      />

      {mode ? (
        loading ? (
          <p className={styles.status}>Cargando restricciones...</p>
        ) : (
          <>
            {error ? (
              <p className={styles.error} role="alert">
                {error}
              </p>
            ) : null}
            {rows.length === 0 && !error ? (
              <p className={styles.status}>
                {mode === 'retiradas'
                  ? 'No hay restricciones retiradas.'
                  : 'No hay restricciones registradas.'}
              </p>
            ) : (
              <>
                <Table minWidth={680} className={styles.desktopOnly}>
                  <thead>
                    <tr>
                      <th>Día</th>
                      <th>Franja</th>
                      <th>Tipo</th>
                      <th>Motivo</th>
                      {mode === 'historico' ? <th>Estado</th> : null}
                      {!readOnly ? <th>Acciones</th> : null}
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map(({ dto, vista, activa }) => (
                      <tr key={dto.id}>
                        <td>{vista.day}</td>
                        <td>{bandLabel(vista.bandId)}</td>
                        <td>{labelAvailability(vista.status)}</td>
                        <td>{vista.motivo || '—'}</td>
                        {mode === 'historico' ? (
                          <td>
                            <span className={activa ? styles.active : styles.retired}>
                              {activa ? 'Activa' : 'Retirada'}
                            </span>
                          </td>
                        ) : null}
                        {!readOnly ? (
                          <td>{!activa ? restoreAction(dto.id, vista.day) : null}</td>
                        ) : null}
                      </tr>
                    ))}
                  </tbody>
                </Table>
                <ul className={styles.cards}>
                  {rows.map(({ dto, vista, activa }) => (
                    <li key={dto.id} className={styles.mobileCard}>
                      <strong>
                        {vista.day} · {bandLabel(vista.bandId)}
                      </strong>
                      <p>
                        {labelAvailability(vista.status)}
                        {mode === 'historico' ? ` · ${activa ? 'Activa' : 'Retirada'}` : ''}
                      </p>
                      <p>{vista.motivo || 'Sin motivo registrado'}</p>
                      {!readOnly && !activa ? restoreAction(dto.id, vista.day) : null}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </>
        )
      ) : null}
    </Card>
  )
}
