import { ApiError, api } from './client'
import { aConflictoRevisado, listarConflictosPendientes, type ConflictoVista } from './conflictos'
import { consultarHorarioGrupo, type EstadoHorario, type HorarioGrupoResponse } from './horarios'

export type DatosPublicacion = {
  tableros: HorarioGrupoResponse[]
  conflictos: ConflictoVista[]
  // Grupos cuyo horario o conflictos no pudieron consultarse.
  fallidos: number
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

// Reúne el horario de cada grupo y los conflictos pendientes. Varios grupos
// comparten un mismo horario, así que cada horarioId se consulta una sola vez.
export async function cargarDatosPublicacion(
  grupoIds: string[],
  yearId: string,
  signal?: AbortSignal,
): Promise<DatosPublicacion> {
  const consultas = await Promise.allSettled(
    grupoIds.map((grupoId) => consultarHorarioGrupo(grupoId, signal, { trabajo: true })),
  )

  const abortada = consultas.find(
    (item) => item.status === 'rejected' && isAbortError(item.reason),
  )

  if (abortada && abortada.status === 'rejected') {
    throw abortada.reason
  }

  const tableros = consultas.flatMap((item) => (item.status === 'fulfilled' ? [item.value] : []))
  let fallidos = consultas.length - tableros.length

  const horarioIds = [
    ...new Set(tableros.flatMap((item) => (item.horarioId ? [item.horarioId] : []))),
  ]
  const pendientes = await Promise.allSettled(
    horarioIds.map((horarioId) => listarConflictosPendientes(horarioId, signal)),
  )

  const abortadaConflictos = pendientes.find(
    (item) => item.status === 'rejected' && isAbortError(item.reason),
  )

  if (abortadaConflictos && abortadaConflictos.status === 'rejected') {
    throw abortadaConflictos.reason
  }

  const conflictos: ConflictoVista[] = []
  const vistos = new Set<string>()

  pendientes.forEach((item) => {
    if (item.status === 'rejected') {
      fallidos += 1
      return
    }

    item.value.forEach((dto) => {
      const conflicto = aConflictoRevisado(dto, yearId)

      if (conflicto && !vistos.has(conflicto.id)) {
        vistos.add(conflicto.id)
        conflictos.push(conflicto)
      }
    })
  })

  return { tableros, conflictos, fallidos }
}

export type PublicarHorarioResponse = {
  horarioId: string
  estado: EstadoHorario
  fechaPublicacion: string
  version: number
}

export type HorarioVersionResponse = {
  id: string
  anioEscolarId: string | null
  sedeId: string | null
  sedeNombre: string | null
  nombre: string
  version: number
  estado: EstadoHorario
  fechaPublicacion: string | null
  notas: string | null
  nuevo: boolean
}

export function listarVersionesHorario(anioEscolarId: string, signal?: AbortSignal) {
  return api.get<HorarioVersionResponse[]>(`/api/horarios/anios/${anioEscolarId}/versiones`, { signal })
}

export function crearBorradorDeMejora(horarioId: string) {
  return api.post<HorarioVersionResponse>(`/api/horarios/${horarioId}/borrador`)
}

export function descartarBorradorHorario(horarioId: string) {
  return api.post<null>(`/api/horarios/${horarioId}/descartar`)
}

export async function crearBorradoresDeMejora(horarioIds: string[]) {
  const unicos = [...new Set(horarioIds)]

  if (unicos.length === 0) {
    throw new ApiError('No hay un horario publicado del cual crear un borrador.', 0)
  }

  const resultados: HorarioVersionResponse[] = []

  for (const horarioId of unicos) {
    resultados.push(await crearBorradorDeMejora(horarioId))
  }

  return resultados
}

export function horariosPublicables(tableros: HorarioGrupoResponse[]) {
  return tableros.filter((item) => Boolean(item.horarioId) && item.estadoHorario === 'BORRADOR')
}

export function publicarHorario(horarioId: string) {
  return api.post<PublicarHorarioResponse>(`/api/horarios/${horarioId}/publicar`)
}

// Publica cada horario en BORRADOR. El backend rechaza la petición si no hay
// clases programadas o si quedan conflictos con severidad ERROR.
export async function publicarHorariosEnBorrador(tableros: HorarioGrupoResponse[]) {
  const pendientes = horariosPublicables(tableros)
  const ids = [...new Set(pendientes.map((item) => item.horarioId as string))]

  if (ids.length === 0) {
    throw new ApiError('No hay horarios en borrador para publicar.', 0)
  }

  const errores: string[] = []
  let publicados = 0

  for (const horarioId of ids) {
    const item = pendientes.find((candidato) => candidato.horarioId === horarioId)

    try {
      await publicarHorario(horarioId)
      publicados += 1
    } catch (error) {
      const detalle =
        error instanceof ApiError && error.message
          ? error.message
          : 'No se pudo publicar el horario'
      const grupo = item?.grupoCodigo || 'un grupo'
      errores.push(`${grupo}: ${detalle}`)
    }
  }

  if (errores.length > 0) {
    throw new ApiError(
      publicados > 0
        ? `Se publicaron ${publicados} de ${ids.length} horarios. ${errores.join(' ')}`
        : errores.join(' '),
      0,
    )
  }

  return publicados
}
