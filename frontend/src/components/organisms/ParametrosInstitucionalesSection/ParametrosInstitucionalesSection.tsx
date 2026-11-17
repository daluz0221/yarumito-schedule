import { useEffect, useState } from 'react'
import {
  ApiError,
  consultarParametroInstitucionalPorClave,
  listarParametrosInstitucionalesPorAnio,
} from '../../../api'
import type { ParametroInstitucionalResponse } from '../../../api/parametrosInstitucionales'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import styles from './ParametrosInstitucionalesSection.module.css'

export type ParametrosInstitucionalesSectionProps = {
  yearId: string
  yearLabel: number
}

type ListaEstado =
  | { estado: 'cargando' }
  | { estado: 'error'; mensaje: string }
  | { estado: 'listo'; items: ParametroInstitucionalResponse[] }

type DetalleEstado =
  | { estado: 'idle' }
  | { estado: 'cargando' }
  | { estado: 'error'; mensaje: string }
  | { estado: 'listo'; item: ParametroInstitucionalResponse }

const UUID =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

const TIPOS: Record<string, string> = {
  INT: 'Entero',
  DECIMAL: 'Decimal',
  BOOL: 'Sí o no',
  STRING: 'Texto',
}

function esUuid(value: string) {
  return UUID.test(value)
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeLista(cause: unknown) {
  if (cause instanceof ApiError) {
    return cause.message
  }

  return 'No se pudieron cargar los parámetros institucionales de este año.'
}

function mensajeDetalle(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Ese parámetro ya no está disponible para el año lectivo.'
    }

    return cause.message
  }

  return 'No se pudo consultar el parámetro institucional.'
}

function etiquetaTipo(tipoDato: string) {
  return TIPOS[tipoDato] ?? tipoDato
}

function mostrarValor(parametro: ParametroInstitucionalResponse) {
  if (parametro.tipoDato === 'BOOL') {
    return parametro.valor.toLowerCase() === 'true' ? 'Sí' : 'No'
  }

  if (parametro.clave.includes('HORAS') && parametro.valor.trim()) {
    return `${parametro.valor} h`
  }

  return parametro.valor
}

function ordenar(items: ParametroInstitucionalResponse[]) {
  return [...items].sort((left, right) => left.clave.localeCompare(right.clave, 'es'))
}

export function ParametrosInstitucionalesSection({
  yearId,
  yearLabel,
}: ParametrosInstitucionalesSectionProps) {
  const [lista, setLista] = useState<ListaEstado>({ estado: 'cargando' })
  const [clave, setClave] = useState('')
  const [consulta, setConsulta] = useState(0)
  const [detalle, setDetalle] = useState<DetalleEstado>({ estado: 'idle' })

  useEffect(() => {
    if (!esUuid(yearId)) {
      return
    }

    const controller = new AbortController()

    listarParametrosInstitucionalesPorAnio(yearId, controller.signal)
      .then((items) => {
        if (controller.signal.aborted) {
          return
        }

        setLista({ estado: 'listo', items: ordenar(items ?? []) })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setLista({ estado: 'error', mensaje: mensajeLista(cause) })
      })

    return () => controller.abort()
  }, [yearId])

  useEffect(() => {
    if (!clave || !esUuid(yearId)) {
      return
    }

    const controller = new AbortController()

    consultarParametroInstitucionalPorClave(yearId, clave, controller.signal)
      .then((item) => {
        if (controller.signal.aborted) {
          return
        }

        setDetalle({ estado: 'listo', item })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setDetalle({ estado: 'error', mensaje: mensajeDetalle(cause) })
      })

    return () => controller.abort()
  }, [clave, consulta, yearId])

  const elegir = (siguiente: string) => {
    setDetalle({ estado: 'cargando' })
    setClave(siguiente)
    setConsulta((actual) => actual + 1)
  }

  const filas = lista.estado === 'listo' ? lista.items : []
  const seleccionado = detalle.estado === 'listo' ? detalle.item : null

  return (
    <Card as="section" padding="lg" className={styles.section}>
      <SectionHeader
        title="Parámetros institucionales"
        description={`Carga ordinaria y tope de horas extra del año lectivo ${yearLabel}.`}
      />

      {!esUuid(yearId) ? (
        <p className={styles.notice}>
          Estos parámetros se consultan en el servidor. Los años de referencia locales no
          tienen parámetros institucionales.
        </p>
      ) : null}

      {esUuid(yearId) && lista.estado === 'cargando' ? (
        <p className={styles.notice}>Cargando parámetros institucionales…</p>
      ) : null}

      {lista.estado === 'error' ? (
        <p className={styles.alert} role="alert">
          {lista.mensaje}
        </p>
      ) : null}

      {lista.estado === 'listo' && filas.length === 0 ? (
        <p className={styles.notice}>
          Este año lectivo no tiene parámetros institucionales. Al crear un año, el
          servidor registra la carga ordinaria y el máximo de horas extra.
        </p>
      ) : null}

      {filas.length > 0 ? (
        <div className={styles.tableHost}>
          <div className={styles.scroll}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Parámetro</th>
                  <th>Clave</th>
                  <th>Valor</th>
                  <th>Tipo</th>
                </tr>
              </thead>
              <tbody>
                {filas.map((item) => (
                  <tr
                    key={item.id}
                    className={item.clave === clave ? styles.selected : ''}
                    tabIndex={0}
                    onClick={() => elegir(item.clave)}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault()
                        elegir(item.clave)
                      }
                    }}
                  >
                    <td>{item.descripcion?.trim() || item.clave}</td>
                    <td className={styles.clave}>{item.clave}</td>
                    <td className={styles.valor}>{mostrarValor(item)}</td>
                    <td>{etiquetaTipo(item.tipoDato)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <ul className={styles.cards}>
            {filas.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={[
                    styles.mobileCard,
                    item.clave === clave ? styles.selected : '',
                  ]
                    .filter(Boolean)
                    .join(' ')}
                  onClick={() => elegir(item.clave)}
                >
                  <strong>{item.descripcion?.trim() || item.clave}</strong>
                  <span>{item.clave}</span>
                  <span>
                    {mostrarValor(item)} · {etiquetaTipo(item.tipoDato)}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      ) : null}

      {clave ? (
        <div className={styles.detail} aria-live="polite">
          <h3 className={styles.detailTitle}>Detalle por clave</h3>
          <Text variant="caption">
            Consultado aparte del listado, con la clave {clave}.
          </Text>

          {detalle.estado === 'cargando' ? (
            <p className={styles.notice}>Consultando {clave}…</p>
          ) : null}

          {detalle.estado === 'error' ? (
            <p className={styles.alert} role="alert">
              {detalle.mensaje}
            </p>
          ) : null}

          {seleccionado ? (
            <dl className={styles.facts}>
              <div>
                <dt>Descripción</dt>
                <dd>{seleccionado.descripcion?.trim() || '—'}</dd>
              </div>
              <div>
                <dt>Clave</dt>
                <dd>{seleccionado.clave}</dd>
              </div>
              <div>
                <dt>Valor</dt>
                <dd>{mostrarValor(seleccionado)}</dd>
              </div>
              <div>
                <dt>Tipo de dato</dt>
                <dd>{etiquetaTipo(seleccionado.tipoDato)}</dd>
              </div>
            </dl>
          ) : null}
        </div>
      ) : filas.length > 0 ? (
        <p className={styles.notice}>
          Elige un parámetro para consultar su detalle por clave.
        </p>
      ) : null}
    </Card>
  )
}
