package co.edu.ieruralyarumito.backend.repository;

// Importa la entidad Area y las herramientas necesarias de Spring Data JPA.
import co.edu.ieruralyarumito.backend.entity.Area; // Entidad Area
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Tipo del identificador
import org.springframework.data.jpa.repository.JpaRepository; // Repositorio base de Spring Data JPA

// Permite consultar y verificar registros de Area en la base de datos.
public interface AreaRepository extends JpaRepository<Area, UUID> {

    Optional<Area> findByCodigo(String codigo);

    List<Area> findByActivaTrueOrderByNombreAsc();
}
