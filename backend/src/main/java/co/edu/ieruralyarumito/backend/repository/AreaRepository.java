package co.edu.ieruralyarumito.backend.repository;

// Importa la entidad Area y las herramientas necesarias de Spring Data JPA.
import co.edu.ieruralyarumito.backend.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

// Permite realizar operaciones de persistencia y consultas dinámicas sobre Area.
public interface AreaRepository extends JpaRepository<Area, UUID>,
        JpaSpecificationExecutor<Area> {

    // Verifica si ya existe un área con el código indicado.
    boolean existsByCodigo(String codigo);

    // Verifica duplicidad de código excluyendo el área que se está actualizando.
    boolean existsByCodigoAndIdNot(String codigo, UUID id);
}
