package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    // La versión más reciente en un estado concreto (borrador de mejora o publicado).
    Optional<Horario> findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
            UUID anioEscolarId,
            UUID sedeId,
            EstadoHorario estado);

    List<Horario> findByAnioEscolarId_IdAndSedeId_IdAndEstado(
            UUID anioEscolarId,
            UUID sedeId,
            EstadoHorario estado);

    List<Horario> findByAnioEscolarId_IdOrderByVersionDesc(
            UUID anioEscolarId);
}
