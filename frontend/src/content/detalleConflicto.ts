export type TipoDetalleConflicto = 'DOCENTE_OCUPADO' | 'AULA_OCUPADA' | 'CAPACIDAD_AULA'

export type DetalleLeido = {
  dia: string
  franja: string
  elementoAfectado: string
  asignatura: string
  docente: string
  aula: string
  grupo: string
}

const DIAS: Record<string, string> = {
  lunes: 'lunes',
  martes: 'martes',
  miercoles: 'miércoles',
  jueves: 'jueves',
  viernes: 'viernes',
}

const VACIO: DetalleLeido = {
  dia: '',
  franja: '',
  elementoAfectado: '',
  asignatura: '',
  docente: '',
  aula: '',
  grupo: '',
}

export function leerDetalleDelMensaje(mensaje: string, tipo: TipoDetalleConflicto): DetalleLeido {
  const texto = mensaje.replace(/^\[PRUEBA\]\s*/i, '').trim()

  if (!texto) {
    return VACIO
  }

  const diaClave = plano(texto).match(/\b(lunes|martes|miercoles|jueves|viernes)\b/)?.[1] ?? ''
  const dia = DIAS[diaClave] ?? ''
  const franjaNumero = texto.match(/franja\s+(\d+)/i)?.[1]
  const rango = texto.match(/(\d{2}:\d{2})\s*[–-]\s*(\d{2}:\d{2})/)
  const franja = rango ? `${rango[1]}–${rango[2]}` : franjaNumero ? `franja ${franjaNumero}` : ''
  const aula = texto.match(/Sal[oó]n\s+\d+[A-Za-z]?/i)?.[0] ?? ''
  const docente =
    texto.match(
      /(?:El|La) docente\s+((?:[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)(?:\s+[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+){1,5})/,
    )?.[1] ?? ''
  const grupo = gruposDelMensaje(texto).join(' y ')
  const asignatura = asignaturaDelMensaje(texto)
  const elementoAfectado =
    tipo === 'DOCENTE_OCUPADO' ? docente : aula || (grupo ? `Grupo ${grupo.split(' y ')[0]}` : '')

  return { dia, franja, elementoAfectado, asignatura, docente, aula, grupo }
}

function gruposDelMensaje(texto: string) {
  const grupos: string[] = []
  const agregar = (codigo: string | undefined) => {
    if (codigo && !grupos.includes(codigo)) {
      grupos.push(codigo)
    }
  }

  for (const match of texto.matchAll(/grupo\s+(\d{3,4})/gi)) {
    agregar(match[1])
  }

  for (const match of texto.matchAll(/\((\d{3,4})\s+y\s+(\d{3,4})\)/g)) {
    agregar(match[1])
    agregar(match[2])
  }

  for (const match of texto.matchAll(/\b(?:por|con|a)\s+(\d{3,4})\b/gi)) {
    agregar(match[1])
  }

  return grupos
}

function asignaturaDelMensaje(texto: string) {
  for (const match of texto.matchAll(/\(([^)]+)\)/g)) {
    const contenido = match[1].trim()

    if (
      /[A-Za-zÁÉÍÓÚáéíóúñ]{3,}/.test(contenido) &&
      !/^\d+\s+de\s+\d+$/.test(contenido) &&
      !/^\d{3,4}\s+y\s+\d{3,4}$/.test(contenido)
    ) {
      return contenido
    }
  }

  return ''
}

function plano(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
}
