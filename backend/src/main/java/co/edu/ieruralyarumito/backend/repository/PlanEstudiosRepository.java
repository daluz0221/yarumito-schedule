package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

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

    Optional<List<PlanEstudios>> findByAnioEscolarAndGrado(UUID anioEscolar, UUID grado);

    // Lista los planes de estudio de un grado dentro de un año escolar.
    // Permite resolver el turno con el que se construye la rejilla.
    List<PlanEstudios> findByAnioEscolar_IdAndGrado_Id(
            UUID anioEscolarId,
            UUID gradoId);

}
