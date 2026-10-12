package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Gestiona la persistencia de los registros del plan de estudios.
public interface PlanEstudiosRepository
        extends JpaRepository<PlanEstudios, UUID> {

    // Verifica si ya existe la misma asignatura
    // para el mismo grado dentro del mismo año escolar.
    boolean existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
            UUID anioEscolarId,
            UUID gradoId,
            UUID asignaturaId);

    // Verifica duplicidad al actualizar,
    // excluyendo el registro actual.
    boolean existsByAnioEscolar_IdAndGrado_IdAndAsignatura_IdAndIdNot(
            UUID anioEscolarId,
            UUID gradoId,
            UUID asignaturaId,
            UUID id);
}
