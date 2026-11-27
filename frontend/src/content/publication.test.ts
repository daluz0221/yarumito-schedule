import { describe, expect, it } from 'vitest'
import type { AcademicYear } from './academicYear'
import { buildPublicationView, pendingPublicationWarnings } from './publication'
import type { ConflictoVista } from '../api/conflictos'
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

  it('bloquea la publicación cuando un grupo todavía tiene horas sin programar', () => {
    const view = buildPublicationView({
      year,
      campusName: 'Sede Principal',
      datos: {
        tableros: [
          tablero({
            estadoHorario: 'BORRADOR',
            grupoId: 'g1',
            grupoCodigo: '601',
            resumen: {
              asignacionesProgramadas: 0,
              totalAsignaciones: 2,
              horasProgramadas: 2,
              totalHoras: 6,
              horasPendientes: 4,
              conflictos: 0,
              advertencias: 0,
            },
          }),
          tablero({ estadoHorario: 'BORRADOR', grupoId: 'g2', grupoCodigo: '602' }),
        ],
        conflictos: [],
        fallidos: 0,
      },
    })

    expect(view.errors).toBe(1)
    expect(view.pendingHours).toBe(4)
    expect(view.readiness).toBe('blocked')
    expect(view.checks.find((item) => item.id === 'assignments')?.passed).toBe(false)
    expect(view.checks.find((item) => item.id === 'teacher')?.passed).toBe(true)
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

  it('cuenta una sola vez los pendientes de un horario compartido por varios grupos', () => {
    const pendientes = [
      ...Array.from({ length: 9 }, (_, index) => conflicto({ id: `error-${index}` })),
      conflicto({ id: 'aviso-1', severity: 'ADVERTENCIA', kind: 'capacity' }),
      conflicto({ id: 'aviso-2', severity: 'ADVERTENCIA', kind: 'capacity' }),
      conflicto({ id: 'resuelto-1', status: 'RESUELTO' }),
    ]
    const repetidos = Array.from({ length: 8 }, () => pendientes).flat()
    const tableros = Array.from({ length: 8 }, (_, index) =>
      tablero({
        estadoHorario: 'BORRADOR',
        grupoId: `grupo-${index}`,
        grupoCodigo: `60${index}`,
        horarioId: 'horario-compartido',
      }),
    )

    const view = buildPublicationView({
      year,
      campusName: 'Sede Principal',
      datos: { tableros, conflictos: repetidos, fallidos: 0 },
    })

    expect(view.errors).toBe(9)
    expect(view.warnings).toBe(2)
    expect(view.readiness).toBe('blocked')
    expect(view.checks.find((item) => item.id === 'teacher')?.passed).toBe(false)
    expect(view.checks.find((item) => item.id === 'classroom')?.passed).toBe(true)
    expect(view.classes).toBe(0)
    expect(view.pendingHours).toBe(0)
    expect(pendingPublicationWarnings(repetidos)).toHaveLength(2)
  })

  it('suma horarios distintos y no cuenta un conflicto repetido ni uno resuelto', () => {
    const view = buildPublicationView({
      year,
      campusName: 'Sede Principal',
      datos: {
        tableros: [
          tablero({ estadoHorario: 'BORRADOR', horarioId: 'horario-a', grupoId: 'g1' }),
          tablero({ estadoHorario: 'BORRADOR', horarioId: 'horario-b', grupoId: 'g2' }),
        ],
        conflictos: [
          conflicto({ id: 'a' }),
          conflicto({ id: 'b' }),
          conflicto({ id: 'b' }),
          conflicto({ id: 'c', severity: 'ADVERTENCIA', kind: 'capacity' }),
          conflicto({ id: 'd', status: 'RESUELTO' }),
        ],
        fallidos: 0,
      },
    })

    expect(view.errors).toBe(2)
    expect(view.warnings).toBe(1)
    expect(view.readiness).toBe('blocked')
  })
})

function conflicto(partial: Partial<ConflictoVista> & Pick<ConflictoVista, 'id'>): ConflictoVista {
  return {
    yearId: year.id,
    severity: 'ERROR',
    kind: 'teacher-busy',
    message: 'Docente ocupado',
    day: null,
    bandId: null,
    slotLabel: '—',
    affected: '—',
    subject: '',
    teacherName: '',
    classroom: '',
    groupCode: '',
    groupIds: [],
    teacherNames: [],
    classrooms: [],
    status: 'PENDIENTE',
    detectedAt: '2026-11-26 22:57',
    ...partial,
  }
}
