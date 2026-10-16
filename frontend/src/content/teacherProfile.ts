import { PENDING_BACKEND_LABEL } from './teachers'

export type TipoIdoneidad = 'PRINCIPAL' | 'AUTORIZADA' | 'EXCEPCIONAL'

export type ProfessionalTitle = {
  id: string
  level: string
  name: string
  institution: string
  year: string
  supportFile?: string
}

export type Idoneidad = {
  id: string
  docenteId: string
  areaId: string
  asignaturaId: string
  tipo: TipoIdoneidad
  tituloSoporteId: string
  justificacion: string
  vigenteDesde: string
  vigenteHasta: string
  aprobadaPorId: string
}

export type CrearIdoneidadForm = {
  areaId: string
  asignaturaId: string
  tipo: TipoIdoneidad
  tituloSoporteId: string
  justificacion: string
  vigenteDesde: string
  vigenteHasta: string
}

export const emptyCrearIdoneidadForm: CrearIdoneidadForm = {
  areaId: '',
  asignaturaId: '',
  tipo: 'PRINCIPAL',
  tituloSoporteId: '',
  justificacion: '',
  vigenteDesde: '',
  vigenteHasta: '',
}

export type CrearIdoneidadRequest = {
  docenteId: string
  areaId: string
  asignaturaId?: string
  tipo: TipoIdoneidad
  tituloSoporteId?: string
  justificacion?: string
  vigenteDesde: string
  vigenteHasta?: string
}

export type ActualizarIdoneidadRequest = {
  tituloSoporteId?: string
  justificacion?: string
}

export type FinalizarVigenciaIdoneidadRequest = {
  vigenteHasta: string
}

export type CrearTituloForm = {
  level: string
  name: string
  institution: string
  year: string
  supportFile: string
}

export const emptyCrearTituloForm: CrearTituloForm = {
  level: '',
  name: '',
  institution: '',
  year: '',
  supportFile: '',
}

export type CrearTituloProfesionalRequest = {
  docenteId: string
  nivel: string
  nombreTitulo: string
  institucion?: string
  anioGraduacion?: number
  archivoSoporte?: string
}

export type ActualizarTituloProfesionalRequest = {
  nivel: string
  nombreTitulo: string
  institucion?: string
  anioGraduacion?: number
  archivoSoporte?: string
}

const NIVEL_TITULO_LABELS: Record<string, string> = {
  NORMALISTA: 'Normalista',
  LICENCIATURA: 'Licenciatura',
  PROFESIONAL: 'Profesional',
  ESPECIALIZACION: 'Especialización',
  MAESTRIA: 'Maestría',
  DOCTORADO: 'Doctorado',
}

export const titleLevelOptions = [
  { value: '', label: 'Seleccione un nivel' },
  ...Object.entries(NIVEL_TITULO_LABELS).map(([value, label]) => ({
    value,
    label,
  })),
]

export const suitabilityTypeOptions = [
  { value: 'PRINCIPAL', label: 'Principal' },
  { value: 'AUTORIZADA', label: 'Autorizada' },
  { value: 'EXCEPCIONAL', label: 'Excepcional' },
]

export function labelTipoIdoneidad(tipo: TipoIdoneidad) {
  const labels: Record<TipoIdoneidad, string> = {
    PRINCIPAL: 'Principal',
    AUTORIZADA: 'Autorizada',
    EXCEPCIONAL: 'Excepcional',
  }

  return labels[tipo]
}

export function isIdoneidadVigente(item: Idoneidad) {
  return !item.vigenteHasta
}

export function formatVigencia(item: Idoneidad) {
  if (item.vigenteHasta) {
    return `${item.vigenteDesde} – ${item.vigenteHasta}`
  }

  return item.vigenteDesde
}

function optionalText(value: string) {
  const trimmed = value.trim()
  return trimmed === '' ? undefined : trimmed
}

