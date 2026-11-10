package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de las aulas institucionales.
public interface AulaRepository extends JpaRepository<Aula, UUID> {

    // Consulta todas las aulas pertenecientes a una sede.
    List<Aula> findBySede_Id(UUID sedeId);

    // Verifica si existe un aula activa con el mismo nombre
    // dentro de la misma sede.
    boolean existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
            UUID sedeId,
            String nombre);

    // Verifica si existe otra aula activa con el mismo nombre
    // dentro de la misma sede, excluyendo el registro actual.
    boolean existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
            UUID sedeId,
            String nombre,
            UUID id);
}
