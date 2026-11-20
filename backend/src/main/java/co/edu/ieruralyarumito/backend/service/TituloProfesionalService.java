package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class TituloProfesionalService {

    private final TituloProfesionalRepository tituloProfesionalRepository;
    private final DocenteRepository docenteRepository;
    private final IdoneidadRepository idoneidadRepository;

    // H-16: auditoría institucional.
    private UsuarioRepository usuarioRepository;
    private RegistroAuditoriaService registroAuditoriaService;

    public TituloProfesionalService(
            TituloProfesionalRepository tituloProfesionalRepository,
            DocenteRepository docenteRepository,
            IdoneidadRepository idoneidadRepository) {

        this.tituloProfesionalRepository = tituloProfesionalRepository;
        this.docenteRepository = docenteRepository;
        this.idoneidadRepository = idoneidadRepository;
    }

    @Autowired
    public void configurarAuditoria(
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.registroAuditoriaService =
                Objects.requireNonNull(registroAuditoriaService);
    }

    private Docente obtenerDocente(UUID docenteId) {

        return docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El docente no existe"));
    }

    private TituloProfesional obtenerTituloProfesional(UUID id) {

        return tituloProfesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El título profesional no existe"));
    }

    private void validarTituloActivo(TituloProfesional titulo) {

        if (!titulo.isActivo()) {
            throw new TransicionEstadoNoPermitidaException(
                    "El título profesional se encuentra retirado. "
                            + "Debe reactivarlo primero");
        }
    }

    // H-04: protege las idoneidades vigentes.
    private void validarIdoneidadesVigentes(UUID tituloId) {

        boolean existenIdoneidadesVigentes =
                idoneidadRepository.existeIdoneidadVigentePorTitulo(
                        tituloId, LocalDate.now());

        if (existenIdoneidadesVigentes) {
            throw new TransicionEstadoNoPermitidaException(
                    "No se puede retirar el título profesional "
                            + "porque respalda idoneidades vigentes. "
                            + "Primero debe finalizar la vigencia "
                            + "de las idoneidades relacionadas");
        }
    }

    private TituloProfesionalResponse convertirAResponse(
            TituloProfesional titulo) {

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(titulo.getId());
        response.setDocenteId(titulo.getDocente().getId());
        response.setNivel(titulo.getNivel());
        response.setNombreTitulo(titulo.getNombreTitulo());
        response.setInstitucion(titulo.getInstitucion());
        response.setAnioGraduacion(titulo.getAnioGraduacion());
        response.setArchivoSoporte(titulo.getArchivoSoporte());
        response.setActivo(titulo.isActivo());

        return response;
    }

    @Transactional
    public TituloProfesionalResponse registrarTituloProfesional(
            CrearTituloProfesionalRequest request) {

        Docente docente = obtenerDocente(request.getDocenteId());

        TituloProfesional titulo = new TituloProfesional();

        titulo.setDocente(docente);
        titulo.setNivel(request.getNivel());
        titulo.setNombreTitulo(request.getNombreTitulo());
        titulo.setInstitucion(request.getInstitucion());
        titulo.setAnioGraduacion(request.getAnioGraduacion());
        titulo.setArchivoSoporte(request.getArchivoSoporte());
        titulo.setActivo(true);

        TituloProfesional guardado =
                tituloProfesionalRepository.save(titulo);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardado,
                    null,
                    datosTitulo(guardado));
        }

        return convertirAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public TituloProfesionalResponse consultarTituloProfesional(UUID id) {

        return convertirAResponse(obtenerTituloProfesional(id));
    }

    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository.findByDocente_Id(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosActivosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_IdAndActivoTrue(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosRetiradosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_IdAndActivoFalse(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional
    public TituloProfesionalResponse actualizarTituloProfesional(
            UUID id,
            ActualizarTituloProfesionalRequest request) {

        TituloProfesional titulo = obtenerTituloProfesional(id);

        validarTituloActivo(titulo);

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosTitulo(titulo);

        titulo.setNivel(request.getNivel());
        titulo.setNombreTitulo(request.getNombreTitulo());
        titulo.setInstitucion(request.getInstitucion());
        titulo.setAnioGraduacion(request.getAnioGraduacion());
        titulo.setArchivoSoporte(request.getArchivoSoporte());

        TituloProfesional actualizado =
                tituloProfesionalRepository.save(titulo);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizado,
                    antes,
                    datosTitulo(actualizado));
        }

        return convertirAResponse(actualizado);
    }

    // H-04: retiro lógico sin eliminación física.
    @Transactional
    public TituloProfesionalResponse retirarTituloProfesional(UUID id) {

        TituloProfesional titulo = obtenerTituloProfesional(id);

        validarTituloActivo(titulo);
        validarIdoneidadesVigentes(id);

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosTitulo(titulo);

        titulo.setActivo(false);

        TituloProfesional retirado =
                tituloProfesionalRepository.save(titulo);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    retirado,
                    antes,
                    datosTitulo(retirado));
        }

        return convertirAResponse(retirado);
    }

    // H-04: reactivación del registro histórico.
    @Transactional
    public TituloProfesionalResponse reactivarTituloProfesional(UUID id) {

        TituloProfesional titulo = obtenerTituloProfesional(id);

        if (titulo.isActivo()) {
            throw new TransicionEstadoNoPermitidaException(
                    "El título profesional ya se encuentra activo");
        }

        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null : datosTitulo(titulo);

        titulo.setActivo(true);

        TituloProfesional reactivado =
                tituloProfesionalRepository.save(titulo);

        if (registroAuditoriaService != null) {
            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    reactivado,
                    antes,
                    datosTitulo(reactivado));
        }

        return convertirAResponse(reactivado);
    }

    // H-16: datos para trazabilidad.
    // No almacenar archivos binarios en el historial.
    private Map<String, Object> datosTitulo(TituloProfesional titulo) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", titulo.getId());
        datos.put("docenteId", titulo.getDocente().getId());
        datos.put("nivel", titulo.getNivel());
        datos.put("nombreTitulo", titulo.getNombreTitulo());
        datos.put("institucion", titulo.getInstitucion());
        datos.put("anioGraduacion", titulo.getAnioGraduacion());

        datos.put("archivoSoportePresente",
                titulo.getArchivoSoporte() != null
                        && !titulo.getArchivoSoporte().isBlank());

        datos.put("activo", titulo.isActivo());

        return datos;
    }

    // H-16: registrar dentro de la misma transacción.
    private void auditar(
            String accion,
            TituloProfesional titulo,
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
                "TituloProfesional",
                titulo.getId(),
                antes,
                despues,
                null);
    }
}
