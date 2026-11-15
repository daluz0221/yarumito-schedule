package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Conflicto;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de los conflictos
// materializados asociados a un horario.
public interface ConflictoRepository
        extends JpaRepository<Conflicto, UUID> {

    List<Conflicto> findByHorario_IdOrderByFechaDeteccionDesc(
            UUID horarioId);

    List<Conflicto> findByHorario_IdAndResueltoOrderByFechaDeteccionDesc(
            UUID horarioId,
            boolean resuelto);

    List<Conflicto> findByHorario_IdAndSeveridadOrderByFechaDeteccionDesc(
            UUID horarioId,
            SeveridadConflicto severidad);
}
