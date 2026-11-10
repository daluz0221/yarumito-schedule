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
  registrarPlanEstudios,
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
  listarTitulosPorDocente,
  registrarTituloProfesional,
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
