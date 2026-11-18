
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.CrearPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.PlanEstudiosResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.Turno;

import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Gestiona los registros del plan de estudios.
@Service
public class PlanEstudiosService {

    private final PlanEstudiosRepository planEstudiosRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final GradoRepository gradoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final TurnoRepository turnoRepository;

    // H-04: verifica dependencias antes de retirar
    // una asignatura del plan de estudios.
    private final AsignacionAcademicaRepository asignacionAcademicaRepository;

    public PlanEstudiosService(
            PlanEstudiosRepository planEstudiosRepository,
            AnioEscolarRepository anioEscolarRepository,
            GradoRepository gradoRepository,
            AsignaturaRepository asignaturaRepository,
            TurnoRepository turnoRepository,
            AsignacionAcademicaRepository asignacionAcademicaRepository) {

        this.planEstudiosRepository = planEstudiosRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.gradoRepository = gradoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.turnoRepository = turnoRepository;
        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
    }

    private AnioEscolar obtenerAnioEscolar(UUID anioEscolarId) {

        return anioEscolarRepository
                .findById(anioEscolarId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));
    }

    private Grado obtenerGrado(UUID gradoId) {

        return gradoRepository
                .findById(gradoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grado no existe"));
    }

    private Asignatura obtenerAsignatura(UUID asignaturaId) {

        return asignaturaRepository
                .findById(asignaturaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignatura no existe"));
    }

    // El turno es opcional durante la preparación.
    private Turno obtenerTurno(UUID turnoId) {

        if (turnoId == null) {
            return null;
        }

        return turnoRepository
                .findById(turnoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El turno no existe"));
    }

    private PlanEstudios obtenerPlanEstudios(UUID id) {

        return planEstudiosRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El registro del plan de estudios no existe"));
    }

    // Impide duplicados incluso cuando existe
    // un registro retirado con la misma combinación.
    private void validarRegistroDuplicado(
            UUID anioEscolarId,
            UUID gradoId,
            UUID asignaturaId) {

        if (planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId)) {

            throw new RecursoDuplicadoException(
                    "La asignatura ya está registrada para ese grado "
                            + "en el año escolar indicado");
        }
    }

    private void validarRegistroDuplicadoAlActualizar(
            UUID anioEscolarId,
            UUID gradoId,
            UUID asignaturaId,
            UUID id) {

        if (planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_IdAndIdNot(
                        anioEscolarId,
                        gradoId,
                        asignaturaId,
                        id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro registro de esa asignatura para ese grado "
                            + "en el año escolar indicado");
        }
    }

    private String normalizarObservacion(String observacion) {

        if (observacion == null) {
            return null;
        }

        String observacionNormalizada = observacion.trim();

        return observacionNormalizada.isEmpty()
                ? null
                : observacionNormalizada;
    }

    // Convierte la entidad al DTO de respuesta.
    private PlanEstudiosResponse convertirAResponse(
            PlanEstudios planEstudios) {

        PlanEstudiosResponse response = new PlanEstudiosResponse();

        response.setId(planEstudios.getId());

        response.setAnioEscolarId(
                planEstudios.getAnioEscolar().getId());

        response.setAnioEscolar(
                planEstudios.getAnioEscolar().getAnio());

        response.setGradoId(
                planEstudios.getGrado().getId());

        response.setGradoNivel(
                planEstudios.getGrado().getNivel());

        response.setGradoNombre(
                planEstudios.getGrado().getNombre());

        response.setAsignaturaId(
                planEstudios.getAsignatura().getId());

        response.setAsignaturaNombre(
                planEstudios.getAsignatura().getNombre());

        response.setAsignaturaCodigo(
                planEstudios.getAsignatura().getCodigo());

        response.setHorasSemanales(
                planEstudios.getHorasSemanales());

        if (planEstudios.getTurno() != null) {
            response.setTurnoId(
                    planEstudios.getTurno().getId());
        }

        response.setObservacion(
                planEstudios.getObservacion());

        // H-04: informa si el registro está activo o retirado.
        response.setActivo(planEstudios.isActivo());

        return response;
    }

    // Registra una asignatura en el plan de estudios.
    @Transactional
    public PlanEstudiosResponse registrarPlanEstudios(
            CrearPlanEstudiosRequest request) {

        validarRegistroDuplicado(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId());

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(request.getAnioEscolarId());

        Grado grado =
                obtenerGrado(request.getGradoId());

        Asignatura asignatura =
                obtenerAsignatura(request.getAsignaturaId());

        Turno turno =
                obtenerTurno(request.getTurnoId());

        PlanEstudios planEstudios = new PlanEstudios();

        planEstudios.setAnioEscolar(anioEscolar);
        planEstudios.setGrado(grado);
        planEstudios.setAsignatura(asignatura);
        planEstudios.setHorasSemanales(
                request.getHorasSemanales());
        planEstudios.setTurno(turno);

        planEstudios.setObservacion(
                normalizarObservacion(
                        request.getObservacion()));

        // Los registros nuevos siempre comienzan activos.
        planEstudios.setActivo(true);

        PlanEstudios planGuardado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planGuardado);
    }

    // Consulta un registro, incluyendo los retirados.
    @Transactional(readOnly = true)
    public PlanEstudiosResponse consultarPlanEstudios(UUID id) {

        return convertirAResponse(
                obtenerPlanEstudios(id));
    }

    // H-04: la consulta ordinaria muestra
    // únicamente registros activos.
    @Transactional(readOnly = true)
    public Page<PlanEstudiosResponse> listarPlanEstudios(
            Pageable pageable) {

        return planEstudiosRepository
                .findByActivoTrue(pageable)
                .map(this::convertirAResponse);
    }

    // H-04: consulta histórica de registros retirados.
    @Transactional(readOnly = true)
    public Page<PlanEstudiosResponse> listarPlanesRetirados(
            Pageable pageable) {

        return planEstudiosRepository
                .findByActivoFalse(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un registro activo del plan de estudios.
    @Transactional
    public PlanEstudiosResponse actualizarPlanEstudios(
            UUID id,
            ActualizarPlanEstudiosRequest request) {

        PlanEstudios planEstudios = obtenerPlanEstudios(id);

        // H-04: evita modificar registros históricos retirados.
        if (!planEstudios.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede modificar un registro retirado. "
                            + "Debe reactivarlo primero");
        }

        validarRegistroDuplicadoAlActualizar(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId(),
                id);

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(request.getAnioEscolarId());

        Grado grado =
                obtenerGrado(request.getGradoId());

        Asignatura asignatura =
                obtenerAsignatura(request.getAsignaturaId());

        Turno turno =
                obtenerTurno(request.getTurnoId());

        planEstudios.setAnioEscolar(anioEscolar);
        planEstudios.setGrado(grado);
        planEstudios.setAsignatura(asignatura);
        planEstudios.setHorasSemanales(
                request.getHorasSemanales());
        planEstudios.setTurno(turno);

        planEstudios.setObservacion(
                normalizarObservacion(
                        request.getObservacion()));

        PlanEstudios planActualizado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planActualizado);
    }

    // H-04: retira una asignatura del plan de estudios
    // conservando el registro para consultas históricas.
    @Transactional
    public PlanEstudiosResponse retirarPlanEstudios(UUID id) {

        PlanEstudios planEstudios = obtenerPlanEstudios(id);

        if (!planEstudios.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "El registro ya se encuentra retirado");
        }

        // No se permite retirar una asignatura si existen
        // asignaciones académicas relacionadas con el mismo
        // año escolar, grado y asignatura.
        boolean existenAsignaciones =
                !asignacionAcademicaRepository
                        .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                                planEstudios.getAnioEscolar().getId(),
                                planEstudios.getGrado().getId(),
                                planEstudios.getAsignatura().getId())
                        .isEmpty();

        if (existenAsignaciones) {

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede retirar la asignatura del plan "
                            + "porque tiene asignaciones académicas relacionadas");
        }

        planEstudios.setActivo(false);

        PlanEstudios planRetirado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planRetirado);
    }

    // H-04: reactiva un registro histórico retirado
    // sin crear un registro duplicado.
    @Transactional
    public PlanEstudiosResponse reactivarPlanEstudios(UUID id) {

        PlanEstudios planEstudios = obtenerPlanEstudios(id);

        if (planEstudios.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "El registro ya se encuentra activo");
        }

        planEstudios.setActivo(true);

        PlanEstudios planReactivado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planReactivado);
    }
}
