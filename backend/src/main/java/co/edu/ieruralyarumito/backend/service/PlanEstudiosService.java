package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.CrearPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.PlanEstudiosResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.entity.Usuario;

import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class PlanEstudiosService {

    private final PlanEstudiosRepository planEstudiosRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final GradoRepository gradoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final TurnoRepository turnoRepository;
    private final AsignacionAcademicaRepository asignacionAcademicaRepository;

    private UsuarioRepository usuarioRepository;
    private RegistroAuditoriaService registroAuditoriaService;

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

    // H-16: integración con auditoría sin modificar
    // el constructor utilizado por las pruebas anteriores.
    @Autowired
    public void configurarAuditoria(
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.registroAuditoriaService =
                Objects.requireNonNull(registroAuditoriaService);
    }

    private AnioEscolar obtenerAnioEscolar(UUID id) {

        return anioEscolarRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El año escolar no existe"));
    }

    private Grado obtenerGrado(UUID id) {

        return gradoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El grado no existe"));
    }

    private Asignatura obtenerAsignatura(UUID id) {

        return asignaturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignatura no existe"));
    }

    private Turno obtenerTurno(UUID id) {

        if (id == null) {
            return null;
        }

        return turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El turno no existe"));
    }

    private PlanEstudios obtenerPlanEstudios(UUID id) {

        return planEstudiosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El registro del plan de estudios no existe"));
    }

    private void validarRegistroDuplicado(
            UUID anioId,
            UUID gradoId,
            UUID asignaturaId) {

        if (planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioId, gradoId, asignaturaId)) {

            throw new RecursoDuplicadoException(
                    "La asignatura ya está registrada para ese grado "
                            + "en el año escolar indicado");
        }
    }

    private void validarRegistroDuplicadoAlActualizar(
            UUID anioId,
            UUID gradoId,
            UUID asignaturaId,
            UUID id) {

        if (planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_IdAndIdNot(
                        anioId, gradoId, asignaturaId, id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro registro de esa asignatura para ese grado "
                            + "en el año escolar indicado");
        }
    }

    private String normalizarObservacion(String observacion) {

        if (observacion == null) {
            return null;
        }

        String valor = observacion.trim();

        return valor.isEmpty() ? null : valor;
    }

    private PlanEstudiosResponse convertirAResponse(PlanEstudios plan) {

        PlanEstudiosResponse response = new PlanEstudiosResponse();

        response.setId(plan.getId());
        response.setAnioEscolarId(plan.getAnioEscolar().getId());
        response.setAnioEscolar(plan.getAnioEscolar().getAnio());
        response.setGradoId(plan.getGrado().getId());
        response.setGradoNivel(plan.getGrado().getNivel());
        response.setGradoNombre(plan.getGrado().getNombre());
        response.setAsignaturaId(plan.getAsignatura().getId());
        response.setAsignaturaNombre(plan.getAsignatura().getNombre());
        response.setAsignaturaCodigo(plan.getAsignatura().getCodigo());
        response.setHorasSemanales(plan.getHorasSemanales());

        if (plan.getTurno() != null) {
            response.setTurnoId(plan.getTurno().getId());
        }

        response.setObservacion(plan.getObservacion());
        response.setActivo(plan.isActivo());

        return response;
    }

    @Transactional
    public PlanEstudiosResponse registrarPlanEstudios(
            CrearPlanEstudiosRequest request) {

        validarRegistroDuplicado(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId());

        AnioEscolar anio = obtenerAnioEscolar(
                request.getAnioEscolarId());

        Grado grado = obtenerGrado(request.getGradoId());

        Asignatura asignatura = obtenerAsignatura(
                request.getAsignaturaId());

        Turno turno = obtenerTurno(request.getTurnoId());

        PlanEstudios plan = new PlanEstudios();

        plan.setAnioEscolar(anio);
        plan.setGrado(grado);
        plan.setAsignatura(asignatura);
        plan.setHorasSemanales(request.getHorasSemanales());
        plan.setTurno(turno);
        plan.setObservacion(
                normalizarObservacion(request.getObservacion()));
        plan.setActivo(true);

        PlanEstudios guardado = planEstudiosRepository.save(plan);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardado,
                    null,
                    datosPlan(guardado));
        }

        return convertirAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public PlanEstudiosResponse consultarPlanEstudios(UUID id) {

        return convertirAResponse(obtenerPlanEstudios(id));
    }

    @Transactional(readOnly = true)
    public Page<PlanEstudiosResponse> listarPlanEstudios(
            Pageable pageable) {

        return planEstudiosRepository
                .findByActivoTrue(pageable)
                .map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlanEstudiosResponse> listarPlanesRetirados(
            Pageable pageable) {

        return planEstudiosRepository
                .findByActivoFalse(pageable)
                .map(this::convertirAResponse);
    }

    @Transactional
    public PlanEstudiosResponse actualizarPlanEstudios(
            UUID id,
            ActualizarPlanEstudiosRequest request) {

        PlanEstudios plan = obtenerPlanEstudios(id);

        if (!plan.isActivo()) {
            throw new TransicionEstadoNoPermitidaException(
                    "No se puede modificar un registro retirado. "
                            + "Debe reactivarlo primero");
        }

        validarRegistroDuplicadoAlActualizar(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId(),
                id);

        AnioEscolar anio = obtenerAnioEscolar(
                request.getAnioEscolarId());

        Grado grado = obtenerGrado(request.getGradoId());

        Asignatura asignatura = obtenerAsignatura(
                request.getAsignaturaId());

        Turno turno = obtenerTurno(request.getTurnoId());

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosPlan(plan);

        plan.setAnioEscolar(anio);
        plan.setGrado(grado);
        plan.setAsignatura(asignatura);
        plan.setHorasSemanales(request.getHorasSemanales());
        plan.setTurno(turno);
        plan.setObservacion(
                normalizarObservacion(request.getObservacion()));

        PlanEstudios actualizado = planEstudiosRepository.save(plan);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizado,
                    antes,
                    datosPlan(actualizado));
        }

        return convertirAResponse(actualizado);
    }

    // H-04: retiro lógico con protección de asignaciones.
    @Transactional
    public PlanEstudiosResponse retirarPlanEstudios(UUID id) {

        PlanEstudios plan = obtenerPlanEstudios(id);

        if (!plan.isActivo()) {
            throw new TransicionEstadoNoPermitidaException(
                    "El registro ya se encuentra retirado");
        }

        boolean existenAsignaciones =
                !asignacionAcademicaRepository
                        .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                                plan.getAnioEscolar().getId(),
                                plan.getGrado().getId(),
                                plan.getAsignatura().getId())
                        .isEmpty();

        if (existenAsignaciones) {
            throw new TransicionEstadoNoPermitidaException(
                    "No se puede retirar la asignatura del plan "
                            + "porque tiene asignaciones académicas relacionadas");
        }

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosPlan(plan);

        plan.setActivo(false);

        PlanEstudios retirado = planEstudiosRepository.save(plan);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    retirado,
                    antes,
                    datosPlan(retirado));
        }

        return convertirAResponse(retirado);
    }

    // H-04: reactivación sin duplicar el registro original.
    @Transactional
    public PlanEstudiosResponse reactivarPlanEstudios(UUID id) {

        PlanEstudios plan = obtenerPlanEstudios(id);

        if (plan.isActivo()) {
            throw new TransicionEstadoNoPermitidaException(
                    "El registro ya se encuentra activo");
        }

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosPlan(plan);

        plan.setActivo(true);

        PlanEstudios reactivado = planEstudiosRepository.save(plan);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    reactivado,
                    antes,
                    datosPlan(reactivado));
        }

        return convertirAResponse(reactivado);
    }

    // H-16: estado anterior o posterior del plan.
    private Map<String, Object> datosPlan(PlanEstudios plan) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", plan.getId());
        datos.put("anioEscolarId", plan.getAnioEscolar().getId());
        datos.put("gradoId", plan.getGrado().getId());
        datos.put("asignaturaId", plan.getAsignatura().getId());
        datos.put("horasSemanales", plan.getHorasSemanales());

        datos.put("turnoId",
                plan.getTurno() == null
                        ? null : plan.getTurno().getId());

        datos.put("observacion", plan.getObservacion());
        datos.put("activo", plan.isActivo());

        return datos;
    }

    // H-16: reutiliza el registro de auditoría de HU-14.
    private void auditar(
            String accion,
            PlanEstudios plan,
            Object antes,
            Object despues) {

        if (registroAuditoriaService == null) {
            return;
        }

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(auth.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado no identificado para auditoría"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "PlanEstudios",
                plan.getId(),
                antes,
                despues,
                null);
    }
}
