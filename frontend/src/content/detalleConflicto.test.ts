import { describe, expect, it } from 'vitest'
import { aConflictoRevisado, type ConflictoResponse } from '../api/conflictos'
import { leerDetalleDelMensaje } from './detalleConflicto'

function conflicto(mensaje: string, tipo: ConflictoResponse['tipo']): ConflictoResponse {
  return {
    id: 'c1',
    horarioId: 'h1',
    tipo,
    severidad: 'ERROR',
    mensaje,
    resuelto: false,
    fechaDeteccion: '2026-11-26T22:57:00',
  }
}

describe('leerDetalleDelMensaje', () => {
  it('lee día, franja, aula y grupos de un aula ocupada', () => {
    expect(
      leerDetalleDelMensaje(
        '[PRUEBA] El Salón 8 tiene dos clases programadas el JUEVES en la franja 4 (801 y 901).',
        'AULA_OCUPADA',
      ),
    ).toMatchObject({
      dia: 'jueves',
      franja: 'franja 4',
      aula: 'Salón 8',
      grupo: '801 y 901',
      elementoAfectado: 'Salón 8',
    })
  })

  it('lee el docente, los dos grupos y la asignatura', () => {
    expect(
      leerDetalleDelMensaje(
        '[PRUEBA] El docente Daniel Arango Toro ya tiene clase con 701 el LUNES en la franja 1 (Matemáticas) y se intentó asignar a 801.',
        'DOCENTE_OCUPADO',
      ),
    ).toMatchObject({
      dia: 'lunes',
      franja: 'franja 1',
      docente: 'Daniel Arango Toro',
      grupo: '701 y 801',
      asignatura: 'Matemáticas',
      elementoAfectado: 'Daniel Arango Toro',
    })
  })

  it('lee el aula y el grupo de un sobreaforo', () => {
    expect(
      leerDetalleDelMensaje(
        '[PRUEBA] El grupo 901 tiene 38 estudiantes y el Salón 6 tiene capacidad para 35.',
        'CAPACIDAD_AULA',
      ),
    ).toMatchObject({
      dia: '',
      aula: 'Salón 6',
      grupo: '901',
      asignatura: '',
      elementoAfectado: 'Salón 6',
    })
  })

  it('no inventa datos en el mensaje genérico', () => {
    expect(
      leerDetalleDelMensaje('El docente ya tiene una clase programada en esta misma franja.', 'DOCENTE_OCUPADO'),
    ).toEqual({
      dia: '',
      franja: '',
      elementoAfectado: '',
      asignatura: '',
      docente: '',
      aula: '',
      grupo: '',
    })
  })
})

describe('aConflictoRevisado con mensajes antiguos', () => {
  it('completa la franja y el elemento afectado desde el mensaje', () => {
    const fila = aConflictoRevisado(
      conflicto(
        '[PRUEBA] La docente Camila Betancur Ruiz tiene cruce de clases el VIERNES en la franja 5 (601 y 1101).',
        'DOCENTE_OCUPADO',
      ),
      'anio',
    )

    expect(fila?.slotLabel).toBe('viernes, franja 5')
    expect(fila?.affected).toBe('Camila Betancur Ruiz')
    expect(fila?.groupCode).toBe('601 y 1101')
  })
})
