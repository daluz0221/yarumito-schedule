import { describe, expect, it } from 'vitest'
import type { AcademicYear } from './academicYear'
import { buildPublicationView } from './publication'
import type { DatosPublicacion } from '../api/publicacionHorario'
import type { HorarioGrupoResponse } from '../api/horarios'

const year: AcademicYear = {
  id: 'anio-1',
  year: 2027,
  startDate: '2027-01-20',
  endDate: '2027-11-30',
  status: 'ACTIVO',
  current: true,
}

function tablero(
  partial: Partial<HorarioGrupoResponse> & Pick<HorarioGrupoResponse, 'estadoHorario'>,
): HorarioGrupoResponse {
  return {
    grupoId: 'grupo-1',
    grupoCodigo: '601',
    gradoId: 'grado-1',
    gradoNombre: 'SEXTO',
    anioEscolarId: year.id,
    anioEscolar: year.year,
    sedeId: 'sede-1',
    sedeNombre: 'SEDE PRINCIPAL',
    horarioId: 'horario-1',
    versionHorario: 1,
    turnoId: null,
    turnoNombre: null,
    resumen: {
      asignacionesProgramadas: 1,
      totalAsignaciones: 1,
      horasProgramadas: 1,
      totalHoras: 1,
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

function datos(tableros: HorarioGrupoResponse[]): DatosPublicacion {
  return { tableros, conflictos: [], fallidos: 0 }
}

describe('buildPublicationView', () => {
  it('trata el horario como pendiente cuando hay un borrador de mejora', () => {
    const view = buildPublicationView({
      year,
      campusName: 'Sede Principal',
      datos: datos([
        tablero({ estadoHorario: 'BORRADOR', versionHorario: 2, horarioId: 'borrador' }),
      ]),
    })

    expect(view.status).toBe('BORRADOR')
    expect(view.version).toBe(2)
    expect(view.publicationLabel).toBe('Pendiente')
    expect(view.hasSchedule).toBe(true)
    expect(view.versionCaption).toBe('Horario General 2027 · pendiente de publicación')
    expect(view.campus).toBe('Principal')
  })

  it('marca publicado solo cuando todos los horarios con registro están publicados', () => {
    const view = buildPublicationView({
      year,
      campusName: 'Sede Principal',
      datos: datos([
        tablero({ estadoHorario: 'PUBLICADO', versionHorario: 2, grupoId: 'g1' }),
        tablero({ estadoHorario: 'PUBLICADO', versionHorario: 2, grupoId: 'g2', horarioId: 'horario-1' }),
      ]),
    })

    expect(view.status).toBe('PUBLICADO')
    expect(view.publicationLabel).toBe('Publicado')
    expect(view.versionCaption).toBe('Horario General 2027 · publicado')
  })

  it('no considera publicado un año sin horarios construidos', () => {
    const view = buildPublicationView({
      year,
      campusName: '',
      datos: datos([tablero({ estadoHorario: 'BORRADOR', horarioId: null, versionHorario: 0 })]),
    })

    expect(view.hasSchedule).toBe(false)
    expect(view.status).toBe('BORRADOR')
    expect(view.campusFull).toBe('Sede no disponible')
  })
})
