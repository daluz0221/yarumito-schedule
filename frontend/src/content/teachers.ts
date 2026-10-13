import type { StatusDotTone } from '../components/atoms/StatusDot'
import type { StatItemData } from './dashboard'

// Alineado con EstadoDocente del backend.
export type TeacherStatus = 'ACTIVO' | 'LICENCIA' | 'RETIRADO'

// Alineado con TipoVinculacion del backend.
export type TeacherContract = 'PLANTA' | 'PROVISIONAL' | 'CONTRATO'

// Modelo alineado con DocenteResponse. Lo que el API no trae se marca como pendiente en la UI.
export type Teacher = {
  id: string
  nombres: string
  apellidos: string
  tipoDocumento: string
  numeroDocumento: string
  telefono: string
  correoInstitucional: string
  tipoVinculacion: TeacherContract
  areaNombramientoId: string
  numeroDecreto: string
  fechaDecreto: string
  escalafon: string
  horasSemanalesContratadas: number
  maxHorasExtra: number
  esExclusivoMediaTecnica: boolean
  estado: TeacherStatus
  fechaVinculacion: string
}

export const PENDING_BACKEND_LABEL = 'Próximamente'

export function formatTeacherName(teacher: Teacher) {
  return `${teacher.nombres} ${teacher.apellidos}`.trim()
}

export function labelEstado(estado: TeacherStatus) {
  const labels: Record<TeacherStatus, string> = {
    ACTIVO: 'Activo',
    LICENCIA: 'Licencia',
    RETIRADO: 'Retirado',
  }

  return labels[estado]
}

export function toneEstado(estado: TeacherStatus): StatusDotTone {
  if (estado === 'ACTIVO') {
    return 'success'
  }

  if (estado === 'LICENCIA') {
    return 'warning'
  }

  return 'neutral'
}

export function labelVinculacion(tipo: TeacherContract) {
  const labels: Record<TeacherContract, string> = {
    PLANTA: 'Planta',
    PROVISIONAL: 'Provisional',
    CONTRATO: 'Contrato',
  }

  return labels[tipo]
}

export function teacherStatsFromCounts(counts: {
  total: number
  active: number
  license: number
  retired: number
}): StatItemData[] {
  return [
    { id: 'total', value: String(counts.total), label: 'Total docentes' },
    { id: 'active', value: String(counts.active), label: 'Docentes activos' },
    { id: 'license', value: String(counts.license), label: 'En licencia' },
    { id: 'retired', value: String(counts.retired), label: 'Retirados' },
  ]
}

export const teacherStatusOptions = [
  { value: '', label: 'Todos' },
  { value: 'ACTIVO', label: 'Activo' },
  { value: 'LICENCIA', label: 'Licencia' },
  { value: 'RETIRADO', label: 'Retirado' },
]

export const teacherContractOptions = [
  { value: '', label: 'Todos' },
  { value: 'PLANTA', label: 'Planta' },
  { value: 'PROVISIONAL', label: 'Provisional' },
  { value: 'CONTRATO', label: 'Contrato' },
]

// El backend filtra por areaId, pero no hay GET /areas ni nombre en DocenteResponse.
export const teacherAreaOptions = [
  { value: '', label: PENDING_BACKEND_LABEL },
]

export const documentTypeOptions = [
  { value: '', label: 'Seleccione un tipo' },
  { value: 'CC', label: 'Cédula de ciudadanía' },
  { value: 'CE', label: 'Cédula de extranjería' },
  { value: 'PA', label: 'Pasaporte' },
  { value: 'PPT', label: 'Permiso por protección temporal' },
]

export const registerAreaOptions = [
  { value: '', label: 'Seleccione un área' },
  { value: 'Matemáticas', label: 'Matemáticas' },
  { value: 'Lengua Castellana', label: 'Lengua Castellana' },
  { value: 'Ciencias Naturales', label: 'Ciencias Naturales' },
  { value: 'Ciencias Sociales', label: 'Ciencias Sociales' },
  { value: 'Inglés', label: 'Inglés' },
  { value: 'Educación Física', label: 'Educación Física' },
]

export const registerContractOptions = [
  { value: '', label: 'Seleccione un tipo' },
  { value: 'PLANTA', label: 'Planta' },
  { value: 'PROVISIONAL', label: 'Provisional' },
  { value: 'CONTRATO', label: 'Contrato' },
]

