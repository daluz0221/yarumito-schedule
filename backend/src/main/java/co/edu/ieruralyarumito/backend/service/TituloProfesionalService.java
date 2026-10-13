package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de títulos profesionales.
@Service
public class TituloProfesionalService {

    // Acceso a los datos de títulos profesionales.
    private final TituloProfesionalRepository tituloProfesionalRepository;

    // Permite consultar docentes.
    private final DocenteRepository docenteRepository;

    // Inyección de dependencias mediante constructor.
    public TituloProfesionalService(
            TituloProfesionalRepository tituloProfesionalRepository,
            DocenteRepository docenteRepository) {

        this.tituloProfesionalRepository = tituloProfesionalRepository;
        this.docenteRepository = docenteRepository;
    }

    // Busca el docente indicado y rechaza la operación si no existe.
    private Docente obtenerDocente(UUID docenteId) {
        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Busca el título profesional indicado y rechaza la operación si no existe.
    private TituloProfesional obtenerTituloProfesional(UUID id) {
        return tituloProfesionalRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"));
    }

    // Convierte una entidad TituloProfesional en el DTO que devuelve la API.
    private TituloProfesionalResponse convertirAResponse(
            TituloProfesional tituloProfesional) {

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(tituloProfesional.getId());
        response.setDocenteId(
                tituloProfesional.getDocente().getId());
        response.setNivel(tituloProfesional.getNivel());
        response.setNombreTitulo(
                tituloProfesional.getNombreTitulo());
        response.setInstitucion(
                tituloProfesional.getInstitucion());
        response.setAnioGraduacion(
                tituloProfesional.getAnioGraduacion());
        response.setArchivoSoporte(
                tituloProfesional.getArchivoSoporte());

        return response;
    }

    // Registra un nuevo título profesional para un docente existente.
    @Transactional
    public TituloProfesionalResponse registrarTituloProfesional(
            CrearTituloProfesionalRequest request) {

        // Valida que el docente exista.
        Docente docente =
                obtenerDocente(request.getDocenteId());

        // Construye la entidad TituloProfesional.
        TituloProfesional tituloProfesional =
                new TituloProfesional();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(request.getNivel());
        tituloProfesional.setNombreTitulo(
                request.getNombreTitulo());
        tituloProfesional.setInstitucion(
                request.getInstitucion());
        tituloProfesional.setAnioGraduacion(
                request.getAnioGraduacion());
        tituloProfesional.setArchivoSoporte(
                request.getArchivoSoporte());

        // Guarda el título profesional.
        TituloProfesional tituloGuardado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloGuardado);
    }

    // Consulta un título profesional por su identificador.
    @Transactional(readOnly = true)
    public TituloProfesionalResponse consultarTituloProfesional(
            UUID id) {

        TituloProfesional tituloProfesional =
                obtenerTituloProfesional(id);

        return convertirAResponse(tituloProfesional);
    }

    // Lista los títulos profesionales de un docente existente.
    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse>
    listarTitulosPorDocente(UUID docenteId) {

        // Valida que el docente exista.
        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_Id(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualiza los datos permitidos de un título profesional existente.
    @Transactional
    public TituloProfesionalResponse actualizarTituloProfesional(
            UUID id,
            ActualizarTituloProfesionalRequest request) {

        // Valida que el título profesional exista.
        TituloProfesional tituloProfesional =
                obtenerTituloProfesional(id);

        // El docente propietario no se modifica.
        tituloProfesional.setNivel(request.getNivel());
        tituloProfesional.setNombreTitulo(
                request.getNombreTitulo());
        tituloProfesional.setInstitucion(
                request.getInstitucion());
        tituloProfesional.setAnioGraduacion(
                request.getAnioGraduacion());
        tituloProfesional.setArchivoSoporte(
                request.getArchivoSoporte());

        TituloProfesional tituloActualizado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloActualizado);
    }
}
