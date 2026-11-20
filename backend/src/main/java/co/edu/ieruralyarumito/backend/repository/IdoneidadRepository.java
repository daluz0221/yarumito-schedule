package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Idoneidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Permite consultar y administrar registros de idoneidad.
public interface IdoneidadRepository
        extends JpaRepository<Idoneidad, UUID> {

    // Consulta las idoneidades asociadas a un docente.
    List<Idoneidad> findByDocente_Id(UUID docenteId);

    // Verifica si existen idoneidades asociadas
    // a una asignatura.
    boolean existsByAsignatura_Id(UUID asignaturaId);

    // H-04: verifica si un título profesional respalda
    // idoneidades cuya vigencia no ha finalizado.
    //
    // También protege las idoneidades con inicio futuro,
    // ya que su periodo de vigencia sigue pendiente.
    @Query("""
            SELECT CASE
                       WHEN COUNT(i) > 0 THEN true
                       ELSE false
                   END
            FROM Idoneidad i
            WHERE i.tituloSoporte.id = :tituloId
              AND (
                    i.vigenteHasta IS NULL
                    OR i.vigenteHasta >= :fechaReferencia
                  )
            """)
    boolean existeIdoneidadVigentePorTitulo(
            @Param("tituloId") UUID tituloId,
            @Param("fechaReferencia") LocalDate fechaReferencia);
}
