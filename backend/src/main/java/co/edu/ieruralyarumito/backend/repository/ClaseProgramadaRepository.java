package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClaseProgramadaRepository
        extends JpaRepository<ClaseProgramada, UUID> {

    // Verifica si un horario tiene al menos una clase programada.
    boolean existsByHorarioId_Id(UUID horarioId);

    // Lista las clases de un grupo dentro de un horario.
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

    // H-07: recupera las clases del docente en el año escolar.
    // Incluye todos sus grupos y sedes.
    // El servicio deberá excluir las versiones históricas
    // antes de calcular su carga semanal vigente.
    @Query("""
            select clase
              from ClaseProgramada clase
              join fetch clase.horarioId horario
              join fetch clase.asignacionAcademicaId asignacion
              join fetch asignacion.docente
              join fetch clase.franjaHorariaId
             where horario.anioEscolarId.id = :anioEscolarId
               and asignacion.docente.id = :docenteId
            """)
    List<ClaseProgramada> findByDocenteAndAnioEscolar(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("docenteId") UUID docenteId);

    // Verifica si el docente está ocupado.
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

    // Verifica ocupación docente excluyendo la clase editada.
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

    // Verifica si el aula está ocupada.
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

    // Verifica ocupación del aula excluyendo la clase editada.
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

    // H-01: evita programar dos asignaturas del mismo grupo
    // en el mismo día y franja dentro del horario.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.id = :horarioId
               and clase.asignacionAcademicaId.grupo.id = :grupoId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
            """)
    boolean existeGrupoOcupado(
            @Param("horarioId") UUID horarioId,
            @Param("grupoId") UUID grupoId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId);

    // H-01: permite mover una clase sin detectarse
    // a sí misma como conflicto.
    @Query("""
            select count(clase) > 0
              from ClaseProgramada clase
             where clase.horarioId.id = :horarioId
               and clase.asignacionAcademicaId.grupo.id = :grupoId
               and clase.dia = :dia
               and clase.franjaHorariaId.id = :franjaHorariaId
               and clase.id <> :claseId
            """)
    boolean existeGrupoOcupadoExcluyendo(
            @Param("horarioId") UUID horarioId,
            @Param("grupoId") UUID grupoId,
            @Param("dia") DiasSemana dia,
            @Param("franjaHorariaId") UUID franjaHorariaId,
            @Param("claseId") UUID claseId);
}
