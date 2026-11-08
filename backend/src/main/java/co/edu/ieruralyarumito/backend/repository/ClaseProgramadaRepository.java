package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
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
              join fetch clase.franjaHorariaId
              join fetch clase.aulaId
             where clase.horarioId.id = :horarioId
               and asignacion.grupo.id = :grupoId
            """)
    List<ClaseProgramada> findByHorarioAndGrupo(
            @Param("horarioId") UUID horarioId,
            @Param("grupoId") UUID grupoId);
}
