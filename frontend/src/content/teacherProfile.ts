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

export const titleLevelOptions = [
  { value: '', label: 'Seleccione un nivel' },
  { value: 'Normalista', label: 'Normalista' },
  { value: 'Licenciatura', label: 'Licenciatura' },
  { value: 'Profesional', label: 'Profesional' },
  { value: 'Especialización', label: 'Especialización' },
  { value: 'Maestría', label: 'Maestría' },
  { value: 'Doctorado', label: 'Doctorado' },
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
