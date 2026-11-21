// La institución maneja solo dos jornadas; el backend y la base de datos
// rechazan cualquier otro nombre de turno y no permiten repetirlos.
export const NOMBRES_TURNO = ['Mañana', 'Contrajornada'] as const

export type NombreTurno = (typeof NOMBRES_TURNO)[number]

// Valores con los que se crean las dos jornadas cuando aún no existen.
// Se ajustan después desde Turnos > Editar.
export const TURNOS_BASE: Record<
  NombreTurno,
  {
    horaInicio: string
    horaFin: string
    duracionClaseMinutos: number
    clasesPorDia: number
    clasesAntesDeDescanso: number
    duracionDescansoMinutos: number
  }
> = {
  // 6:30 + 3 clases de 55 min = 9:15; descanso de 30 min; 2 clases más = 11:35.
  Mañana: {
    horaInicio: '06:30',
    horaFin: '11:35',
    duracionClaseMinutos: 55,
    clasesPorDia: 5,
    clasesAntesDeDescanso: 3,
    duracionDescansoMinutos: 30,
  },
  // Jornada de la tarde: 3 clases de 55 min seguidas, sin descanso.
  Contrajornada: {
    horaInicio: '13:00',
    horaFin: '15:45',
    duracionClaseMinutos: 55,
    clasesPorDia: 3,
    clasesAntesDeDescanso: 3,
    duracionDescansoMinutos: 0,
  },
}