export const registerStatusOptions = [
  { value: 'ACTIVO', label: 'Activo' },
  { value: 'LICENCIA', label: 'Licencia' },
  { value: 'RETIRADO', label: 'Retirado' },
]

// Forma del formulario de registro, alineada con CrearDocenteRequest.
export type CrearDocenteForm = {
  nombres: string
  apellidos: string
  tipoDocumento: string
  numeroDocumento: string
  telefono: string
  correoInstitucional: string
  tipoVinculacion: TeacherContract | ''
  areaNombramientoId: string
  estado: TeacherStatus
  numeroDecreto: string
  fechaDecreto: string
  escalafon: string
  fechaVinculacion: string
}

export const emptyCrearDocenteForm: CrearDocenteForm = {
  nombres: '',
  apellidos: '',
  tipoDocumento: '',
  numeroDocumento: '',
  telefono: '',
  correoInstitucional: '',
  tipoVinculacion: '',
  areaNombramientoId: '',
  estado: 'ACTIVO',
  numeroDecreto: '',
  fechaDecreto: '',
  escalafon: '',
  fechaVinculacion: '',
}

function optionalText(value: string) {
  const trimmed = value.trim()
  return trimmed === '' ? undefined : trimmed
}

const NAME_PATTERN = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$/
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/
const ESCALAFON_PATTERN = /^[A-Za-z0-9]{1,8}$/

export type CrearDocenteFormErrors = Partial<
  Record<keyof CrearDocenteForm, string>
>

export function sanitizeDigits(value: string) {
  return value.replace(/\D/g, '')
}

export function sanitizeDocumentNumber(tipoDocumento: string, value: string) {
  if (tipoDocumento === 'PA' || tipoDocumento === 'PPT') {
    return value.replace(/[^A-Za-z0-9]/g, '').toUpperCase()
  }

  return sanitizeDigits(value)
}

export function validateCrearDocenteForm(
  form: CrearDocenteForm,
): CrearDocenteFormErrors {
  const errors: CrearDocenteFormErrors = {}
  const nombres = form.nombres.trim()
  const apellidos = form.apellidos.trim()
  const numeroDocumento = form.numeroDocumento.trim()
  const telefono = sanitizeDigits(form.telefono)
  const correo = form.correoInstitucional.trim()
  const numeroDecreto = form.numeroDecreto.trim()
  const escalafon = form.escalafon.trim()

  if (!nombres) {
    errors.nombres = 'Los nombres son obligatorios'
  } else if (!NAME_PATTERN.test(nombres)) {
    errors.nombres = 'Los nombres solo pueden incluir letras y espacios'
  }

  if (!apellidos) {
    errors.apellidos = 'Los apellidos son obligatorios'
  } else if (!NAME_PATTERN.test(apellidos)) {
    errors.apellidos = 'Los apellidos solo pueden incluir letras y espacios'
  }

  if (!form.tipoDocumento) {
    errors.tipoDocumento = 'El tipo de documento es obligatorio'
  }

  if (!numeroDocumento) {
    errors.numeroDocumento = 'El número de documento es obligatorio'
  } else if (form.tipoDocumento === 'PA' || form.tipoDocumento === 'PPT') {
    if (!/^[A-Z0-9]{5,15}$/.test(numeroDocumento)) {
      errors.numeroDocumento =
        'El documento debe tener entre 5 y 15 caracteres alfanuméricos'
    }
  } else if (!/^\d{6,15}$/.test(numeroDocumento)) {
    errors.numeroDocumento = 'El número de documento debe tener entre 6 y 15 dígitos'
  }

  if (telefono && !/^\d{7,15}$/.test(telefono)) {
    errors.telefono = 'El teléfono debe tener entre 7 y 15 dígitos'
  }

  if (correo && !EMAIL_PATTERN.test(correo)) {
    errors.correoInstitucional = 'Ingresa un correo electrónico válido'
  }

  if (!form.tipoVinculacion) {
    errors.tipoVinculacion = 'El tipo de vinculación es obligatorio'
  }

  if (!form.areaNombramientoId) {
    errors.areaNombramientoId = 'El área de nombramiento es obligatoria'
  }

  if (numeroDecreto && !/^\d{1,20}$/.test(numeroDecreto)) {
    errors.numeroDecreto = 'El número de decreto solo puede contener dígitos'
  }

  if (form.fechaDecreto && !DATE_PATTERN.test(form.fechaDecreto)) {
    errors.fechaDecreto = 'La fecha de decreto no es válida'
  }

  if (escalafon && !ESCALAFON_PATTERN.test(escalafon)) {
    errors.escalafon = 'El escalafón solo puede incluir letras y números'
  }

  if (form.fechaVinculacion && !DATE_PATTERN.test(form.fechaVinculacion)) {
    errors.fechaVinculacion = 'La fecha de vinculación no es válida'
  }

  if (!form.estado) {
    errors.estado = 'El estado es obligatorio'
  }

  return errors
}

