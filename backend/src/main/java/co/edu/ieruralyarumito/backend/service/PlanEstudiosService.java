package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.CrearPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.PlanEstudiosResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Contiene la lógica de negocio para la gestión del plan de estudios.
@Service
public class PlanEstudiosService {

    private final PlanEstudiosRepository planEstudiosRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final GradoRepository gradoRepository;
    private final AsignaturaRepository asignaturaRepository;

    // Inyección de dependencias mediante constructor.
    public PlanEstudiosService(
            PlanEstudiosRepository planEstudiosRepository,
            AnioEscolarRepository anioEscolarRepository,
            GradoRepository gradoRepository,
            AsignaturaRepository asignaturaRepository) {

        this.planEstudiosRepository = planEstudiosRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.gradoRepository = gradoRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    // Obtiene un año escolar existente.
    private AnioEscolar obtenerAnioEscolar(UUID anioEscolarId) {

        return anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));
    }

    // Obtiene un grado existente.
    private Grado obtenerGrado(UUID gradoId) {

        return gradoRepository.findById(gradoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grado no existe"));
    }

    // Obtiene una asignatura existente.
    private Asignatura obtenerAsignatura(UUID asignaturaId) {

        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignatura no existe"));
    }

    // Obtiene un registro existente del plan de estudios.
    private PlanEstudios obtenerPlanEstudios(UUID id) {

        return planEstudiosRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El registro del plan de estudios no existe"));
    }

    // Evita registrar dos veces la misma asignatura
    // para el mismo grado dentro del mismo año escolar.
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

    // Valida duplicidad al actualizar excluyendo el registro actual.
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

    // Normaliza una observación opcional.
    private String normalizarObservacion(String observacion) {

        if (observacion == null) {
            return null;
        }

        String observacionNormalizada =
                observacion.trim();

        return observacionNormalizada.isEmpty()
                ? null
                : observacionNormalizada;
    }

    // Convierte la entidad PlanEstudios en el DTO devuelto por la API.
    private PlanEstudiosResponse convertirAResponse(
            PlanEstudios planEstudios) {

        PlanEstudiosResponse response =
                new PlanEstudiosResponse();

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

        response.setTurnoId(
                planEstudios.getTurnoId());

        response.setObservacion(
                planEstudios.getObservacion());

        return response;
    }

    // Registra una asignatura dentro del plan de estudios.
    @Transactional
    public PlanEstudiosResponse registrarPlanEstudios(
            CrearPlanEstudiosRequest request) {

        validarRegistroDuplicado(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId());

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(
                        request.getAnioEscolarId());

        Grado grado =
                obtenerGrado(
                        request.getGradoId());

        Asignatura asignatura =
                obtenerAsignatura(
                        request.getAsignaturaId());

        PlanEstudios planEstudios =
                new PlanEstudios();

        planEstudios.setAnioEscolar(anioEscolar);
        planEstudios.setGrado(grado);
        planEstudios.setAsignatura(asignatura);

        // Corresponde a la intensidad semanal que recibe el grado
        // para esta asignatura; no a la carga laboral del docente.
        planEstudios.setHorasSemanales(
                request.getHorasSemanales());

        planEstudios.setTurnoId(
                request.getTurnoId());

        planEstudios.setObservacion(
                normalizarObservacion(
                        request.getObservacion()));

        PlanEstudios planGuardado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planGuardado);
    }

    // Consulta un registro del plan de estudios.
    @Transactional(readOnly = true)
    public PlanEstudiosResponse consultarPlanEstudios(
            UUID id) {

        return convertirAResponse(
                obtenerPlanEstudios(id));
    }

    // Lista los registros del plan de estudios con paginación.
    @Transactional(readOnly = true)
    public Page<PlanEstudiosResponse> listarPlanEstudios(
            Pageable pageable) {

        return planEstudiosRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un registro existente del plan de estudios.
    @Transactional
    public PlanEstudiosResponse actualizarPlanEstudios(
            UUID id,
            ActualizarPlanEstudiosRequest request) {

        PlanEstudios planEstudios =
                obtenerPlanEstudios(id);

        validarRegistroDuplicadoAlActualizar(
                request.getAnioEscolarId(),
                request.getGradoId(),
                request.getAsignaturaId(),
                id);

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(
                        request.getAnioEscolarId());

        Grado grado =
                obtenerGrado(
                        request.getGradoId());

        Asignatura asignatura =
                obtenerAsignatura(
                        request.getAsignaturaId());

        planEstudios.setAnioEscolar(anioEscolar);
        planEstudios.setGrado(grado);
        planEstudios.setAsignatura(asignatura);
        planEstudios.setHorasSemanales(
                request.getHorasSemanales());
        planEstudios.setTurnoId(
                request.getTurnoId());
        planEstudios.setObservacion(
                normalizarObservacion(
                        request.getObservacion()));

        PlanEstudios planActualizado =
                planEstudiosRepository.save(planEstudios);

        return convertirAResponse(planActualizado);
    }
}
