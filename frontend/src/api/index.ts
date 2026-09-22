export { api, apiRequest, ApiError } from './client'
export { getAuthenticatedUser, login, logout, verifySession } from './auth'
export { listarAreas, toAreaSelectOptions } from './areas'
export {
  actualizarDocente,
  consultarDocente,
  consultarResumenDocentes,
  listarDocentes,
  registrarDocente,
  toTeacher,
} from './docentes'
export {
  actualizarIdoneidad,
  finalizarVigenciaIdoneidad,
  listarIdoneidadesPorDocente,
  registrarIdoneidad,
} from './idoneidades'
export {
  clearSession,
  getToken,
  getUser,
  saveSession,
  updateStoredUser,
} from './session'
export type { AuthUser, LoginPayload, LoginResult } from './types'
