import { describe, expect, it } from 'vitest'
import { filterConflicts, type ReviewedConflict } from './validations'

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
