import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('./client', () => {
  class ApiError extends Error {
    readonly status: number

    constructor(message: string, status: number) {
      super(message)
      this.name = 'ApiError'
      this.status = status
    }
  }

  return {
    ApiError,
    api: {
      get: vi.fn(),
      post: vi.fn(),
    },
  }
})

import { ApiError, api } from './client'
import type { HorarioGrupoResponse } from './horarios'
import {
  cargarDatosPublicacion,
  crearBorradoresDeMejora,
  horariosPublicables,
  publicarHorariosEnBorrador,
  type HorarioVersionResponse,
} from './publicacionHorario'

const post = vi.mocked(api.post)
const get = vi.mocked(api.get)

function tablero(
  partial: Partial<HorarioGrupoResponse> & Pick<HorarioGrupoResponse, 'horarioId' | 'estadoHorario'>,
): HorarioGrupoResponse {
  return {
    grupoId: partial.grupoId ?? 'grupo-1',
    grupoCodigo: partial.grupoCodigo ?? '601',
    gradoId: 'grado-1',
    gradoNombre: 'SEXTO',
    anioEscolarId: 'anio-1',
    anioEscolar: 2027,
    sedeId: 'sede-1',
    sedeNombre: 'SEDE PRINCIPAL',
    versionHorario: 1,
    turnoId: null,
    turnoNombre: null,
    resumen: {
      asignacionesProgramadas: 0,
      totalAsignaciones: 0,
      horasProgramadas: 0,
      totalHoras: 0,
      horasPendientes: 0,
      conflictos: 0,
      advertencias: 0,
    },
    franjas: [],
    clasesProgramadas: [],
    asignacionesPendientes: [],
    ...partial,
  }
}

function version(id: string, nuevo: boolean): HorarioVersionResponse {
  return {
    id,
    anioEscolarId: 'anio-1',
    sedeId: 'sede-1',
    sedeNombre: 'SEDE PRINCIPAL',
    nombre: 'Horario 2027',
    version: 2,
    estado: 'BORRADOR',
    fechaPublicacion: null,
    notas: 'Borrador de mejora de la versión 1',
    nuevo,
  }
}

describe('horariosPublicables', () => {
  it('deja solo los borradores que ya tienen horario', () => {
    const publicables = horariosPublicables([
      tablero({ horarioId: 'publicado', estadoHorario: 'PUBLICADO' }),
      tablero({ horarioId: null, estadoHorario: 'BORRADOR' }),
      tablero({ horarioId: 'borrador', estadoHorario: 'BORRADOR', grupoCodigo: '602' }),
      tablero({ horarioId: 'archivado', estadoHorario: 'ARCHIVADO' }),
    ])

    expect(publicables.map((item) => item.horarioId)).toEqual(['borrador'])
  })
})

describe('publicarHorariosEnBorrador', () => {
  beforeEach(() => {
    post.mockReset()
  })

  it('publica una sola vez un horario compartido por varios grupos', async () => {
    post.mockResolvedValue({
      horarioId: 'borrador',
      estado: 'PUBLICADO',
      fechaPublicacion: '2026-11-26T18:00:00',
      version: 2,
    })

    const publicados = await publicarHorariosEnBorrador([
      tablero({ horarioId: 'borrador', estadoHorario: 'BORRADOR', grupoCodigo: '601' }),
      tablero({ horarioId: 'borrador', estadoHorario: 'BORRADOR', grupoCodigo: '602' }),
      tablero({ horarioId: 'publicado', estadoHorario: 'PUBLICADO', grupoCodigo: '701' }),
    ])

    expect(publicados).toBe(1)
    expect(post).toHaveBeenCalledTimes(1)
    expect(post).toHaveBeenCalledWith('/api/horarios/borrador/publicar')
  })

  it('rechaza la publicación si no hay borradores', async () => {
    await expect(
      publicarHorariosEnBorrador([
        tablero({ horarioId: 'publicado', estadoHorario: 'PUBLICADO' }),
      ]),
    ).rejects.toThrow('No hay horarios en borrador para publicar.')
    expect(post).not.toHaveBeenCalled()
  })

  it('informa los grupos que no se pudieron publicar', async () => {
    post.mockRejectedValue(new ApiError('Hay un conflicto bloqueante', 409))

    await expect(
      publicarHorariosEnBorrador([
        tablero({ horarioId: 'borrador', estadoHorario: 'BORRADOR', grupoCodigo: '601' }),
      ]),
    ).rejects.toThrow('601: Hay un conflicto bloqueante')
  })
})

