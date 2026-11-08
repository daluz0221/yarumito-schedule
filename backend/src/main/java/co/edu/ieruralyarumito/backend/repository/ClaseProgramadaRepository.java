package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de las clases ubicadas en la rejilla.
public interface ClaseProgramadaRepository
        extends JpaRepository<ClaseProgramada, UUID> {

    // Lista las clases de un grupo dentro de un horario.
    // La entidad nombra las relaciones como "horarioId" y "asignacionAcademicaId".
    // Se traen las asociaciones usadas por el tablero para evitar consultas N+1.
    @Query("""
            select clase
              from ClaseProgramada clase
              join fetch clase.asignacionAcademicaId asignacion
              join fetch asignacion.asignatura
              join fetch asignacion.docente
              join fetch asignacion.grupo
              join fetch clase.franjaHorariaId
              join fetch clase.aulaId
             where clase.horarioId.id = :horarioId
               and asignacion.grupo.id = :grupoId
            """)
    List<ClaseProgramada> findByHorarioAndGrupo(
            @Param("horarioId") UUID horarioId,
            @Param("grupoId") UUID grupoId);

    // Verifica si el docente ya tiene otra clase en el mismo día y franja
    // dentro del año escolar. Se usa al crear una clase.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.anioEscolarId.id = :anioEscolarId
               and clase.asignacionAcademicaId.docente.id = :docenteId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
            """)
    boolean existeDocenteOcupado(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("docenteId") UUID docenteId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId);

    // Verifica el conflicto de docente al editar, excluyendo la clase actual.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.anioEscolarId.id = :anioEscolarId
               and clase.asignacionAcademicaId.docente.id = :docenteId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
               and clase.id <> :claseId
            """)
    boolean existeDocenteOcupadoExcluyendo(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("docenteId") UUID docenteId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId,
            @Param("claseId") UUID claseId);

    // Verifica si el aula ya está ocupada en el mismo día y franja
    // dentro del horario. Se usa al crear una clase.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.id = :horarioId
               and clase.aulaId.id = :aulaId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
            """)
    boolean existeAulaOcupada(
            @Param("horarioId") UUID horarioId,
            @Param("aulaId") UUID aulaId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId);

    // Verifica el conflicto de aula al editar, excluyendo la clase actual.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.id = :horarioId
               and clase.aulaId.id = :aulaId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
               and clase.id <> :claseId
            """)
    boolean existeAulaOcupadaExcluyendo(
            @Param("horarioId") UUID horarioId,
            @Param("aulaId") UUID aulaId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId,
            @Param("claseId") UUID claseId);
}
