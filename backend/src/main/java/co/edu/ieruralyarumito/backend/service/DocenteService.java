package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CambiarEstadoDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.DocenteResponse;

import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import co.edu.ieruralyarumito.backend.specification.DocenteSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Gestión institucional de docentes.
// H-16: integración con Control y Auditoría.
@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final AreaRepository areaRepository;

    // H-16: dependencias de auditoría.
    private UsuarioRepository usuarioRepository;
    private RegistroAuditoriaService registroAuditoriaService;

    // Conserva el constructor utilizado por las pruebas existentes.
    public DocenteService(
            DocenteRepository docenteRepository,
            AreaRepository areaRepository) {

        this.docenteRepository = docenteRepository;
        this.areaRepository = areaRepository;
    }

    @Autowired
    public void configurarAuditoria(
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.usuarioRepository =
                Objects.requireNonNull(usuarioRepository);

        this.registroAuditoriaService =
                Objects.requireNonNull(registroAuditoriaService);
    }

    private Area obtenerArea(UUID areaId) {

        return areaRepository.findById(areaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área de nombramiento no existe"));
    }

    private Docente obtenerDocente(UUID id) {

        return docenteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Conserva la validación original de documentos duplicados.
    private void validarDocumentoDuplicado(String numeroDocumento) {

        if (docenteRepository.existsByNumeroDocumento(numeroDocumento)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un docente con ese número de documento");
        }
    }

    private void validarDocumentoDuplicadoAlActualizar(
            String numeroDocumento,
            UUID id) {

        if (docenteRepository.existsByNumeroDocumentoAndIdNot(
                numeroDocumento,
                id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro docente con ese número de documento");
        }
    }

    // Consulta paginada con los filtros existentes.
    @Transactional(readOnly = true)
    public Page<DocenteResponse> listarDocentes(
            String texto,
            UUID areaId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            Pageable pageable) {

        Specification<Docente> specification =
                DocenteSpecification.buscarPorTexto(texto)
                        .and(DocenteSpecification.porArea(areaId))
                        .and(DocenteSpecification.porEstado(estado))
                        .and(DocenteSpecification.porTipoVinculacion(
                                tipoVinculacion));

        return docenteRepository.findAll(specification, pageable)
                .map(this::convertirAResponse);
    }

    // Cambia el estado administrativo.
    @Transactional
    public DocenteResponse cambiarEstado(
            UUID id,
            CambiarEstadoDocenteRequest request) {

        Docente docente = obtenerDocente(id);

        validarTransicionEstado(
                docente.getEstado(),
                request.getNuevoEstado());

        // H-16: conservar el estado anterior.
        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null
                        : datosDocente(docente);

        /*
         * Se conserva la regla institucional:
         *
         * LICENCIA y RETIRADO no requieren eliminar
         * las responsabilidades académicas históricas.
         *
         * La cobertura posterior de asignaciones corresponde
         * al proceso de asignación académica.
         */
        docente.setEstado(request.getNuevoEstado());

        Docente actualizado = docenteRepository.save(docente);

        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizado,
                    antes,
                    datosDocente(actualizado));
        }

        return convertirAResponse(actualizado);
    }

    // Conserva las transiciones de estado permitidas.
    private void validarTransicionEstado(
            EstadoDocente estadoActual,
            EstadoDocente nuevoEstado) {

        boolean transicionPermitida = switch (estadoActual) {

            case ACTIVO ->
                    nuevoEstado == EstadoDocente.LICENCIA
                            || nuevoEstado == EstadoDocente.RETIRADO;

            case LICENCIA ->
                    nuevoEstado == EstadoDocente.ACTIVO
                            || nuevoEstado == EstadoDocente.RETIRADO;

            // RETIRADO requiere nuevo nombramiento.
            case RETIRADO -> false;
        };

        if (!transicionPermitida) {

            throw new TransicionEstadoNoPermitidaException(
                    "No se permite cambiar el estado del docente de "
                            + estadoActual + " a " + nuevoEstado);
        }
    }

    // Conversión a DTO de respuesta.
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

        response.setAreaNombramientoId(
                docente.getAreaNombramiento().getId());

        response.setNumeroDecreto(docente.getNumeroDecreto());
        response.setFechaDecreto(docente.getFechaDecreto());
        response.setEscalafon(docente.getEscalafon());

        response.setHorasSemanalesContratadas(
                docente.getHorasSemanalesContratadas());

        response.setMaxHorasExtra(docente.getMaxHorasExtra());

        response.setEsExclusivoMediaTecnica(
                docente.isEsExclusivoMediaTecnica());

        response.setEstado(docente.getEstado());
        response.setFechaVinculacion(docente.getFechaVinculacion());

        return response;
    }

    // Registra un docente.
    @Transactional
    public DocenteResponse registrarDocente(CrearDocenteRequest request) {

        validarDocumentoDuplicado(request.getNumeroDocumento());

        Area area = obtenerArea(request.getAreaNombramientoId());

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

        // Conserva los valores predeterminados del modelo:
        // 22 horas ordinarias y hasta 10 extraordinarias.
        Docente guardado = docenteRepository.save(docente);

        // H-16: auditoría de creación.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardado,
                    null,
                    datosDocente(guardado));
        }

        return convertirAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public DocenteResponse consultarDocente(UUID id) {

        return convertirAResponse(obtenerDocente(id));
    }

    // Actualiza datos permitidos sin modificar el área
    // de nombramiento mediante esta operación.
    @Transactional
    public DocenteResponse actualizarDocente(
            UUID id,
            ActualizarDocenteRequest request) {

        Docente docente = obtenerDocente(id);

        validarDocumentoDuplicadoAlActualizar(
                request.getNumeroDocumento(),
                id);

        Area area = obtenerArea(request.getAreaNombramientoId());

        if (!docente.getAreaNombramiento().getId()
                .equals(area.getId())) {

            throw new RelacionAcademicaInvalidaException(
                    "El cambio de área de nombramiento requiere "
                            + "un nuevo nombramiento");
        }

        // H-16: instantánea antes de modificar.
        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null
                        : datosDocente(docente);

        docente.setNombres(request.getNombres());
        docente.setApellidos(request.getApellidos());
        docente.setTipoDocumento(request.getTipoDocumento());
        docente.setNumeroDocumento(request.getNumeroDocumento());
        docente.setTelefono(request.getTelefono());
        docente.setCorreoInstitucional(request.getCorreoInstitucional());
        docente.setTipoVinculacion(request.getTipoVinculacion());
        docente.setNumeroDecreto(request.getNumeroDecreto());
        docente.setFechaDecreto(request.getFechaDecreto());
        docente.setEscalafon(request.getEscalafon());
        docente.setFechaVinculacion(request.getFechaVinculacion());

        Docente actualizado = docenteRepository.save(docente);

        // H-16: auditoría de modificación.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizado,
                    antes,
                    datosDocente(actualizado));
        }

        return convertirAResponse(actualizado);
    }

    // H-16: instantánea de los datos institucionales.
    // No serializar directamente la entidad JPA.
    private Map<String, Object> datosDocente(Docente docente) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", docente.getId());
        datos.put("nombres", docente.getNombres());
        datos.put("apellidos", docente.getApellidos());
        datos.put("tipoDocumento", docente.getTipoDocumento());
        datos.put("numeroDocumento", docente.getNumeroDocumento());
        datos.put("telefono", docente.getTelefono());
        datos.put("correoInstitucional",
                docente.getCorreoInstitucional());

        datos.put("tipoVinculacion", docente.getTipoVinculacion());

        datos.put("areaNombramientoId",
                docente.getAreaNombramiento() == null
                        ? null
                        : docente.getAreaNombramiento().getId());

        datos.put("numeroDecreto", docente.getNumeroDecreto());
        datos.put("fechaDecreto", docente.getFechaDecreto());
        datos.put("escalafon", docente.getEscalafon());

        datos.put("horasSemanalesContratadas",
                docente.getHorasSemanalesContratadas());

        datos.put("maxHorasExtra", docente.getMaxHorasExtra());

        datos.put("esExclusivoMediaTecnica",
                docente.isEsExclusivoMediaTecnica());

        datos.put("estado", docente.getEstado());
        datos.put("fechaVinculacion", docente.getFechaVinculacion());

        return datos;
    }

    // H-16: reutiliza HU-14 dentro de la transacción.
    private void auditar(
            String accion,
            Docente docente,
            Object antes,
            Object despues) {

        // Compatibilidad con las pruebas anteriores.
        if (registroAuditoriaService == null) {
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Usuario autenticado no identificado "
                                        + "para auditoría"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "Docente",
                docente.getId(),
                antes,
                despues,
                null);
    }
}
