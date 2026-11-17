package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.entity.RegistroAuditoria;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.repository.RegistroAuditoriaRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Gestiona la trazabilidad histórica de cambios relevantes del sistema.
@Service
public class RegistroAuditoriaService {

    public static final String ACCION_CREATE = "CREATE";
    public static final String ACCION_UPDATE = "UPDATE";
    public static final String ACCION_DELETE = "DELETE";
    public static final String ACCION_PUBLICAR = "PUBLICAR";

    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final ObjectMapper objectMapper;

    public RegistroAuditoriaService(
            RegistroAuditoriaRepository registroAuditoriaRepository,
            ObjectMapper objectMapper) {

        this.registroAuditoriaRepository =
                registroAuditoriaRepository;

        this.objectMapper =
                objectMapper;
    }

    // Registra una acción exitosa sobre una entidad.
    @Transactional
    public RegistroAuditoria registrar(
            Usuario usuario,
            String accion,
            String entidad,
            UUID idEntidad,
            Object datosAntes,
            Object datosDespues,
            String ip) {

        validarDatosObligatorios(
                usuario,
                accion,
                entidad,
                idEntidad);

        RegistroAuditoria registro =
                new RegistroAuditoria();

        registro.setUsuario(usuario);
        registro.setAccion(accion.trim());
        registro.setEntidad(entidad.trim());
        registro.setIdEntidad(idEntidad);

        registro.setDatosAntes(
                convertirAJson(datosAntes));

        registro.setDatosDespues(
                convertirAJson(datosDespues));

        registro.setFecha(
                LocalDateTime.now());

        registro.setIp(
                normalizarIp(ip));

        return registroAuditoriaRepository.save(registro);
    }

    // Consulta el historial de una entidad específica.
    @Transactional(readOnly = true)
    public List<RegistroAuditoria> consultarHistorialEntidad(
            String entidad,
            UUID idEntidad) {

        if (entidad == null || entidad.isBlank()) {
            throw new IllegalArgumentException(
                    "La entidad es obligatoria");
        }

        if (idEntidad == null) {
            throw new IllegalArgumentException(
                    "El identificador de la entidad es obligatorio");
        }

        return registroAuditoriaRepository
                .findByEntidadAndIdEntidadOrderByFechaDesc(
                        entidad.trim(),
                        idEntidad);
    }

    // Consulta las acciones registradas para un usuario.
    @Transactional(readOnly = true)
    public List<RegistroAuditoria> consultarHistorialUsuario(
            UUID usuarioId) {

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio");
        }

        return registroAuditoriaRepository
                .findByUsuario_IdOrderByFechaDesc(
                        usuarioId);
    }

    // Verifica los datos mínimos exigidos por el modelo.
    private void validarDatosObligatorios(
            Usuario usuario,
            String accion,
            String entidad,
            UUID idEntidad) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario autenticado es obligatorio para registrar la auditoría");
        }

        if (accion == null || accion.isBlank()) {
            throw new IllegalArgumentException(
                    "La acción de auditoría es obligatoria");
        }

        if (entidad == null || entidad.isBlank()) {
            throw new IllegalArgumentException(
                    "La entidad auditada es obligatoria");
        }

        if (idEntidad == null) {
            throw new IllegalArgumentException(
                    "El identificador de la entidad auditada es obligatorio");
        }

        validarAccion(accion);
    }

    // Mantiene un conjunto técnico controlado de acciones de auditoría.
    private void validarAccion(
            String accion) {

        String accionNormalizada =
                accion.trim();

        if (!ACCION_CREATE.equals(accionNormalizada)
                && !ACCION_UPDATE.equals(accionNormalizada)
                && !ACCION_DELETE.equals(accionNormalizada)
                && !ACCION_PUBLICAR.equals(accionNormalizada)) {

            throw new IllegalArgumentException(
                    "La acción de auditoría no es válida");
        }
    }

    // Convierte el estado de una entidad a JSON.
    // Un estado inexistente se conserva como null.
    private String convertirAJson(
            Object datos) {

        if (datos == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(datos);

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "No fue posible serializar los datos de auditoría",
                    ex);
        }
    }

    // La IP es opcional según el modelo.
    private String normalizarIp(
            String ip) {

        if (ip == null || ip.isBlank()) {
            return null;
        }

        return ip.trim();
    }
}
