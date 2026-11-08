package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Gestiona la persistencia de los horarios institucionales.
public interface HorarioRepository
        extends JpaRepository<Horario, UUID> {

    // Obtiene la versión más reciente del horario para un año escolar y sede.
    // La entidad nombra las relaciones como "anioEscolarId" y "sedeId".
    Optional<Horario> findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
            UUID anioEscolarId,
            UUID sedeId);
}
