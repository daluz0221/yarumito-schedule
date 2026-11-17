import { useEffect, useState } from 'react'
import type { AlertItemData, StatItemData } from '../content/dashboard'
import { listarCatalogoAniosEscolares, anioEscolarVisible } from './aniosEscolares'
import { listarCatalogoAreas } from './areas'
import { contarDocentes } from './docentes'
import { listarCatalogoGrados } from './grados'
import { listarCatalogoGrupos } from './grupos'
import { consultarHorarioGrupo, type HorarioGrupoResponse } from './horarios'
import { listarPlanesEstudio } from './planesEstudio'
import { listarRestriccionesPorAnio } from './restriccionesDocentes'

export type ResumenTablero = {
  stats: StatItemData[]
  year: string
  status: string
  alerts: AlertItemData[]
}

const ESTADO_ANIO: Record<string, string> = {
  PLANEACION: 'En planeación',
  ACTIVO: 'Activo',
  CERRADO: 'Cerrado',
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function plural(count: number, singular: string, pluralForm: string) {
  return `${count} ${count === 1 ? singular : pluralForm}`
}

async function horariosDeGrupos(grupoIds: string[], signal?: AbortSignal) {
  const resultados = await Promise.all(
    grupoIds.map((id) =>
      consultarHorarioGrupo(id, signal).catch((cause) => {
        if (isAbortError(cause)) {
          throw cause
        }

        return null
      }),
    ),
  )

  return resultados.filter((item): item is HorarioGrupoResponse => item !== null)
}

export async function consultarResumenTablero(signal?: AbortSignal): Promise<ResumenTablero> {
  const [anios, grupos, areas, grados, planes, docentesActivos] = await Promise.all([
    listarCatalogoAniosEscolares(signal),
    listarCatalogoGrupos(signal),
    listarCatalogoAreas(signal),
    listarCatalogoGrados(signal),
    listarPlanesEstudio(signal),
    contarDocentes('ACTIVO', signal),
  ])

  const anio = anioEscolarVisible(anios)
  const gruposActivos = grupos.filter(
    (grupo) => grupo.activo && (!anio || grupo.anioEscolarId === anio.id),
  )
  const areasActivas = areas.filter((area) => area.activa !== false)

  const stats: StatItemData[] = [
    { id: 'teachers', value: String(docentesActivos), label: 'Docentes activos' },
    { id: 'groups', value: String(gruposActivos.length), label: 'Grupos activos' },
    { id: 'areas', value: String(areasActivas.length), label: 'Áreas académicas' },
    { id: 'year', value: anio ? String(anio.year) : '—', label: 'Año lectivo' },
  ]

  const alerts: AlertItemData[] = []

  if (anio) {
    const [restricciones, horarios] = await Promise.all([
      listarRestriccionesPorAnio(anio.id, signal),
      horariosDeGrupos(
        gruposActivos.map((grupo) => grupo.id),
        signal,
      ),
    ])

    const docentesConRestricciones = new Set(restricciones.map((item) => item.docenteId)).size
    const sinRestricciones = Math.max(docentesActivos - docentesConRestricciones, 0)

    if (sinRestricciones > 0) {
      alerts.push({
        id: 'teachers-open',
        message: `${plural(sinRestricciones, 'docente sin restricciones: disponible', 'docentes sin restricciones: disponibles')}`,
        tone: 'warning',
      })
    }

    const gradosConPlan = new Set(
      planes.filter((plan) => plan.anioEscolarId === anio.id).map((plan) => plan.gradoId),
    )
    const gradosEnUso = new Set(gruposActivos.map((grupo) => grupo.gradoId))
    const gradosSinPlan = grados.filter(
      (grado) => gradosEnUso.has(grado.id) && !gradosConPlan.has(grado.id),
    ).length

    if (gradosSinPlan > 0) {
      alerts.push({
        id: 'plan-pending',
        message: `${plural(gradosSinPlan, 'grado con plan de estudios pendiente', 'grados con plan de estudios pendiente')}`,
        tone: 'alert',
      })
    }

    const gruposPendientes = horarios.filter(
      (horario) => (horario.resumen?.horasPendientes ?? 0) > 0,
    ).length
    const conflictos = horarios.reduce((total, item) => total + (item.resumen?.conflictos ?? 0), 0)
    const publicados = horarios.filter((item) => item.estadoHorario === 'PUBLICADO').length

    if (conflictos > 0) {
      alerts.push({
        id: 'schedule-conflicts',
        message: `${plural(conflictos, 'conflicto de horario por resolver', 'conflictos de horario por resolver')}`,
        tone: 'alert',
      })
    }

    if (gruposPendientes > 0) {
      alerts.push({
        id: 'schedule-pending',
        message: `${plural(gruposPendientes, 'grupo con horas por programar', 'grupos con horas por programar')}`,
        tone: 'warning',
      })
    }

    if (alerts.length === 0 && horarios.length > 0) {
      alerts.push({
        id: 'all-ok',
        message:
          publicados === horarios.length
            ? 'Horarios publicados, sin pendientes'
            : 'Sin pendientes: listo para publicar',
        tone: 'success',
      })
    }
  }

  if (alerts.length === 0) {
    alerts.push({ id: 'none', message: 'Sin alertas por el momento', tone: 'neutral' })
  }

  return {
    stats,
    year: anio ? String(anio.year) : '—',
    status: anio ? (ESTADO_ANIO[anio.status] ?? anio.status) : 'Sin año lectivo',
    alerts,
  }
}

export function useResumenTablero() {
  const [resumen, setResumen] = useState<ResumenTablero | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    consultarResumenTablero(controller.signal)
      .then((data) => {
        setResumen(data)
        setError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setError('No se pudo cargar el resumen del panel.')
      })

    return () => controller.abort()
  }, [])

  return { resumen, error }
}
