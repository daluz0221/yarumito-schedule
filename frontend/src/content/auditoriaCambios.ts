// Traduce el estado guardado en el historial de auditoría (JSON del backend)
// a filas legibles para el rector: nombres de campo en español, valores con
// formato y nombres en lugar de identificadores.

export type NombresPorId = Map<string, string>

export type ResolvedorAuditoria = {
  anios: NombresPorId
  grados: NombresPorId
  sedes: NombresPorId
  grupos: NombresPorId
  docentes: NombresPorId
  areas: NombresPorId
  asignaturas: NombresPorId
  turnos: NombresPorId
  franjas: NombresPorId
  aulas: NombresPorId
  usuarios: NombresPorId
  // Asignaciones académicas conocidas (clases programadas apuntan a una).
  asignaciones: NombresPorId
}

export type FilaCambio = {
  campo: string
  antes: string | null
  despues: string | null
}

export type CambiosLegibles = {
  titulo: string
  filas: FilaCambio[]
  // 'comparar' muestra antes → después; 'listar' solo el valor.
  modo: 'comparar' | 'listar'
}

type Formato =
  | 'texto'
  | 'numero'
  | 'fecha'
  | 'dia'
  | 'si-no'
  | 'estado-activo'
  | 'estado-activa'
  | 'enum'
  | { ref: keyof ResolvedorAuditoria }
  | { etiquetas: Record<string, string> }

type Campo = { etiqueta: string; formato: Formato }

const SI_NO = (valor: boolean) => (valor ? 'Sí' : 'No')

const TIPO_AULA = { AULA: 'Aula', LABORATORIO: 'Laboratorio' }
const TIPO_RESTRICCION = { NO_DISPONIBLE: 'No disponible', PREFERENTE: 'Preferencia' }
const ESTADO_ASIGNACION = { BORRADOR: 'Borrador', CONFIRMADA: 'Confirmada' }
const ESTADO_DOCENTE = { ACTIVO: 'Activo', LICENCIA: 'Licencia', RETIRADO: 'Retirado' }
const VINCULACION = { PLANTA: 'Planta', PROVISIONAL: 'Provisional', CONTRATO: 'Contrato' }
const TIPO_DOCUMENTO = {
  CC: 'Cédula de ciudadanía',
  CE: 'Cédula de extranjería',
  PA: 'Pasaporte',
  PPT: 'Permiso por protección temporal',
}
const NIVEL_TITULO = {
  NORMALISTA: 'Normalista',
  LICENCIATURA: 'Licenciatura',
  PROFESIONAL: 'Profesional',
  ESPECIALIZACION: 'Especialización',
  MAESTRIA: 'Maestría',
  DOCTORADO: 'Doctorado',
}

