package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Gestiona las restricciones de disponibilidad
// de los docentes.
public interface RestriccionDocenteRepository
        extends JpaRepository<RestriccionDocente, UUID> {

    // H-04: busca únicamente la condición ACTIVA
    // de un docente para un año, día y franja.
    // Las restricciones retiradas no participan
    // en las validaciones de horarios.
    @Query("""
            SELECT r
            FROM RestriccionDocente r
            WHERE r.docente.id = :docenteId
              AND r.anioEscolar.id = :anioEscolarId
              AND r.dia = :dia
              AND r.franjaHoraria.id = :franjaHorariaId
              AND r.activo = true
            """)
    Optional<RestriccionDocente>
    findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
            @Param("docenteId") UUID docenteId,
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("dia") DiaSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId);

    // Verifica duplicados incluyendo registros activos
    // y retirados. De esta manera se evita crear otro
    // registro con la misma combinación.
    //
    // Para recuperar una restricción retirada
    // debe utilizarse la operación de reactivación.
    boolean existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
            UUID docenteId,
            UUID anioEscolarId,
            DiaSemana dia,
            UUID franjaHorariaId);

    // H-04: lista únicamente las restricciones activas
    // de un docente para un año escolar.
    @Query("""
            SELECT r
            FROM RestriccionDocente r
            WHERE r.docente.id = :docenteId
              AND r.anioEscolar.id = :anioEscolarId
              AND r.activo = true
            """)
    List<RestriccionDocente>
    findByDocente_IdAndAnioEscolar_Id(
            @Param("docenteId") UUID docenteId,
            @Param("anioEscolarId") UUID anioEscolarId);

    // H-04: lista únicamente las restricciones activas
    // registradas para un año escolar.
    @Query("""
            SELECT r
            FROM RestriccionDocente r
            WHERE r.anioEscolar.id = :anioEscolarId
              AND r.activo = true
            """)
    List<RestriccionDocente>
    findByAnioEscolar_Id(
            @Param("anioEscolarId") UUID anioEscolarId);

    // H-04: consulta las restricciones retiradas
    // de un docente para un año escolar.
    List<RestriccionDocente>
    findByDocente_IdAndAnioEscolar_IdAndActivoFalse(
            UUID docenteId,
            UUID anioEscolarId);

    // H-04: consulta las restricciones retiradas
    // registradas para un año escolar.
    List<RestriccionDocente>
    findByAnioEscolar_IdAndActivoFalse(
            UUID anioEscolarId);

    // H-04: consulta histórica completa de un docente.
    // Incluye restricciones activas y retiradas.
    List<RestriccionDocente>
    findAllByDocente_IdAndAnioEscolar_Id(
            UUID docenteId,
            UUID anioEscolarId);

    // H-04: consulta histórica completa de un año escolar.
    List<RestriccionDocente>
    findAllByAnioEscolar_Id(
            UUID anioEscolarId);
}
