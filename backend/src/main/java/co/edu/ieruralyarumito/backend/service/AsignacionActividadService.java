package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignacionActividadResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionActividadRequest;
import co.edu.ieruralyarumito.backend.entity.ActividadInstitucional;
import co.edu.ieruralyarumito.backend.entity.AsignacionActividad;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionActividadMapper;
import co.edu.ieruralyarumito.backend.repository.ActividadInstitucionalRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionActividadRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio de las actividades asignadas a docentes.
@Service
public class AsignacionActividadService {

    private final AsignacionActividadRepository asignacionActividadRepository;
    private final ActividadInstitucionalRepository actividadInstitucionalRepository;
    private final DocenteRepository docenteRepository;
    private final AsignacionActividadMapper asignacionActividadMapper;

    public AsignacionActividadService(
            AsignacionActividadRepository asignacionActividadRepository,
            ActividadInstitucionalRepository actividadInstitucionalRepository,
            DocenteRepository docenteRepository,
            AsignacionActividadMapper asignacionActividadMapper) {

        this.asignacionActividadRepository = asignacionActividadRepository;
        this.actividadInstitucionalRepository = actividadInstitucionalRepository;
        this.docenteRepository = docenteRepository;
        this.asignacionActividadMapper = asignacionActividadMapper;
    }

    // CA-11: asigna una actividad institucional existente a un docente.
    // Sólo guarda las horas y la justificación; no consolida la carga total.
    @Transactional
    public AsignacionActividadResponse asignarActividad(
            UUID docenteId,
            CrearAsignacionActividadRequest request) {

        Docente docente = docenteRepository
                .findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El docente no existe"));

        ActividadInstitucional actividad = actividadInstitucionalRepository
                .findById(request.getActividadId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La actividad institucional no existe"));

        AsignacionActividad asignacion =
                asignacionActividadMapper.toEntity(
                        request,
                        docente,
                        actividad);

        AsignacionActividad guardada =
                asignacionActividadRepository.save(asignacion);

        return asignacionActividadMapper.toResponse(guardada);
    }

    // Lista las actividades compensatorias asignadas a un docente.
    @Transactional(readOnly = true)
    public List<AsignacionActividadResponse> listarPorDocente(
            UUID docenteId) {

        if (!docenteRepository.existsById(docenteId)) {
            throw new RecursoNoEncontradoException(
                    "El docente no existe");
        }

        return asignacionActividadRepository
                .findByDocenteId_Id(docenteId)
                .stream()
                .map(asignacionActividadMapper::toResponse)
                .toList();
    }
}