const CAMPOS: Record<string, Record<string, Campo>> = {
  Aula: {
    sedeId: { etiqueta: 'Sede', formato: { ref: 'sedes' } },
    nombre: { etiqueta: 'Nombre', formato: 'texto' },
    tipo: { etiqueta: 'Tipo', formato: { etiquetas: TIPO_AULA } },
    capacidad: { etiqueta: 'Capacidad', formato: 'numero' },
    activa: { etiqueta: 'Estado', formato: 'estado-activa' },
  },
  Docente: {
    nombres: { etiqueta: 'Nombres', formato: 'texto' },
    apellidos: { etiqueta: 'Apellidos', formato: 'texto' },
    tipoDocumento: { etiqueta: 'Tipo de documento', formato: { etiquetas: TIPO_DOCUMENTO } },
    numeroDocumento: { etiqueta: 'Número de documento', formato: 'texto' },
    telefono: { etiqueta: 'Teléfono', formato: 'texto' },
    correoInstitucional: { etiqueta: 'Correo institucional', formato: 'texto' },
    tipoVinculacion: { etiqueta: 'Tipo de vinculación', formato: { etiquetas: VINCULACION } },
    areaNombramientoId: { etiqueta: 'Área de nombramiento', formato: { ref: 'areas' } },
    numeroDecreto: { etiqueta: 'Número de decreto', formato: 'texto' },
    fechaDecreto: { etiqueta: 'Fecha del decreto', formato: 'fecha' },
    escalafon: { etiqueta: 'Escalafón', formato: 'texto' },
    horasSemanalesContratadas: { etiqueta: 'Horas semanales contratadas', formato: 'numero' },
    maxHorasExtra: { etiqueta: 'Máximo de horas extra', formato: 'numero' },
    esExclusivoMediaTecnica: { etiqueta: 'Exclusivo de media técnica', formato: 'si-no' },
    estado: { etiqueta: 'Estado', formato: { etiquetas: ESTADO_DOCENTE } },
    fechaVinculacion: { etiqueta: 'Fecha de vinculación', formato: 'fecha' },
  },
  PlanEstudios: {
    anioEscolarId: { etiqueta: 'Año lectivo', formato: { ref: 'anios' } },
    gradoId: { etiqueta: 'Grado', formato: { ref: 'grados' } },
    asignaturaId: { etiqueta: 'Asignatura', formato: { ref: 'asignaturas' } },
    horasSemanales: { etiqueta: 'Horas semanales', formato: 'numero' },
    turnoId: { etiqueta: 'Turno', formato: { ref: 'turnos' } },
    observacion: { etiqueta: 'Observación', formato: 'texto' },
    activo: { etiqueta: 'Estado', formato: 'estado-activo' },
  },
  RestriccionDocente: {
    docenteId: { etiqueta: 'Docente', formato: { ref: 'docentes' } },
    anioEscolarId: { etiqueta: 'Año lectivo', formato: { ref: 'anios' } },
    dia: { etiqueta: 'Día', formato: 'dia' },
    franjaHorariaId: { etiqueta: 'Franja horaria', formato: { ref: 'franjas' } },
    tipo: { etiqueta: 'Tipo', formato: { etiquetas: TIPO_RESTRICCION } },
    motivo: { etiqueta: 'Motivo', formato: 'texto' },
    documentoSoportePresente: { etiqueta: 'Documento de soporte', formato: 'si-no' },
    aprobadaPorId: { etiqueta: 'Aprobada por', formato: { ref: 'usuarios' } },
    activo: { etiqueta: 'Estado', formato: 'estado-activo' },
  },
  TituloProfesional: {
    docenteId: { etiqueta: 'Docente', formato: { ref: 'docentes' } },
    nivel: { etiqueta: 'Nivel', formato: { etiquetas: NIVEL_TITULO } },
    nombreTitulo: { etiqueta: 'Título', formato: 'texto' },
    institucion: { etiqueta: 'Institución', formato: 'texto' },
    anioGraduacion: { etiqueta: 'Año de graduación', formato: 'numero' },
    archivoSoportePresente: { etiqueta: 'Documento de soporte', formato: 'si-no' },
    activo: { etiqueta: 'Estado', formato: 'estado-activo' },
  },
  AsignacionAcademica: {
    anioEscolarId: { etiqueta: 'Año lectivo', formato: { ref: 'anios' } },
    grupoId: { etiqueta: 'Grupo', formato: { ref: 'grupos' } },
    docenteId: { etiqueta: 'Docente', formato: { ref: 'docentes' } },
    asignaturaId: { etiqueta: 'Asignatura', formato: { ref: 'asignaturas' } },
    estado: { etiqueta: 'Estado', formato: { etiquetas: ESTADO_ASIGNACION } },
    horasSemanales: { etiqueta: 'Horas semanales', formato: 'numero' },
  },
  ClaseProgramada: {
    asignacionAcademicaId: { etiqueta: 'Asignación', formato: { ref: 'asignaciones' } },
    dia: { etiqueta: 'Día', formato: 'dia' },
    franjaHorariaId: { etiqueta: 'Franja horaria', formato: { ref: 'franjas' } },
    aulaId: { etiqueta: 'Aula', formato: { ref: 'aulas' } },
    esHoraExtra: { etiqueta: 'Hora extra', formato: 'si-no' },
  },
}

// Campos internos que no aportan nada a quien lee el historial.
const OCULTOS = new Set(['id', 'horarioId'])

function aObjeto(valor: unknown): Record<string, unknown> | null {
  if (valor == null) {
    return null
  }

  if (typeof valor === 'object' && !Array.isArray(valor)) {
    return valor as Record<string, unknown>
  }

  if (typeof valor === 'string' && valor.trim() && valor.trim() !== 'null') {
    try {
      const analizado: unknown = JSON.parse(valor)

      return analizado && typeof analizado === 'object' && !Array.isArray(analizado)
        ? (analizado as Record<string, unknown>)
        : null
    } catch {
      return null
    }
  }

  return null
}

function vacio(valor: unknown) {
  return valor == null || (typeof valor === 'string' && !valor.trim())
}

