import { api, apiRequest, ApiError } from "./client";
import {
  actualizarAula,
  consultarAula,
  eliminarAula,
  etiquetaTipoAula,
  listarAulasPorSede,
  registrarAula,
  registrarNuevaVersionAula,
  tiposAula
} from "./aulas";
import {
  actualizarTurno,
  consultarTurno,
  horaCorta,
  listarCatalogoTurnos,
  registrarTurno
} from "./turnos";
import {
  actualizarFranjaHoraria,
  consultarFranjaHoraria,
  horaDesdeMinutos,
  listarFranjasPorTurno,
  minutosDelDia,
  registrarFranjaHoraria
} from "./franjasHorarias";
import {
  listarAreasPredefinidas,
  listarAsignaturasPredefinidas,
  listarEscalafones,
  listarRegimenesDocentes,
  regimenDesdeEscalafon
} from "./catalogos";
import { getAuthenticatedUser, login, logout, verifySession } from "./auth";
import { etiquetaUsuario, listarUsuarios } from "./usuarios";
import {
  actualizarArea,
  cambiarEstadoArea,
  consultarArea,
  listarAreas,
  listarCatalogoAreas,
  registrarArea,
  toAreaSelectOptions
} from "./areas";
import {
  actualizarAsignatura,
  cambiarEstadoAsignatura,
  consultarAsignatura,
  listarAsignaturas,
  registrarAsignatura
} from "./asignaturas";
import {
  actualizarDocente,
  cambiarEstadoDocente,
  consultarDocente,
  consultarResumenDocentes,
  listarDocentes,
  registrarDocente,
  toTeacher
} from "./docentes";
import {
  actualizarIdoneidad,
  consultarIdoneidad,
  finalizarVigenciaIdoneidad,
  listarIdoneidadesPorDocente,
  registrarIdoneidad
} from "./idoneidades";
import {
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
  registrarGrado
} from "./grados";
import {
  aAcademicYear,
  actualizarAnioEscolar,
  anioEscolarVisible,
  consultarAnioEscolar,
  listarCatalogoAniosEscolares,
  opcionesAniosEscolares,
  registrarAnioEscolar
} from "./aniosEscolares";
import {
  actualizarPlanEstudios,
  consultarPlanEstudios,
  listarPlanesEstudio,
  registrarPlanEstudios
} from "./planesEstudio";
import {
  actualizarSede,
  consultarSede,
  etiquetaCatalogoSedes,
  listarCatalogoSedes,
  opcionesCatalogoSedes,
  registrarSede,
  sedeVisible
} from "./sedes";
import {
  actualizarTituloProfesional,
  consultarTituloProfesional,
  listarTitulosPorDocente,
  registrarTituloProfesional
} from "./titulos";
import {
  actualizarGrupo,
  consultarGrupo,
  gruposVisibles,
  listarCatalogoGrupos,
  registrarGrupo,
  sugerirCodigoGrupo
} from "./grupos";
import {
  actualizarClaseProgramada,
  bandasDelTablero,
  clasesDelTablero,
  consultarHorarioGrupo,
  crearClaseProgramada,
  diaSemanaApi,
  eliminarClaseProgramada,
  etiquetaEstadoHorario,
  metricasDelTablero,
  pendientesDelTablero
} from "./horarios";
import {
  aConflictoRevisado,
  listarConflictosPendientes,
  listarConflictosPorHorario,
  listarConflictosPorSeveridad,
  marcarConflictoResuelto
} from "./conflictos";
import {
  confirmarAsignacion,
  consultarAsignacion,
  listarAsignacionesPorDocente,
  listarAsignacionesPorGrupo,
  listarBorradores,
  registrarAsignacion
} from "./asignaciones";
import { asignarActividadDocente, listarActividadesDocente } from "./actividadesDocente";
import {
  consultarAuditoriaPorEntidad,
  consultarAuditoriaPorUsuario
} from "./registrosAuditoria";
import {
  consultarInformeHorario,
  descargarInformeHorario
} from "./informesHorarios";
import {
  consultarInformeAreasAsignaturas,
  consultarInformePerfilesProfesionales,
  consultarInformePlantaDocente,
  descargarInformeAreasAsignaturas,
  descargarInformePerfilesProfesionales,
  descargarInformePlantaDocente
} from "./informesInstitucionales";
import {
  consultarParametroInstitucionalPorClave,
  listarParametrosInstitucionalesPorAnio
} from "./parametrosInstitucionales";
import {
  actualizarRestriccion,
  aRestriccionVista,
  consultarRestriccion,
  diaApi,
  listarRestriccionesPorAnio,
  listarRestriccionesPorDocenteYAnio,
  registrarRestriccion,
  tipoApi
} from "./restriccionesDocentes";
import { useCatalogoAniosEscolares } from "./useCatalogoAniosEscolares";
import { useCatalogoGrados } from "./useCatalogoGrados";
import { useCatalogoGrupos } from "./useCatalogoGrupos";
import { useCatalogoSedes } from "./useCatalogoSedes";
import {
  clearSession,
  getToken,
  getUser,
  saveSession,
  updateStoredUser
} from "./session";
export {
  ApiError,
  aAcademicYear,
  aConflictoRevisado,
  aRestriccionVista,
  actualizarAnioEscolar,
  actualizarArea,
  actualizarAsignatura,
  actualizarAula,
  actualizarClaseProgramada,
  actualizarDocente,
  actualizarFranjaHoraria,
  actualizarGrado,
  actualizarGrupo,
  actualizarIdoneidad,
  actualizarPlanEstudios,
  actualizarRestriccion,
  actualizarSede,
  actualizarTituloProfesional,
  actualizarTurno,
  anioEscolarVisible,
  api,
  apiRequest,
  asignarActividadDocente,
  bandasDelTablero,
  cambiarEstadoArea,
  cambiarEstadoAsignatura,
  cambiarEstadoDocente,
  clasesDelTablero,
  clearSession,
  confirmarAsignacion,
  consultarAnioEscolar,
  consultarArea,
  consultarAsignacion,
  consultarAsignatura,
  consultarAuditoriaPorEntidad,
  consultarAuditoriaPorUsuario,
  consultarAula,
  consultarDocente,
  consultarFranjaHoraria,
  consultarGrado,
  consultarGrupo,
  consultarHorarioGrupo,
  consultarIdoneidad,
  consultarInformeAreasAsignaturas,
  consultarInformeHorario,
  consultarInformePerfilesProfesionales,
  consultarInformePlantaDocente,
  consultarParametroInstitucionalPorClave,
  consultarPlanEstudios,
  consultarRestriccion,
  consultarResumenDocentes,
  consultarSede,
  consultarTituloProfesional,
  consultarTurno,
  crearClaseProgramada,
  descargarInformeAreasAsignaturas,
  descargarInformeHorario,
  descargarInformePerfilesProfesionales,
  descargarInformePlantaDocente,
  diaApi,
  diaSemanaApi,
  eliminarAula,
  eliminarClaseProgramada,
  etiquetaCatalogoSedes,
  etiquetaEstadoHorario,
  etiquetaGrado,
  etiquetaTipoAula,
  etiquetaUsuario,
  finalizarVigenciaIdoneidad,
  getAuthenticatedUser,
  getToken,
  getUser,
  gradoPorEtiqueta,
  gradoPorId,
  gruposVisibles,
  horaCorta,
  horaDesdeMinutos,
  listarActividadesDocente,
  listarAreas,
  listarAreasPredefinidas,
  listarAsignacionesPorDocente,
  listarAsignacionesPorGrupo,
  listarAsignaturas,
  listarAsignaturasPredefinidas,
  listarAulasPorSede,
  listarBorradores,
  listarCatalogoAniosEscolares,
  listarCatalogoAreas,
  listarCatalogoGrados,
  listarCatalogoGrupos,
  listarCatalogoSedes,
  listarCatalogoTurnos,
  listarConflictosPendientes,
  listarConflictosPorHorario,
  listarConflictosPorSeveridad,
  listarDocentes,
  listarEscalafones,
  listarFranjasPorTurno,
  listarIdoneidadesPorDocente,
  listarParametrosInstitucionalesPorAnio,
  listarPlanesEstudio,
  listarRegimenesDocentes,
  listarRestriccionesPorAnio,
  listarRestriccionesPorDocenteYAnio,
  listarTitulosPorDocente,
  listarUsuarios,
  login,
  logout,
  marcarConflictoResuelto,
  metricasDelTablero,
  minutosDelDia,
  opcionesAniosEscolares,
  opcionesCatalogoSedes,
  opcionesFiltroGrados,
  opcionesGradoRequerido,
  opcionesSelectorPlan,
  pendientesDelTablero,
  planDesdeGrado,
  regimenDesdeEscalafon,
  registrarAnioEscolar,
  registrarArea,
  registrarAsignacion,
  registrarAsignatura,
  registrarAula,
  registrarDocente,
  registrarFranjaHoraria,
  registrarGrado,
  registrarGrupo,
  registrarIdoneidad,
  registrarNuevaVersionAula,
  registrarPlanEstudios,
  registrarRestriccion,
  registrarSede,
  registrarTituloProfesional,
  registrarTurno,
  saveSession,
  sedeVisible,
  sugerirCodigoGrupo,
  tipoApi,
  tiposAula,
  toAreaSelectOptions,
  toTeacher,
  updateStoredUser,
  useCatalogoAniosEscolares,
  useCatalogoGrados,
  useCatalogoGrupos,
  useCatalogoSedes,
  verifySession
};
