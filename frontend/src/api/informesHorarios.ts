import { api, apiDownload, guardarArchivo } from './client'

export type VersionInformeHorario = 'institucional' | 'publicado'
export type FormatoInformeHorario = 'pdf' | 'excel'

export type InformeHorariosFiltroRequest = {
  anioEscolarId?: string
  periodoAcademicoId?: string
  fechaInicio?: string
  fechaFin?: string
  docenteId?: string
  gradoId?: string
  grupoId?: string
  asignaturaId?: string
  jornada?: string
  dia?: string
  estado?: string
}

export type InformeHorarioClaseDetalle = {
  horarioId: string | null
  versionHorario: number | null
  estadoHorario: string | null
  docenteId: string | null
  nombreDocente: string | null
  grupoId: string | null
  grado: string | null
  nombreGrupo: string | null
  asignaturaId: string | null
  nombreAsignatura: string | null
  diaSemana: string | null
  horaInicio: string | null
  horaFin: string | null
  jornada: string | null
  aulaId: string | null
  nombreAula: string | null
  esHoraExtra: boolean
}

export type InformeHorarioCarga = {
  docenteId: string | null
  nombreDocente: string | null
  grupoId: string | null
  nombreGrupo: string | null
  asignaturaId: string | null
  nombreAsignatura: string | null
  horasOrdinarias: number | null
  horasExtras: number | null
  horasJornadaRegular: number | null
  horasContrajornada: number | null
  totalHorasAcademicas: number | null
}

export type InformeHorariosResponse = {
  anioEscolarId: string | null
  nombreAnioEscolar: string | null
  fechaInicioConsulta: string | null
  fechaFinConsulta: string | null
  nombreInstitucion: string | null
  nombreSede: string | null
  filtrosAplicados: InformeHorariosFiltroRequest | null
  clasesProgramadas: InformeHorarioClaseDetalle[] | null
  resumenCargaHoraria: InformeHorarioCarga[] | null
  totalClasesProgramadas: number | null
  totalDocentes: number | null
  totalGrupos: number | null
  totalAsignaturas: number | null
  horasAcademicasProgramadas: number | null
  horasAcademicasJornadaRegular: number | null
  horasAcademicasContrajornada: number | null
}

const NOMBRES_ARCHIVO: Record<
  VersionInformeHorario,
  Record<FormatoInformeHorario, string>
> = {
  institucional: {
    pdf: 'horario-institucional.pdf',
    excel: 'horario-institucional.xlsx',
  },
  publicado: {
    pdf: 'horario-publicado.pdf',
    excel: 'horario-publicado.xlsx',
  },
}

export function rutaInformeHorario(
  grupoId: string,
  version: VersionInformeHorario,
  formato?: FormatoInformeHorario,
) {
  const base = `/api/v1/informes/horarios/grupos/${grupoId}`
  const publicado = version === 'publicado' ? '/publicado' : ''
  return formato ? `${base}${publicado}/${formato}` : `${base}${publicado}`
}

export function consultarInformeHorario(
  grupoId: string,
  version: VersionInformeHorario,
  filtros: InformeHorariosFiltroRequest,
  signal?: AbortSignal,
) {
  return api.post<InformeHorariosResponse, InformeHorariosFiltroRequest>(
    rutaInformeHorario(grupoId, version),
    filtros,
    { signal },
  )
}

export async function descargarInformeHorario(
  grupoId: string,
  version: VersionInformeHorario,
  formato: FormatoInformeHorario,
  filtros: InformeHorariosFiltroRequest,
  signal?: AbortSignal,
) {
  const archivo = await apiDownload(rutaInformeHorario(grupoId, version, formato), {
    body: filtros,
    filename: NOMBRES_ARCHIVO[version][formato],
    signal,
  })

  guardarArchivo(archivo.blob, archivo.filename)
  return archivo.filename
}