function capitalizar(texto: string) {
  const limpio = texto.replace(/_/g, ' ').toLowerCase()
  return limpio.charAt(0).toUpperCase() + limpio.slice(1)
}

function fechaLegible(valor: unknown) {
  if (Array.isArray(valor) && valor.length >= 3) {
    const [year, month, day] = valor.map((parte) => Number(parte))
    const pad = (n: number) => String(n).padStart(2, '0')

    if ([year, month, day].every(Number.isFinite)) {
      return `${pad(day)}/${pad(month)}/${year}`
    }
  }

  const iso = typeof valor === 'string' ? valor.slice(0, 10) : ''
  const partes = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso)

  return partes ? `${partes[3]}/${partes[2]}/${partes[1]}` : String(valor)
}

function etiquetaCampoDesconocido(clave: string) {
  const texto = clave.replace(/([A-Z])/g, ' $1').toLowerCase().trim()
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function formatear(valor: unknown, formato: Formato, resolvedor: ResolvedorAuditoria) {
  if (vacio(valor)) {
    return null
  }

  if (typeof formato === 'object') {
    if ('ref' in formato) {
      return resolvedor[formato.ref].get(String(valor)) ?? 'Registro no disponible'
    }

    return formato.etiquetas[String(valor)] ?? capitalizar(String(valor))
  }

  if (formato === 'si-no') {
    return SI_NO(valor === true || valor === 'true')
  }

  if (formato === 'estado-activo') {
    return valor === true || valor === 'true' ? 'Activo' : 'Retirado'
  }

  if (formato === 'estado-activa') {
    return valor === true || valor === 'true' ? 'Activa' : 'Inactiva'
  }

  if (formato === 'fecha') {
    return fechaLegible(valor)
  }

  if (formato === 'dia') {
    return capitalizar(String(valor)).replace('Miercoles', 'Miércoles')
  }

  return String(valor)
}

function filasDe(entidad: string, datos: Record<string, unknown>) {
  const definidos = CAMPOS[entidad] ?? {}

  return Object.keys(datos)
    .filter((clave) => !OCULTOS.has(clave))
    .flatMap((clave) => {
      const definicion = definidos[clave]

      // Un identificador sin definición no se puede mostrar de forma útil.
      if (!definicion && /Id$/.test(clave)) {
        return []
      }

      const campo: Campo = definicion ?? {
        etiqueta: etiquetaCampoDesconocido(clave),
        formato: typeof datos[clave] === 'boolean' ? 'si-no' : 'texto',
      }

      return [{ clave, campo }]
    })
}

export function describirCambios(
  entidad: string,
  accion: string,
  antes: unknown,
  despues: unknown,
  resolvedor: ResolvedorAuditoria,
): CambiosLegibles {
  const previo = aObjeto(antes)
  const actual = aObjeto(despues)

  // Alta (o publicación sin estado previo): solo los datos registrados.
  if (!previo && actual) {
    const filas = filasDe(entidad, actual).flatMap(({ clave, campo }) => {
      const valor = formatear(actual[clave], campo.formato, resolvedor)
      return valor === null ? [] : [{ campo: campo.etiqueta, antes: null, despues: valor }]
    })

    return { titulo: 'Datos registrados', filas, modo: 'listar' }
  }

  // Baja física: los datos que tenía el registro.
  if (previo && !actual) {
    const filas = filasDe(entidad, previo).flatMap(({ clave, campo }) => {
      const valor = formatear(previo[clave], campo.formato, resolvedor)
      return valor === null ? [] : [{ campo: campo.etiqueta, antes: valor, despues: null }]
    })

    return {
      titulo: accion === 'DELETE' ? 'Datos del registro eliminado' : 'Datos anteriores',
      filas,
      modo: 'listar',
    }
  }

  if (previo && actual) {
    const claves = new Set([...Object.keys(previo), ...Object.keys(actual)])
    const unido: Record<string, unknown> = {}
    claves.forEach((clave) => {
      unido[clave] = actual[clave] ?? previo[clave]
    })

    const filas = filasDe(entidad, unido).flatMap(({ clave, campo }) => {
      const valorAntes = formatear(previo[clave], campo.formato, resolvedor)
      const valorDespues = formatear(actual[clave], campo.formato, resolvedor)

      return valorAntes === valorDespues
        ? []
        : [{ campo: campo.etiqueta, antes: valorAntes, despues: valorDespues }]
    })

    return { titulo: 'Qué cambió', filas, modo: 'comparar' }
  }

  return { titulo: '', filas: [], modo: 'listar' }
}
