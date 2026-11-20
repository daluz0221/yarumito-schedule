package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.PlanEstudios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Gestiona la persistencia de los registros del plan de estudios.
public interface PlanEstudiosRepository
        extends JpaRepository<PlanEstudios, UUID> {

    // Verifica si ya existe la misma asignatura
    // para el mismo grado dentro del mismo año escolar.
    // Incluye registros activos y retirados para evitar duplicados.
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

    // Conserva el método utilizado previamente
    // por otros componentes del backend.
    Optional<List<PlanEstudios>> findByAnioEscolarAndGrado(
            UUID anioEscolar,
            UUID grado);

    // H-04: retorna únicamente las asignaturas activas
    // del plan de estudios de un grado y año escolar.
    //
    // Conserva la firma original para mantener compatibles
    // los servicios de Horario y Asignación Académica.
    @Query("""
            SELECT p
            FROM PlanEstudios p
            WHERE p.anioEscolar.id = :anioEscolarId
              AND p.grado.id = :gradoId
              AND p.activo = true
            """)
    List<PlanEstudios> findByAnioEscolar_IdAndGrado_Id(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("gradoId") UUID gradoId);

    // H-04: permite consultar únicamente
    // los registros activos del plan de estudios.
    Page<PlanEstudios> findByActivoTrue(
            Pageable pageable);

    // H-04: permite consultar los registros retirados
    // sin perder su información histórica.
    Page<PlanEstudios> findByActivoFalse(
            Pageable pageable);
}
