export { api, apiRequest, ApiError } from './client'
export {
  actualizarAula,
  consultarAula,
  eliminarAula,
  etiquetaTipoAula,
  listarAulasPorSede,
  registrarAula,
  registrarNuevaVersionAula,
  tiposAula,
} from './aulas'
export type { ActualizarAulaRequest, AulaRequest, AulaResponse, TipoAula } from './aulas'
export {
  actualizarTurno,
  asegurarTurnosBase,
  consultarTurno,
  horaCorta,
  listarCatalogoTurnos,
  registrarTurno,
} from './turnos'
export type { TurnoRequest, TurnoResponse } from './turnos'
export {
  actualizarFranjaHoraria,
  consultarFranjaHoraria,
  horaDesdeMinutos,
  listarFranjasPorTurno,
  minutosDelDia,
  registrarFranjaHoraria,
} from './franjasHorarias'
export type { FranjaHorariaRequest, FranjaHorariaResponse } from './franjasHorarias'
export {
  listarAreasPredefinidas,
  listarAsignaturasPredefinidas,
  listarEscalafones,
  listarRegimenesDocentes,
  regimenDesdeEscalafon,
} from './catalogos'
export type { OpcionAsignaturaPredefinida, OpcionCatalogo } from './catalogos'
export { getAuthenticatedUser, login, logout, verifySession } from './auth'
export { etiquetaUsuario, listarUsuarios } from './usuarios'
export type { UsuarioListado } from './usuarios'
export {
  actualizarArea,
  cambiarEstadoArea,
  consultarArea,
  listarAreas,
  listarCatalogoAreas,
  registrarArea,
  toAreaSelectOptions,
} from './areas'
export {
  actualizarAsignatura,
  cambiarEstadoAsignatura,
  consultarAsignatura,
  listarAsignaturas,
  registrarAsignatura,
} from './asignaturas'
export {
  actualizarDocente,
  cambiarEstadoDocente,
  consultarDocente,
  consultarResumenDocentes,
  listarDocentes,
  registrarDocente,
  toTeacher,
} from './docentes'
export {
  actualizarIdoneidad,
  consultarIdoneidad,
  finalizarVigenciaIdoneidad,
  listarIdoneidadesPorDocente,
  registrarIdoneidad,
} from './idoneidades'
export {
  actualizarGrado,
  consultarGrado,
  etiquetaGrado,
  gradoPorEtiqueta,
  gradoPorId,
  listarCatalogoGrados,
  opcionesFiltroGrados,
  opcionesGradoRequerido,
  opcionesSelectorPlan,
  planDesdeGrado,
  registrarGrado,
} from './grados'
export type { GradoRequest, GradoResponse } from './grados'
export {
  aAcademicYear,
  actualizarAnioEscolar,
  anioEscolarVisible,
  consultarAnioEscolar,
  listarCatalogoAniosEscolares,
  opcionesAniosEscolares,
  registrarAnioEscolar,
} from './aniosEscolares'
export type { AnioEscolarRequest, AnioEscolarResponse } from './aniosEscolares'
export {
  actualizarPlanEstudios,
  consultarPlanEstudios,
  listarPlanesEstudio,
  listarPlanesEstudioRetirados,
  reactivarPlanEstudios,
  registrarPlanEstudios,
  retirarPlanEstudios,
} from './planesEstudio'
export type { PlanEstudiosRequest, PlanEstudiosResponse } from './planesEstudio'
export {
  actualizarSede,
  consultarSede,
  etiquetaCatalogoSedes,
  listarCatalogoSedes,
  opcionesCatalogoSedes,
  registrarSede,
  sedeVisible,
} from './sedes'
export type { SedeRequest, SedeResponse } from './sedes'
export {
  actualizarTituloProfesional,
  consultarTituloProfesional,
  listarTitulosActivosPorDocente,
  listarTitulosPorDocente,
  listarTitulosRetiradosPorDocente,
  reactivarTituloProfesional,
  registrarTituloProfesional,
  retirarTituloProfesional,
} from './titulos'
export {
  actualizarGrupo,
  consultarGrupo,
  gruposVisibles,
  listarCatalogoGrupos,
  registrarGrupo,
  sugerirCodigoGrupo,
} from './grupos'
export type { CrearGrupoRequest, GrupoResponse } from './grupos'
export {
  actualizarClaseProgramada,
  bandasDelTablero,
  clasesDelTablero,
  consultarHorarioGrupo,
  crearClaseProgramada,
  diaSemanaApi,
  eliminarClaseProgramada,
  etiquetaEstadoHorario,
  metricasDelTablero,
  pendientesDelTablero,
} from './horarios'
export {
  cargarDatosPublicacion,
  crearBorradorDeMejora,
  crearBorradoresDeMejora,
  descartarBorradorHorario,
  horariosPublicables,
  listarVersionesHorario,
  publicarHorario,
  publicarHorariosEnBorrador,
} from './publicacionHorario'
export type { DatosPublicacion, HorarioVersionResponse, PublicarHorarioResponse } from './publicacionHorario'
export {
  aConflictoRevisado,
  listarConflictosPendientes,
  listarConflictosPorHorario,
  listarConflictosPorSeveridad,
  marcarConflictoResuelto,
} from './conflictos'
export type { ConflictoResponse, ConflictoVista, SeveridadConflicto, TipoConflicto } from './conflictos'
export type {
  ActualizarClaseProgramadaRequest,
  AsignacionPendienteResponse,
  ClaseProgramadaResponse,
  ClaseVista,
  CrearClaseProgramadaRequest,
  EstadoHorario,
  HorarioGrupoResponse,
  ResumenHorarioResponse,
} from './horarios'
export {
  actualizarAsignacion,
  confirmarAsignacion,
  consultarAsignacion,
  listarAsignacionesPorDocente,
  listarAsignacionesPorGrupo,
  listarBorradores,
  registrarAsignacion,
  regresarAsignacionABorrador,
  retirarAsignacion,
} from './asignaciones'
export { asignarActividadDocente, listarActividadesDocente } from './actividadesDocente'
export type {
  AsignacionActividadResponse,
  CrearAsignacionActividadRequest,
  TipoActividadInstitucional,
} from './actividadesDocente'
export type {
  ActualizarAsignacionRequest,
  AsignacionAcademicaResponse,
  CrearAsignacionAcademicaRequest,
  EstadoAsignacionAcademica,
} from './asignaciones'
export {
  consultarAuditoriaPorEntidad,
  consultarAuditoriaPorUsuario,
} from './registrosAuditoria'
export {
  consultarInformeHorario,
  descargarInformeHorario,
} from './informesHorarios'
export {
  consultarInformeAreasAsignaturas,
  consultarInformePerfilesProfesionales,
  consultarInformePlantaDocente,
  descargarInformeAreasAsignaturas,
  descargarInformePerfilesProfesionales,
  descargarInformePlantaDocente,
} from './informesInstitucionales'
export {
  consultarParametroInstitucionalPorClave,
  listarParametrosInstitucionalesPorAnio,
} from './parametrosInstitucionales'
export type { ParametroInstitucionalResponse } from './parametrosInstitucionales'
export type {
  FormatoInformeHorario,
  InformeHorarioCarga,
  InformeHorarioClaseDetalle,
  InformeHorariosFiltroRequest,
  InformeHorariosResponse,
  VersionInformeHorario,
} from './informesHorarios'
export type {
  FiltroAreasAsignaturas,
  FiltroPerfilProfesional,
  FiltroPlantaDocente,
  FormatoInformeInstitucional,
  InformeAreasAsignaturas,
  InformeAreaDetalle,
  InformeAsignaturaDetalle,
  InformeIdoneidadDetalle,
  InformePerfilDocente,
  InformePerfilProfesional,
  InformePlantaDocente,
  InformePlantaDocenteDetalle,
  InformeTituloDetalle,
  TipoIdoneidadInforme,
  TipoVinculacionInforme,
} from './informesInstitucionales'
export type { EntidadAuditable, RegistroAuditoriaResponse } from './registrosAuditoria'
export {
  actualizarRestriccion,
  aRestriccionVista,
  consultarRestriccion,
  diaApi,
  listarHistoricoRestriccionesPorAnio,
  listarHistoricoRestriccionesPorDocenteYAnio,
  listarRestriccionesPorAnio,
  listarRestriccionesPorDocenteYAnio,
  listarRestriccionesRetiradasPorAnio,
  listarRestriccionesRetiradasPorDocenteYAnio,
  reactivarRestriccion,
  registrarRestriccion,
  retirarRestriccion,
  tipoApi,
} from './restriccionesDocentes'
export type {
  ActualizarRestriccionDocenteRequest,
  CrearRestriccionDocenteRequest,
  RestriccionDocenteResponse,
  RestriccionDocenteVista,
} from './restriccionesDocentes'
export { useCatalogoAniosEscolares } from './useCatalogoAniosEscolares'
export { useCatalogoGrados } from './useCatalogoGrados'
export { useCatalogoGrupos } from './useCatalogoGrupos'
export { useCatalogoSedes } from './useCatalogoSedes'
export {
  clearSession,
  getToken,
  getUser,
  saveSession,
  updateStoredUser,
} from './session'
export type { AuthUser, LoginPayload, LoginResult } from './types'
