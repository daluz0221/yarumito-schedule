export { api, apiRequest, ApiError } from './client'
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
  etiquetaGrado,
  gradoPorEtiqueta,
  gradoPorId,
  listarCatalogoGrados,
  opcionesFiltroGrados,
  opcionesGradoRequerido,
  opcionesSelectorPlan,
  planDesdeGrado,
} from './grados'
export {
  anioEscolarVisible,
  listarCatalogoAniosEscolares,
  opcionesAniosEscolares,
} from './aniosEscolares'
export { listarPlanesEstudio } from './planesEstudio'
export {
  etiquetaCatalogoSedes,
  listarCatalogoSedes,
  opcionesCatalogoSedes,
  sedeVisible,
} from './sedes'
export {
  actualizarTituloProfesional,
  consultarTituloProfesional,
  listarTitulosPorDocente,
  registrarTituloProfesional,
} from './titulos'
export { gruposVisibles, listarCatalogoGrupos, registrarGrupo } from './grupos'
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
