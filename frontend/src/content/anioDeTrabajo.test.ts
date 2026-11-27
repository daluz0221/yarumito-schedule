import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AcademicYear } from './academicYear'
import { conservarAnio, guardarAnioDeTrabajo, leerAnioDeTrabajo } from './anioDeTrabajo'

const memoria = new Map<string, string>()

const anio = (id: string, year: number, current = false): AcademicYear => ({
  id,
  year,
  startDate: `${year}-01-20`,
  endDate: `${year}-11-30`,
  status: current ? 'ACTIVO' : 'PLANEACION',
  current,
})

describe('conservarAnio', () => {
  it('mantiene el año en el que se estaba trabajando aunque haya uno más reciente', () => {
    const anios = [anio('a2023', 2023), anio('a2027', 2027), anio('a2034', 2034)]

    expect(conservarAnio(anios, 'a2027')).toBe('a2027')
  })

  it('usa el año actual cuando todavía no hay una elección guardada', () => {
    const anios = [anio('a2023', 2023), anio('a2027', 2027, true), anio('a2034', 2034)]

    expect(conservarAnio(anios, '')).toBe('a2027')
  })

  it('usa el último año solo si no hay elección ni año actual', () => {
    const anios = [anio('a2023', 2023), anio('a2027', 2027), anio('a2034', 2034)]

    expect(conservarAnio(anios, '')).toBe('a2034')
    expect(conservarAnio(anios, 'borrado')).toBe('a2034')
  })
})

describe('año de trabajo en la sesión', () => {
  beforeEach(() => {
    memoria.clear()
    vi.stubGlobal('sessionStorage', {
      getItem: (key: string) => memoria.get(key) ?? null,
      setItem: (key: string, value: string) => {
        memoria.set(key, value)
      },
    })
  })

  it('recuerda el año elegido para la siguiente visita', () => {
    guardarAnioDeTrabajo('a2027')

    expect(leerAnioDeTrabajo()).toBe('a2027')
  })
})
