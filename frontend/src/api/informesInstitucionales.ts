import { api, apiDownload, guardarArchivo } from './client'

export type FormatoInformeInstitucional = 'pdf' | 'excel'

export type EstadoDocenteInforme = 'ACTIVO' | 'LICENCIA' | 'RETIRADO'
export type TipoVinculacionInforme = 'PLANTA' | 'PROVISIONAL' | 'CONTRATO'
export type TipoIdoneidadInforme = 'PRINCIPAL' | 'AUTORIZADA' | 'EXCEPCIONAL'

export type FiltroPlantaDocente = {
  areaNombramientoId?: string
  estado?: string
  tipoVinculacion?: string
  anioEscolarId?: string
  docenteId?: string
}

export type InformePlantaDocenteDetalle = {
  docenteId: string
  nombreCompleto: string | null
  areaNombramiento: string | null
  estado: EstadoDocenteInforme | null
  tipoVinculacion: TipoVinculacionInforme | null
  horasSemanalesContratadas: number | null
  horasSemanalesAsignadas?: number | null
  horasSemanalesExtras?: number | null
}

export type InformePlantaDocente = {
  cantidadDocentes: number
  totalHorasSemanalesContratadas: number
  docentes: InformePlantaDocenteDetalle[] | null
  anioEscolarId?: string | null
  anioEscolar?: number | null
  totalHorasSemanalesAsignadas?: number | null
  totalHorasSemanalesExtras?: number | null
}

export type FiltroAreasAsignaturas = {
  areaId?: string
  activa?: boolean
}

export type InformeAsignaturaDetalle = {
  asignaturaId: string
  nombre: string | null
  codigo: string | null
  activa: boolean
  esMediaTecnica: boolean
  exigeIdoneidadEstricta: boolean
  requiereDocenteExclusivo: boolean
}

export type InformeAreaDetalle = {
  areaId: string
  nombre: string | null
  codigo: string | null
  activa: boolean
  obligatoria: boolean
  soloMedia: boolean
  cantidadAsignaturas: number
  asignaturas: InformeAsignaturaDetalle[] | null
}

export type InformeAreasAsignaturas = {
  cantidadAreas: number
  cantidadAsignaturas: number
  areas: InformeAreaDetalle[] | null
}

export type FiltroPerfilProfesional = {
  docenteId?: string
  areaId?: string
  tipo?: string
}

export type InformeTituloDetalle = {
  tituloId: string
  nombreTitulo: string | null
  nivel: string | null
  institucion: string | null
  anioGraduacion: number | null
}

export type InformeIdoneidadDetalle = {
  idoneidadId: string
  areaId: string | null
  nombreArea: string | null
  asignaturaId: string | null
  nombreAsignatura: string | null
  tipo: TipoIdoneidadInforme | null
  tituloSoporteId: string | null
  vigenteDesde: string | null
  vigenteHasta: string | null
  vigenteEnFecha: boolean
}

export type InformePerfilDocente = {
  docenteId: string
  nombreCompleto: string | null
  areaNombramiento: string | null
  titulos: InformeTituloDetalle[] | null
  idoneidades: InformeIdoneidadDetalle[] | null
}

export type InformePerfilProfesional = {
  fechaReferencia: string | null
  cantidadDocentes: number
  cantidadTitulos: number
  cantidadIdoneidades: number
  docentes: InformePerfilDocente[] | null
}

const RUTAS = {
  planta: '/api/v1/informes/planta-docente',
  areas: '/api/v1/informes/areas-asignaturas',
  perfiles: '/api/v1/informes/perfiles-profesionales',
} as const

const ARCHIVOS = {
  planta: { pdf: 'planta-docente.pdf', excel: 'planta-docente.xlsx' },
  areas: { pdf: 'areas-asignaturas.pdf', excel: 'areas-asignaturas.xlsx' },
  perfiles: {
    pdf: 'perfiles-profesionales.pdf',
    excel: 'perfiles-profesionales.xlsx',
  },
} as const

function conParametros(path: string, params: Record<string, string | undefined>) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (!value) {
      return
    }

    search.set(key, value)
  })

  const query = search.toString()
  return query ? `${path}?${query}` : path
}

function parametrosPlanta(filtros: FiltroPlantaDocente) {
  return {
    areaNombramientoId: filtros.areaNombramientoId,
    estado: filtros.estado,
    tipoVinculacion: filtros.tipoVinculacion,
    anioEscolarId: filtros.anioEscolarId,
    docenteId: filtros.docenteId,
  }
}

function parametrosAreas(filtros: FiltroAreasAsignaturas) {
  return {
    areaId: filtros.areaId,
    activa: filtros.activa === undefined ? undefined : String(filtros.activa),
  }
}

function parametrosPerfiles(filtros: FiltroPerfilProfesional) {
  return {
    docenteId: filtros.docenteId,
    areaId: filtros.areaId,
    tipo: filtros.tipo,
  }
}

export function consultarInformePlantaDocente(
  filtros: FiltroPlantaDocente,
  signal?: AbortSignal,
) {
  return api.get<InformePlantaDocente>(
    conParametros(RUTAS.planta, parametrosPlanta(filtros)),
    { signal },
  )
}

export function consultarInformeAreasAsignaturas(
  filtros: FiltroAreasAsignaturas,
  signal?: AbortSignal,
) {
  return api.get<InformeAreasAsignaturas>(
    conParametros(RUTAS.areas, parametrosAreas(filtros)),
    { signal },
  )
}

export function consultarInformePerfilesProfesionales(
  filtros: FiltroPerfilProfesional,
  signal?: AbortSignal,
) {
  return api.get<InformePerfilProfesional>(
    conParametros(RUTAS.perfiles, parametrosPerfiles(filtros)),
    { signal },
  )
}

function rutaDescarga(
  base: string,
  formato: FormatoInformeInstitucional,
  params: Record<string, string | undefined>,
) {
  return conParametros(`${base}/${formato}`, params)
}

async function descargar(
  path: string,
  filename: string,
  signal?: AbortSignal,
) {
  const archivo = await apiDownload(path, {
    method: 'GET',
    filename,
    signal,
  })

  guardarArchivo(archivo.blob, archivo.filename)
  return archivo.filename
}

export function descargarInformePlantaDocente(
  formato: FormatoInformeInstitucional,
  filtros: FiltroPlantaDocente,
  signal?: AbortSignal,
) {
  return descargar(
    rutaDescarga(RUTAS.planta, formato, parametrosPlanta(filtros)),
    ARCHIVOS.planta[formato],
    signal,
  )
}

export function descargarInformeAreasAsignaturas(
  formato: FormatoInformeInstitucional,
  filtros: FiltroAreasAsignaturas,
  signal?: AbortSignal,
) {
  return descargar(
    rutaDescarga(RUTAS.areas, formato, parametrosAreas(filtros)),
    ARCHIVOS.areas[formato],
    signal,
  )
}

export function descargarInformePerfilesProfesionales(
  formato: FormatoInformeInstitucional,
  filtros: FiltroPerfilProfesional,
  signal?: AbortSignal,
) {
  return descargar(
    rutaDescarga(RUTAS.perfiles, formato, parametrosPerfiles(filtros)),
    ARCHIVOS.perfiles[formato],
    signal,
  )
}
