package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Contiene la lógica de negocio para la gestión de idoneidades docentes.
@Service
public class IdoneidadService {

    // Acceso a los datos de idoneidad.
    private final IdoneidadRepository idoneidadRepository;

    // Permite consultar docentes.
    private final DocenteRepository docenteRepository;

    // Permite consultar áreas.
    private final AreaRepository areaRepository;

    // Permite consultar asignaturas.
    private final AsignaturaRepository asignaturaRepository;

    // Permite consultar títulos profesionales.
    private final TituloProfesionalRepository tituloProfesionalRepository;

    // Inyección de dependencias mediante constructor.
    public IdoneidadService(
            IdoneidadRepository idoneidadRepository,
            DocenteRepository docenteRepository,
            AreaRepository areaRepository,
            AsignaturaRepository asignaturaRepository,
            TituloProfesionalRepository tituloProfesionalRepository) {

        this.idoneidadRepository = idoneidadRepository;
        this.docenteRepository = docenteRepository;
        this.areaRepository = areaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.tituloProfesionalRepository = tituloProfesionalRepository;
    }

    // Busca el docente indicado y rechaza la operación si no existe.
    private Docente obtenerDocente(UUID docenteId) {
        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Busca el área indicada y rechaza la operación si no existe.
    private Area obtenerArea(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área no existe"));
    }

    // Busca la asignatura indicada y rechaza la operación si no existe.
    private Asignatura obtenerAsignatura(UUID asignaturaId) {
        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignatura no existe"));
    }

    // Busca el título profesional indicado y rechaza la operación si no existe.
    private TituloProfesional obtenerTituloProfesional(UUID tituloSoporteId) {
        return tituloProfesionalRepository.findById(tituloSoporteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"));
    }

    // Convierte una entidad Idoneidad en el DTO que devuelve la API.
    private IdoneidadResponse convertirAResponse(Idoneidad idoneidad) {

        IdoneidadResponse response = new IdoneidadResponse();

        response.setId(idoneidad.getId());
        response.setDocenteId(idoneidad.getDocente().getId());
        response.setAreaId(idoneidad.getArea().getId());

        // La asignatura es opcional.
        if (idoneidad.getAsignatura() != null) {
            response.setAsignaturaId(idoneidad.getAsignatura().getId());
        }

        response.setTipo(idoneidad.getTipo());
        response.setJustificacion(idoneidad.getJustificacion());
        response.setVigenteDesde(idoneidad.getVigenteDesde());
        response.setVigenteHasta(idoneidad.getVigenteHasta());

        // El título profesional es opcional.
        if (idoneidad.getTituloSoporte() != null) {
            response.setTituloSoporteId(idoneidad.getTituloSoporte().getId());
        }

        // El usuario aprobador es opcional.
        if (idoneidad.getAprobadaPor() != null) {
            response.setAprobadaPorId(idoneidad.getAprobadaPor().getId());
        }
        return response;
    }

    // Registra una nueva idoneidad docente.
    @Transactional
    public IdoneidadResponse registrarIdoneidad(CrearIdoneidadRequest request) {

        // Valida que el docente exista.
        Docente docente = obtenerDocente(request.getDocenteId());

        // Valida que el área exista.
        Area area = obtenerArea(request.getAreaId());

        // Construye la entidad Idoneidad.
        Idoneidad idoneidad = new Idoneidad();

        idoneidad.setDocente(docente);
        idoneidad.setArea(area);

        // Asocia la asignatura únicamente cuando fue enviada.
        if (request.getAsignaturaId() != null) {
            Asignatura asignatura = obtenerAsignatura(request.getAsignaturaId());
            idoneidad.setAsignatura(asignatura);
        }

        // Establece el tipo de idoneidad.
        idoneidad.setTipo(request.getTipo());

        // Asocia el título profesional únicamente cuando fue enviado.
        if (request.getTituloSoporteId() != null) {
            TituloProfesional tituloSoporte =
                    obtenerTituloProfesional(request.getTituloSoporteId());

            idoneidad.setTituloSoporte(tituloSoporte);
        }

        // Completa los datos de la idoneidad.
        idoneidad.setJustificacion(request.getJustificacion());
        idoneidad.setVigenteDesde(request.getVigenteDesde());
        idoneidad.setVigenteHasta(request.getVigenteHasta());

        // Guarda la idoneidad en la base de datos.
        Idoneidad idoneidadGuardada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadGuardada);
    }
}