describe('crearBorradoresDeMejora', () => {
  beforeEach(() => {
    post.mockReset()
  })

  it('no repite la creación cuando el mismo publicado llega más de una vez', async () => {
    post.mockResolvedValue(version('borrador', true))

    const resultados = await crearBorradoresDeMejora(['publicado', 'publicado'])

    expect(resultados).toHaveLength(1)
    expect(resultados[0]?.nuevo).toBe(true)
    expect(post).toHaveBeenCalledTimes(1)
    expect(post).toHaveBeenCalledWith('/api/horarios/publicado/borrador')
  })

  it('exige al menos un horario publicado', async () => {
    await expect(crearBorradoresDeMejora([])).rejects.toThrow(
      'No hay un horario publicado del cual crear un borrador.',
    )
    expect(post).not.toHaveBeenCalled()
  })
})

describe('cargarDatosPublicacion', () => {
  beforeEach(() => {
    get.mockReset()
  })

  it('consulta una sola vez el horario compartido por ocho grupos', async () => {
    const grupos = Array.from({ length: 8 }, (_, index) => `grupo-${index}`)
    const pendientes = Array.from({ length: 9 }, (_, index) => ({
      id: `error-${index}`,
      horarioId: 'horario-compartido',
      tipo: 'DOCENTE_OCUPADO',
      severidad: 'ERROR',
      mensaje: 'Docente ocupado',
      resuelto: false,
      fechaDeteccion: '2026-11-26T22:57:00',
    }))

    get.mockImplementation(async (path: string) => {
      if (path.includes('/conflictos/')) {
        return pendientes
      }

      const grupoId = path.match(/grupos\/([^?]+)/)?.[1] ?? 'grupo'
      return tablero({
        horarioId: 'horario-compartido',
        estadoHorario: 'BORRADOR',
        grupoId,
        grupoCodigo: grupoId,
      })
    })

    const datos = await cargarDatosPublicacion(grupos, 'anio-1')
    const consultas = get.mock.calls
      .map((call) => String(call[0]))
      .filter((path) => path.includes('/conflictos/horario/horario-compartido/pendientes'))

    expect(consultas).toHaveLength(1)
    expect(datos.conflictos).toHaveLength(9)
  })

  it('suma los pendientes de horarios distintos sin repetir el mismo conflicto', async () => {
    get.mockImplementation(async (path: string) => {
      if (path.includes('/conflictos/horario/horario-a/pendientes')) {
        return [pendiente('compartido', 'horario-a'), pendiente('solo-a', 'horario-a')]
      }

      if (path.includes('/conflictos/horario/horario-b/pendientes')) {
        return [pendiente('compartido', 'horario-b'), pendiente('solo-b', 'horario-b', true)]
      }

      const grupoId = path.match(/grupos\/([^?]+)/)?.[1] ?? 'grupo'
      return tablero({
        horarioId: grupoId === 'grupo-b' ? 'horario-b' : 'horario-a',
        estadoHorario: 'BORRADOR',
        grupoId,
        grupoCodigo: grupoId,
      })
    })

    const datos = await cargarDatosPublicacion(['grupo-a', 'grupo-a', 'grupo-b'], 'anio-1')
    const consultas = get.mock.calls.map((call) => String(call[0])).filter((path) => path.includes('/pendientes'))

    expect(consultas).toEqual([
      '/api/v1/conflictos/horario/horario-a/pendientes',
      '/api/v1/conflictos/horario/horario-b/pendientes',
    ])
    expect(datos.conflictos.map((item) => item.id).sort()).toEqual(['compartido', 'solo-a', 'solo-b'])
  })
})

function pendiente(id: string, horarioId: string, resuelto = false) {
  return {
    id,
    horarioId,
    tipo: 'DOCENTE_OCUPADO' as const,
    severidad: 'ERROR' as const,
    mensaje: 'Docente ocupado',
    resuelto,
    fechaDeteccion: '2026-11-26T22:57:00',
  }
}
