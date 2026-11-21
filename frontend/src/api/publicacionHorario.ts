import { aConflictoRevisado, listarConflictosPendientes, type ConflictoVista } from './conflictos'
import { consultarHorarioGrupo, type HorarioGrupoResponse } from './horarios'

export type DatosPublicacion = {
  tableros: HorarioGrupoResponse[]
  conflictos: ConflictoVista[]
  // Grupos cuyo horario o conflictos no pudieron consultarse.
  fallidos: number
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

// Reúne el horario de cada grupo y los conflictos pendientes de cada horario
// existente. El backend maneja un horario por grupo; la publicación los resume.
export async function cargarDatosPublicacion(
  grupoIds: string[],
  yearId: string,
  signal?: AbortSignal,
): Promise<DatosPublicacion> {
  const consultas = await Promise.allSettled(
    grupoIds.map((grupoId) => consultarHorarioGrupo(grupoId, signal)),
  )

  const abortada = consultas.find(
    (item) => item.status === 'rejected' && isAbortError(item.reason),
  )

  if (abortada && abortada.status === 'rejected') {
    throw abortada.reason
  }

  const tableros = consultas.flatMap((item) => (item.status === 'fulfilled' ? [item.value] : []))
  let fallidos = consultas.length - tableros.length

  const conRegistro = tableros.filter((item) => item.horarioId)
  const pendientes = await Promise.allSettled(
    conRegistro.map((item) => listarConflictosPendientes(item.horarioId as string, signal)),
  )

  const abortadaConflictos = pendientes.find(
    (item) => item.status === 'rejected' && isAbortError(item.reason),
  )

  if (abortadaConflictos && abortadaConflictos.status === 'rejected') {
    throw abortadaConflictos.reason
  }

  const conflictos: ConflictoVista[] = []

  pendientes.forEach((item) => {
    if (item.status === 'rejected') {
      fallidos += 1
      return
    }

    item.value.forEach((dto) => {
      const conflicto = aConflictoRevisado(dto, yearId)

      if (conflicto) {
        conflictos.push(conflicto)
      }
    })
  })

  return { tableros, conflictos, fallidos }
}
