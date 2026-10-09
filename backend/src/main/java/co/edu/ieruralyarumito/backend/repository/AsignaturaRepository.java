package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Asignatura;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Permite consultar y verificar registros de Asignatura en la base de datos.
public interface AsignaturaRepository extends JpaRepository<Asignatura, UUID> {

    boolean existsByCodigo(String codigo);

    @Query("""
            select asignatura
            from Asignatura asignatura
            join fetch asignatura.area
            where asignatura.area.id = :areaId
            order by asignatura.nombre asc
            """)
    List<Asignatura> findByAreaIdOrderByNombreAsc(@Param("areaId") UUID areaId);
}
