package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Permite consultar y persistir registros históricos de auditoría.
public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, UUID> {

    // Consulta el historial de auditoría asociado a una entidad específica.
    List<RegistroAuditoria> findByEntidadAndIdEntidadOrderByFechaDesc(
            String entidad,
            UUID idEntidad);

    // Consulta el historial generado por un usuario.
    List<RegistroAuditoria> findByUsuario_IdOrderByFechaDesc(
            UUID usuarioId);
}