export function toCrearIdoneidadRequest(
  docenteId: string,
  form: CrearIdoneidadForm,
): CrearIdoneidadRequest {
  if (!form.areaId) {
    throw new Error('El área es obligatoria')
  }

  if (!form.vigenteDesde) {
    throw new Error('La fecha de inicio de vigencia es obligatoria')
  }

  return {
    docenteId,
    areaId: form.areaId,
    asignaturaId: optionalText(form.asignaturaId),
    tipo: form.tipo,
    tituloSoporteId: optionalText(form.tituloSoporteId),
    justificacion: optionalText(form.justificacion),
    vigenteDesde: form.vigenteDesde,
    vigenteHasta: optionalText(form.vigenteHasta),
  }
}

export function toActualizarIdoneidadRequest(
  form: CrearIdoneidadForm,
): ActualizarIdoneidadRequest {
  return {
    tituloSoporteId: optionalText(form.tituloSoporteId),
    justificacion: optionalText(form.justificacion),
  }
}

export function toFinalizarVigenciaRequest(
  vigenteHasta: string,
): FinalizarVigenciaIdoneidadRequest {
  return { vigenteHasta }
}

function toTituloProfesionalPayload(
  form: CrearTituloForm,
): ActualizarTituloProfesionalRequest {
  const nombreTitulo = form.name.trim()

  if (!form.level || !(form.level in NIVEL_TITULO_LABELS)) {
    throw new Error('El nivel del título es obligatorio')
  }

  if (!nombreTitulo) {
    throw new Error('El nombre del título es obligatorio')
  }

  return {
    nivel: form.level,
    nombreTitulo,
    institucion: optionalText(form.institution),
    anioGraduacion: parseAnioGraduacion(form.year),
    archivoSoporte: optionalText(form.supportFile),
  }
}

export function toCrearTituloProfesionalRequest(
  docenteId: string,
  form: CrearTituloForm,
): CrearTituloProfesionalRequest {
  return {
    docenteId,
    ...toTituloProfesionalPayload(form),
  }
}

export function toActualizarTituloProfesionalRequest(
  form: CrearTituloForm,
): ActualizarTituloProfesionalRequest {
  return toTituloProfesionalPayload(form)
}

function parseAnioGraduacion(value: string) {
  const trimmed = value.trim()

  if (!trimmed) {
    return undefined
  }

  if (!/^\d{4}$/.test(trimmed)) {
    throw new Error('El año de graduación debe tener 4 dígitos')
  }

  const year = Number(trimmed)
  const maxYear = new Date().getFullYear() + 1

  if (year < 1900 || year > maxYear) {
    throw new Error(`El año de graduación debe estar entre 1900 y ${maxYear}`)
  }

  return year
}

export function idoneidadToForm(item: Idoneidad): CrearIdoneidadForm {
  return {
    areaId: item.areaId,
    asignaturaId: item.asignaturaId,
    tipo: item.tipo,
    tituloSoporteId: item.tituloSoporteId,
    justificacion: item.justificacion,
    vigenteDesde: item.vigenteDesde.slice(0, 10),
    vigenteHasta: item.vigenteHasta.slice(0, 10),
  }
}

export function labelNivelTitulo(nivel: string) {
  return NIVEL_TITULO_LABELS[nivel] ?? (nivel || 'Sin nivel')
}

export function formatTituloSoporte(title: ProfessionalTitle) {
  const year = title.year ? ` (${title.year})` : ''
  return `${labelNivelTitulo(title.level)} · ${title.name}${year}`
}

export function resolveTituloSoporte(
  tituloId: string,
  titles: ProfessionalTitle[],
) {
  if (!tituloId) {
    return 'Sin título'
  }

  const title = titles.find((item) => item.id === tituloId)
  return title ? formatTituloSoporte(title) : 'Título no disponible'
}

export function resolveAreaName(
  areaId: string,
  areas: { id: string; nombre: string }[],
) {
  return areas.find((area) => area.id === areaId)?.nombre ?? PENDING_BACKEND_LABEL
}

export function formatDocumentNumber(value: string) {
  return value.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

export { PENDING_BACKEND_LABEL }
