import { describe, expect, it } from 'vitest'
import { conflictDetailFacts, filterConflicts, summarizeConflicts, type ReviewedConflict } from './validations'

function conflicto(partial: Partial<ReviewedConflict> & Pick<ReviewedConflict, 'id'>): ReviewedConflict {
  return {
    yearId: 'anio',
    severity: 'ERROR',
    kind: 'teacher-busy',
    message: 'Conflicto',
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
    ...partial,
  }
}

const vacios = {
  severity: '',
  status: '',
  kind: '',
  groupId: '',
  teacherName: '',
  classroom: '',
}

describe('filterConflicts', () => {
  const conflictos = [
    conflicto({ id: '1', severity: 'ERROR', kind: 'teacher-busy', status: 'PENDIENTE' }),
    conflicto({ id: '2', severity: 'ADVERTENCIA', kind: 'capacity', status: 'PENDIENTE' }),
    conflicto({ id: '3', severity: 'ERROR', kind: 'classroom-busy', status: 'RESUELTO' }),
  ]

  it('filtra por severidad, estado y tipo a la vez', () => {
    expect(
      filterConflicts(conflictos, {
        ...vacios,
        severity: 'ERROR',
        status: 'PENDIENTE',
        kind: 'teacher-busy',
      }).map((item) => item.id),
    ).toEqual(['1'])
  })

  it('deja ver solo los resueltos cuando el estado lo pide', () => {
    expect(
      filterConflicts(conflictos, { ...vacios, status: 'RESUELTO' }).map((item) => item.id),
    ).toEqual(['3'])
  })

  it('no descarta conflictos cuando los filtros están en todos', () => {
    expect(filterConflicts(conflictos, vacios)).toHaveLength(3)
  })
})

describe('conflictDetailFacts', () => {
  it('muestra quién, cuándo y dónde ocurrió el conflicto', () => {
    const facts = conflictDetailFacts(
      conflicto({
        id: '1',
        teacherName: 'Ana Gómez',
        groupCode: '601',
        subject: 'Matemáticas',
        classroom: 'Salón 6',
        slotLabel: 'lunes, 06:30–07:25',
      }),
      'Horario 2028',
      '2026-11-25 02:37',
    )

    expect(facts.map((fact) => `${fact.label}: ${fact.value}`)).toEqual([
      'Horario: Horario 2028',
      'Día / Franja: lunes, 06:30–07:25',
      'Docente: Ana Gómez',
      'Grupo: 601',
      'Asignatura: Matemáticas',
      'Aula: Salón 6',
      'Fecha de detección: 2026-11-25 02:37',
    ])
  })

  it('omite las casillas vacías de un conflicto antiguo', () => {
    const facts = conflictDetailFacts(conflicto({ id: '1' }), 'Horario 2028', '2026-11-25 02:37')

    expect(facts.map((fact) => fact.label)).toEqual(['Horario', 'Fecha de detección'])
  })
})

describe('summarizeConflicts', () => {
  it('no cuenta dos veces el mismo conflicto ni suma los resueltos como pendientes', () => {
    const resumen = summarizeConflicts([
      conflicto({ id: '1', status: 'PENDIENTE' }),
      conflicto({ id: '1', status: 'PENDIENTE' }),
      conflicto({ id: '2', severity: 'ADVERTENCIA', kind: 'capacity', status: 'PENDIENTE' }),
      conflicto({ id: '3', status: 'RESUELTO' }),
    ])

    expect(resumen.total).toBe(3)
    expect(resumen.pending).toBe(2)
    expect(resumen.resolved).toBe(1)
    expect(resumen.readiness).toBe('blocked')
  })
})