// Cuerpo de POST /api/v1/docentes.
export type CrearDocenteRequest = {
  nombres: string
  apellidos: string
  tipoDocumento: string
  numeroDocumento: string
  telefono?: string
  correoInstitucional?: string
  tipoVinculacion: TeacherContract
  areaNombramientoId: string
  estado: TeacherStatus
  numeroDecreto?: string
  fechaDecreto?: string
  escalafon?: string
  fechaVinculacion?: string
}

export function toCrearDocenteRequest(form: CrearDocenteForm): CrearDocenteRequest {
  if (!form.tipoVinculacion) {
    throw new Error('El tipo de vinculación es obligatorio')
  }

  if (!form.areaNombramientoId) {
    throw new Error('El área de nombramiento es obligatoria')
  }

  return {
    nombres: form.nombres.trim(),
    apellidos: form.apellidos.trim(),
    tipoDocumento: form.tipoDocumento,
    numeroDocumento: sanitizeDocumentNumber(
      form.tipoDocumento,
      form.numeroDocumento,
    ),
    telefono: optionalText(sanitizeDigits(form.telefono)),
    correoInstitucional: optionalText(form.correoInstitucional.trim()),
    tipoVinculacion: form.tipoVinculacion,
    areaNombramientoId: form.areaNombramientoId,
    estado: form.estado,
    numeroDecreto: optionalText(sanitizeDigits(form.numeroDecreto)),
    fechaDecreto: optionalText(form.fechaDecreto),
    escalafon: optionalText(form.escalafon),
    fechaVinculacion: optionalText(form.fechaVinculacion),
  }
}

export type ActualizarDocenteRequest = Omit<CrearDocenteRequest, 'estado'>

export function validateActualizarDocenteForm(form: CrearDocenteForm) {
  const errors = validateCrearDocenteForm(form)
  delete errors.estado
  return errors
}

export function toActualizarDocenteRequest(
  form: CrearDocenteForm,
): ActualizarDocenteRequest {
  const { estado: _estado, ...payload } = toCrearDocenteRequest(form)
  return payload
}

export function teacherToForm(teacher: Teacher): CrearDocenteForm {
  return {
    nombres: teacher.nombres,
    apellidos: teacher.apellidos,
    tipoDocumento: teacher.tipoDocumento,
    numeroDocumento: teacher.numeroDocumento,
    telefono: teacher.telefono,
    correoInstitucional: teacher.correoInstitucional,
    tipoVinculacion: teacher.tipoVinculacion,
    areaNombramientoId: teacher.areaNombramientoId,
    estado: teacher.estado,
    numeroDecreto: teacher.numeroDecreto,
    fechaDecreto: teacher.fechaDecreto.slice(0, 10),
    escalafon: teacher.escalafon,
    fechaVinculacion: teacher.fechaVinculacion.slice(0, 10),
  }
}

function docente(
  data: Pick<
    Teacher,
    | 'id'
    | 'nombres'
    | 'apellidos'
    | 'numeroDocumento'
    | 'correoInstitucional'
    | 'telefono'
    | 'tipoVinculacion'
    | 'estado'
  > &
    Partial<Teacher>,
): Teacher {
  return {
    tipoDocumento: 'CC',
    areaNombramientoId: '00000000-0000-0000-0000-000000000001',
    numeroDecreto: '',
    fechaDecreto: '',
    escalafon: '',
    horasSemanalesContratadas: 22,
    maxHorasExtra: 4,
    esExclusivoMediaTecnica: false,
    fechaVinculacion: '2020-01-15',
    ...data,
  }
}

