import { useEffect, useState } from 'react'
import { ApiError, listarParametrosInstitucionalesPorAnio } from '../../../api'
import type { ParametroInstitucionalResponse } from '../../../api/parametrosInstitucionales'
import { Card } from '../../atoms/Card'
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

const UUID =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

const ORDEN = ['HORAS_DOCENTE_SEMANALES', 'MAX_HORAS_EXTRA']

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

  return 'No se pudo consultar la carga de los docentes para este año.'
}

function horas(parametro: ParametroInstitucionalResponse | undefined) {
  const valor = Number(parametro?.valor)
  return Number.isFinite(valor) ? valor : null
}

function textoHoras(valor: number) {
  return `${valor} ${valor === 1 ? 'hora' : 'horas'}`
}

function explicar(item: ParametroInstitucionalResponse) {
  const valor = horas(item)

  if (item.clave === 'HORAS_DOCENTE_SEMANALES' && valor !== null) {
    return `Cada docente puede dictar ${textoHoras(valor)} de clase a la semana dentro de su jornada habitual.`
  }

  if (item.clave === 'MAX_HORAS_EXTRA' && valor !== null) {
    return `Si el horario lo necesita, puede sumar hasta ${textoHoras(valor)} extra en esa misma semana.`
  }

  if (item.tipoDato === 'BOOL') {
    const activo = item.valor.toLowerCase() === 'true'
    return activo ? 'Está activo para este año.' : 'Está inactivo para este año.'
  }

  return item.descripcion?.trim() || 'Valor definido para este año lectivo.'
}

function titulo(item: ParametroInstitucionalResponse) {
  if (item.clave === 'HORAS_DOCENTE_SEMANALES') {
    return 'Jornada habitual'
  }

  if (item.clave === 'MAX_HORAS_EXTRA') {
    return 'Horas extra'
  }

  return item.descripcion?.trim() || 'Límite del año'
}

function valorVisible(item: ParametroInstitucionalResponse) {
  const cantidad = horas(item)

  if (item.clave.includes('HORAS') && cantidad !== null) {
    return textoHoras(cantidad)
  }

  if (item.tipoDato === 'BOOL') {
    return item.valor.toLowerCase() === 'true' ? 'Sí' : 'No'
  }

  return item.valor
}

function ordenar(items: ParametroInstitucionalResponse[]) {
  return [...items].sort((left, right) => {
    const izquierda = ORDEN.indexOf(left.clave)
    const derecha = ORDEN.indexOf(right.clave)
    const ordenIzquierda = izquierda === -1 ? ORDEN.length : izquierda
    const ordenDerecha = derecha === -1 ? ORDEN.length : derecha

    return ordenIzquierda - ordenDerecha || left.clave.localeCompare(right.clave, 'es')
  })
}

function resumen(items: ParametroInstitucionalResponse[], yearLabel: number) {
  const ordinaria = horas(items.find((item) => item.clave === 'HORAS_DOCENTE_SEMANALES'))
  const extra = horas(items.find((item) => item.clave === 'MAX_HORAS_EXTRA'))

  if (ordinaria === null || extra === null) {
    return `Estos límites se usan al armar el horario de ${yearLabel}.`
  }

  return `En ${yearLabel} un docente puede llegar a ${textoHoras(ordinaria + extra)} de clase por semana: ${textoHoras(ordinaria)} de jornada habitual y ${textoHoras(extra)} extra.`
}

export function ParametrosInstitucionalesSection({
  yearId,
  yearLabel,
}: ParametrosInstitucionalesSectionProps) {
  const [lista, setLista] = useState<ListaEstado>({ estado: 'cargando' })

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

  const filas = lista.estado === 'listo' ? lista.items : []

  return (
    <Card as="section" padding="lg" className={styles.section}>
      <SectionHeader
        title="Carga de los docentes"
        description={
          filas.length > 0
            ? resumen(filas, yearLabel)
            : `Límites de clase de cada docente en ${yearLabel}.`
        }
      />

      {!esUuid(yearId) ? (
        <p className={styles.notice}>
          La carga semanal se consulta en el servidor. Este año de referencia todavía no la tiene.
        </p>
      ) : null}

      {esUuid(yearId) && lista.estado === 'cargando' ? (
        <p className={styles.notice}>Cargando la carga de los docentes…</p>
      ) : null}

      {lista.estado === 'error' ? (
        <p className={styles.alert} role="alert">
          {lista.mensaje}
        </p>
      ) : null}

      {lista.estado === 'listo' && filas.length === 0 ? (
        <p className={styles.notice}>
          Este año todavía no tiene definida la carga semanal de los docentes.
        </p>
      ) : null}

      {filas.length > 0 ? (
        <ul className={styles.facts}>
          {filas.map((item) => (
            <li key={item.id} className={styles.fact}>
              <p className={styles.label}>{titulo(item)}</p>
              <p className={styles.value}>{valorVisible(item)}</p>
              <p className={styles.explain}>{explicar(item)}</p>
            </li>
          ))}
        </ul>
      ) : null}
    </Card>
  )
}
