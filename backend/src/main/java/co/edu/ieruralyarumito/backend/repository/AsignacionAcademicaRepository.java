package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de las asignaciones académicas.
public interface AsignacionAcademicaRepository
        extends JpaRepository<AsignacionAcademica, UUID> {

    // Lista las asignaciones que se encuentran en un estado determinado.
    List<AsignacionAcademica> findByEstado(
            EstadoAsignacionAcademica estado);

    // Lista las asignaciones de un docente.
    List<AsignacionAcademica> findByDocente_Id(
            UUID docenteId);

    // Lista las asignaciones de un grupo.
    List<AsignacionAcademica> findByGrupo_Id(
            UUID grupoId);

    // Lista las asignaciones de un grupo dentro de un año escolar.
    List<AsignacionAcademica> findByAnioEscolar_IdAndGrupo_Id(
            UUID anioEscolarId,
            UUID grupoId);

    // Verifica si ya existe una asignación para el mismo año escolar,
    // grupo y asignatura.
    boolean existsByAnioEscolar_IdAndGrupo_IdAndAsignatura_Id(
            UUID anioEscolarId,
            UUID grupoId,
            UUID asignaturaId);
}