export const teachers: Teacher[] = [
  docente({
    id: '1',
    nombres: 'Carlos Andrés',
    apellidos: 'Pérez Gómez',
    numeroDocumento: '1234567890',
    correoInstitucional: 'carlos.perez@yarumito.edu.co',
    telefono: '3001234567',
    tipoVinculacion: 'PLANTA',
    estado: 'ACTIVO',
  }),
  docente({
    id: '2',
    nombres: 'Carlos Andrés',
    apellidos: 'López',
    numeroDocumento: '1032456781',
    correoInstitucional: 'carlos.lopez@yarumito.edu.co',
    telefono: '3014567901',
    tipoVinculacion: 'PROVISIONAL',
    estado: 'ACTIVO',
  }),
  docente({
    id: '3',
    nombres: 'Dianara Marcela',
    apellidos: 'Ruíz',
    numeroDocumento: '1023578944',
    correoInstitucional: 'dianara.ruiz@yarumito.edu.co',
    telefono: '3104569820',
    tipoVinculacion: 'PLANTA',
    estado: 'ACTIVO',
  }),
  docente({
    id: '4',
    nombres: 'Jorge Hernán',
    apellidos: 'Torres',
    numeroDocumento: '1012345678',
    correoInstitucional: 'jorge.torres@yarumito.edu.co',
    telefono: '3157892340',
    tipoVinculacion: 'CONTRATO',
    estado: 'RETIRADO',
  }),
  docente({
    id: '5',
    nombres: 'Laura Sofía',
    apellidos: 'Martínez',
    numeroDocumento: '1009876543',
    correoInstitucional: 'laura.martinez@yarumito.edu.co',
    telefono: '3206541188',
    tipoVinculacion: 'PROVISIONAL',
    estado: 'ACTIVO',
  }),
  docente({
    id: '6',
    nombres: 'Miguel Ángel',
    apellidos: 'Pérez',
    numeroDocumento: '9876543210',
    correoInstitucional: 'miguel.perez@yarumito.edu.co',
    telefono: '3009876543',
    tipoVinculacion: 'PLANTA',
    estado: 'RETIRADO',
  }),
  docente({
    id: '7',
    nombres: 'Ana María',
    apellidos: 'Gómez',
    numeroDocumento: '1098765432',
    correoInstitucional: 'ana.gomez@yarumito.edu.co',
    telefono: '3112223344',
    tipoVinculacion: 'PLANTA',
    estado: 'ACTIVO',
  }),
  docente({
    id: '8',
    nombres: 'Pedro Luis',
    apellidos: 'Ramírez',
    numeroDocumento: '1087654321',
    correoInstitucional: 'pedro.ramirez@yarumito.edu.co',
    telefono: '3123334455',
    tipoVinculacion: 'PLANTA',
    estado: 'LICENCIA',
  }),
  docente({
    id: '9',
    nombres: 'Camila Andrea',
    apellidos: 'Soto',
    numeroDocumento: '1076543210',
    correoInstitucional: 'camila.soto@yarumito.edu.co',
    telefono: '3134445566',
    tipoVinculacion: 'PROVISIONAL',
    estado: 'ACTIVO',
  }),
  docente({
    id: '10',
    nombres: 'Andrés Felipe',
    apellidos: 'Muñoz',
    numeroDocumento: '1065432109',
    correoInstitucional: 'andres.munoz@yarumito.edu.co',
    telefono: '3145556677',
    tipoVinculacion: 'PLANTA',
    estado: 'ACTIVO',
  }),
  docente({
    id: '11',
    nombres: 'Valentina',
    apellidos: 'Ríos',
    numeroDocumento: '1054321098',
    correoInstitucional: 'valentina.rios@yarumito.edu.co',
    telefono: '3156667788',
    tipoVinculacion: 'CONTRATO',
    estado: 'ACTIVO',
  }),
  docente({
    id: '12',
    nombres: 'Sebastián',
    apellidos: 'Hoyos',
    numeroDocumento: '1043210987',
    correoInstitucional: 'sebastian.hoyos@yarumito.edu.co',
    telefono: '3167778899',
    tipoVinculacion: 'PLANTA',
    estado: 'LICENCIA',
  }),
  docente({
    id: '13',
    nombres: 'Mariana',
    apellidos: 'López Cano',
    numeroDocumento: '1032109876',
    correoInstitucional: 'mariana.lopez@yarumito.edu.co',
    telefono: '3178889900',
    tipoVinculacion: 'PROVISIONAL',
    estado: 'ACTIVO',
  }),
  docente({
    id: '14',
    nombres: 'Julián David',
    apellidos: 'Restrepo',
    numeroDocumento: '1021098765',
    correoInstitucional: 'julian.restrepo@yarumito.edu.co',
    telefono: '3189990011',
    tipoVinculacion: 'PLANTA',
    estado: 'ACTIVO',
  }),
]

export const teacherStats = teacherStatsFromCounts({
  total: 0,
  active: 0,
  license: 0,
  retired: 0,
})
