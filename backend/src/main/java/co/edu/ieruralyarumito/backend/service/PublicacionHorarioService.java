package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.PublicarHorarioResponse;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Publica un horario en BORRADOR después de validar que pueda salir
// a producción. Reutiliza la consulta de conflictos y la auditoría
// existentes para no duplicar reglas.
@Service
public class PublicacionHorarioService {

    private final HorarioRepository horarioRepository;
    private final ClaseProgramadaRepository claseProgramadaRepository;
    private final ConflictoService conflictoService;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAuditoriaService registroAuditoriaService;

    public PublicacionHorarioService(
            HorarioRepository horarioRepository,
            ClaseProgramadaRepository claseProgramadaRepository,
            ConflictoService conflictoService,
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.horarioRepository = horarioRepository;
        this.claseProgramadaRepository = claseProgramadaRepository;
        this.conflictoService = conflictoService;
        this.usuarioRepository = usuarioRepository;
        this.registroAuditoriaService = registroAuditoriaService;
    }

    // H-10: BORRADOR -> PUBLICADO.
    @Transactional
    public PublicarHorarioResponse publicar(
            UUID horarioId,
            String correoUsuario) {

        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El identificador del horario es obligatorio");
        }

        Horario horario =
                horarioRepository
                        .findById(horarioId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El horario no existe"));

        EstadoHorario estadoActual =
                horario.getEstado();

        if (estadoActual != EstadoHorario.BORRADOR) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo se puede publicar un horario en estado "
                            + "BORRADOR. Estado actual: "
                            + (estadoActual == null
                            ? "DESCONOCIDO"
                            : estadoActual.name()));
        }

        if (!claseProgramadaRepository
                .existsByHorarioId_Id(horarioId)) {

            throw new RelacionAcademicaInvalidaException(
                    "El horario no tiene clases programadas; "
                            + "no es posible publicarlo");
        }

        // Misma fuente que GET /api/v1/conflictos/horario/{id}/pendientes.
        // Solo los ERROR bloquean; ADVERTENCIA no impide publicar.
        List<ConflictoResponse> pendientes =
                conflictoService.listarPendientes(horarioId);

        List<String> errores =
                pendientes.stream()
                        .filter(conflicto ->
                                conflicto.getSeveridad()
                                        == SeveridadConflicto.ERROR)
                        .map(ConflictoResponse::getMensaje)
                        .distinct()
                        .toList();

        if (!errores.isEmpty()) {
            String detalle = errores.size() == 1
                    ? "1 conflicto bloqueante pendiente"
                    : errores.size() + " conflictos bloqueantes pendientes";

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede publicar el horario: existen "
                            + detalle + ": "
                            + String.join("; ", errores));
        }

        Map<String, Object> datosAntes =
                datosHorario(horario);

        horario.setEstado(EstadoHorario.PUBLICADO);
        horario.setFechaPublicacion(LocalDateTime.now());

        Horario guardado =
                horarioRepository.save(horario);

        registrarAuditoria(
                guardado,
                correoUsuario,
                datosAntes);

        return convertirAResponse(guardado);
    }

    private void registrarAuditoria(
            Horario horario,
            String correoUsuario,
            Map<String, Object> datosAntes) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(
                                resolverCorreo(correoUsuario))
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no identificado "
                                                + "para auditar la publicación"));

        registroAuditoriaService.registrar(
                usuario,
                RegistroAuditoriaService.ACCION_PUBLICAR,
                "Horario",
                horario.getId(),
                datosAntes,
                datosHorario(horario),
                null);
    }

    private String resolverCorreo(
            String correoUsuario) {

        if (correoUsuario != null
                && !correoUsuario.isBlank()) {

            return correoUsuario;
        }

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        return auth.getName();
    }

    private Map<String, Object> datosHorario(
            Horario horario) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        datos.put("horarioId", horario.getId());
        datos.put(
                "estado",
                horario.getEstado() == null
                        ? null
                        : horario.getEstado().name());
        datos.put(
                "fechaPublicacion",
                horario.getFechaPublicacion() == null
                        ? null
                        : horario.getFechaPublicacion().toString());
        datos.put("version", horario.getVersion());

        return datos;
    }

    private PublicarHorarioResponse convertirAResponse(
            Horario horario) {

        PublicarHorarioResponse response =
                new PublicarHorarioResponse();

        response.setHorarioId(horario.getId());
        response.setEstado(horario.getEstado());
        response.setFechaPublicacion(
                horario.getFechaPublicacion());
        response.setVersion(horario.getVersion());

        return response;
    }
}
