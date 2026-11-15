package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.entity.RegistroAuditoria;
import co.edu.ieruralyarumito.backend.service.RegistroAuditoriaService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * HU-14 - Control y Auditoria.
 *
 * Expone consultas HTTP del historial de auditoria.
 *
 * No crea, modifica ni elimina registros.
 * Reutiliza RegistroAuditoriaService.
 *
 * Acceso institucional restringido al rector.
 */
@RestController
@RequestMapping("/api/v1/registros-auditoria")
public class RegistroAuditoriaController {

    private final RegistroAuditoriaService auditoriaService;

    public RegistroAuditoriaController(
            RegistroAuditoriaService auditoriaService) {

        this.auditoriaService = auditoriaService;
    }

    /**
     * Consulta las acciones registradas sobre una entidad.
     *
     * GET /api/v1/registros-auditoria/entidad/{entidad}/{idEntidad}
     */
    @GetMapping("/entidad/{entidad}/{idEntidad}")
    public List<RegistroAuditoriaResponse> consultarPorEntidad(
            @PathVariable String entidad,
            @PathVariable UUID idEntidad,
            Authentication authentication) {

        exigirRector(authentication);

        return auditoriaService
                .consultarHistorialEntidad(entidad, idEntidad)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Consulta las acciones realizadas por un usuario.
     *
     * GET /api/v1/registros-auditoria/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<RegistroAuditoriaResponse> consultarPorUsuario(
            @PathVariable UUID usuarioId,
            Authentication authentication) {

        exigirRector(authentication);

        return auditoriaService
                .consultarHistorialUsuario(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Evita exponer directamente las entidades JPA
     * y la informacion completa del usuario.
     */
    private RegistroAuditoriaResponse convertirAResponse(
            RegistroAuditoria registro) {

        return new RegistroAuditoriaResponse(
                registro.getId(),
                registro.getUsuario().getId(),
                registro.getAccion(),
                registro.getEntidad(),
                registro.getIdEntidad(),
                registro.getDatosAntes(),
                registro.getDatosDespues(),
                registro.getFecha(),
                registro.getIp()
        );
    }

    /**
     * Control de acceso institucional.
     */
    private void exigirRector(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere autenticacion."
            );
        }

        boolean esRector = authentication
                .getAuthorities()
                .stream()
                .anyMatch(autoridad -> {

                    String nombre = autoridad.getAuthority();

                    return "RECTOR".equals(nombre)
                            || "ROLE_RECTOR".equals(nombre);
                });

        if (!esRector) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo del rector."
            );
        }
    }

    /**
     * DTO de respuesta para consultas de auditoria.
     */
    public record RegistroAuditoriaResponse(
            UUID id,
            UUID usuarioId,
            String accion,
            String entidad,
            UUID idEntidad,
            String datosAntes,
            String datosDespues,
            LocalDateTime fecha,
            String ip
    ) {
    }
}
