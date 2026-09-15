package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import org.springframework.stereotype.Service;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import java.util.UUID;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.dto.DocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.dto.CrearDocenteRequest;
import org.springframework.transaction.annotation.Transactional;
import co.edu.ieruralyarumito.backend.dto.ActualizarDocenteRequest;

// Contiene la lógica de negocio para la gestión de docentes.
@Service
public class DocenteService {

    // Acceso a los datos de docentes.
    private final DocenteRepository docenteRepository;

    // Permite validar y consultar el área de nombramiento del docente.
    private final AreaRepository areaRepository;

    // Inyección de dependencias mediante constructor.
    public DocenteService(
            DocenteRepository docenteRepository,
            AreaRepository areaRepository) {

        this.docenteRepository = docenteRepository;
        this.areaRepository = areaRepository;
    }

    // Busca el área indicada y rechaza la operación si no existe.
    private Area obtenerArea(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área de nombramiento no existe"));
    }

    // Valida que el número de documento no esté registrado por otro docente.
    private void validarDocumentoDuplicado(String numeroDocumento) {
        if (docenteRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un docente con ese número de documento");
        }

    }

    // Valida que el número de documento no pertenezca a otro docente.
    private void validarDocumentoDuplicadoAlActualizar(
            String numeroDocumento,
            UUID id) {

        if (docenteRepository.existsByNumeroDocumentoAndIdNot(
                numeroDocumento, id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro docente con ese número de documento");
        }
    }

    // Convierte la entidad Docente en el DTO que será devuelto por la API.
    private DocenteResponse convertirAResponse(Docente docente) {

        DocenteResponse response = new DocenteResponse();

        response.setId(docente.getId());
        response.setNombres(docente.getNombres());
        response.setApellidos(docente.getApellidos());
        response.setTipoDocumento(docente.getTipoDocumento());
        response.setNumeroDocumento(docente.getNumeroDocumento());
        response.setTelefono(docente.getTelefono());
        response.setCorreoInstitucional(docente.getCorreoInstitucional());
        response.setTipoVinculacion(docente.getTipoVinculacion());
        response.setAreaNombramientoId(docente.getAreaNombramiento().getId());
        response.setNumeroDecreto(docente.getNumeroDecreto());
        response.setFechaDecreto(docente.getFechaDecreto());
        response.setEscalafon(docente.getEscalafon());
        response.setHorasSemanalesContratadas(docente.getHorasSemanalesContratadas());
        response.setMaxHorasExtra(docente.getMaxHorasExtra());
        response.setEsExclusivoMediaTecnica(docente.isEsExclusivoMediaTecnica());
        response.setEstado(docente.getEstado());
        response.setFechaVinculacion(docente.getFechaVinculacion());

        return response;
    }

    // Registra un nuevo docente después de validar sus datos principales.
    @Transactional
    public DocenteResponse registrarDocente(CrearDocenteRequest request) {

        // Evita registrar dos docentes con el mismo número de documento.
        validarDocumentoDuplicado(request.getNumeroDocumento());

        // Valida que el área de nombramiento exista.
        Area area = obtenerArea(request.getAreaNombramientoId());

        // Construye la entidad Docente con los datos recibidos.
        Docente docente = new Docente();

        docente.setNombres(request.getNombres());
        docente.setApellidos(request.getApellidos());
        docente.setTipoDocumento(request.getTipoDocumento());
        docente.setNumeroDocumento(request.getNumeroDocumento());
        docente.setTelefono(request.getTelefono());
        docente.setCorreoInstitucional(request.getCorreoInstitucional());
        docente.setTipoVinculacion(request.getTipoVinculacion());
        docente.setAreaNombramiento(area);
        docente.setNumeroDecreto(request.getNumeroDecreto());
        docente.setFechaDecreto(request.getFechaDecreto());
        docente.setEscalafon(request.getEscalafon());
        docente.setEstado(request.getEstado());
        docente.setFechaVinculacion(request.getFechaVinculacion());

        // Guarda el docente en la base de datos.
        Docente docenteGuardado = docenteRepository.save(docente);

        return convertirAResponse(docenteGuardado);
    }

    // Consulta un docente por su identificador.
    @Transactional(readOnly = true)
    public DocenteResponse consultarDocente(UUID id) {

        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));

        return convertirAResponse(docente);
    }

    // Actualiza los datos básicos e institucionales de un docente existente.
    @Transactional
    public DocenteResponse actualizarDocente(
            UUID id,
            ActualizarDocenteRequest request) {

        // Verifica que el docente exista.
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));

        // Evita asignar un documento que pertenezca a otro docente.
        validarDocumentoDuplicadoAlActualizar(
                request.getNumeroDocumento(), id);

        // Valida que el área de nombramiento exista.
        Area area = obtenerArea(request.getAreaNombramientoId());

        // Actualiza los datos permitidos en esta HU.
        docente.setNombres(request.getNombres());
        docente.setApellidos(request.getApellidos());
        docente.setTipoDocumento(request.getTipoDocumento());
        docente.setNumeroDocumento(request.getNumeroDocumento());
        docente.setTelefono(request.getTelefono());
        docente.setCorreoInstitucional(request.getCorreoInstitucional());
        docente.setTipoVinculacion(request.getTipoVinculacion());
        docente.setAreaNombramiento(area);
        docente.setNumeroDecreto(request.getNumeroDecreto());
        docente.setFechaDecreto(request.getFechaDecreto());
        docente.setEscalafon(request.getEscalafon());
        docente.setFechaVinculacion(request.getFechaVinculacion());

        // Guarda los cambios realizados.
        Docente docenteActualizado = docenteRepository.save(docente);

        return convertirAResponse(docenteActualizado);
    }
}
