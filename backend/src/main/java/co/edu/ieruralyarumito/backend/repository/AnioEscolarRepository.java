package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Gestiona la persistencia de los años escolares.
public interface AnioEscolarRepository extends JpaRepository<AnioEscolar, UUID> {

    // Verifica si ya existe un año lectivo con el mismo número.
    boolean existsByAnio(int anio);

    // Verifica duplicidad del año excluyendo el registro que se está actualizando.
    boolean existsByAnioAndIdNot(int anio, UUID id);

    // Verifica si ya existe algún año escolar marcado como actual.
    boolean existsByEsActualTrue();

    // Verifica si existe otro año escolar marcado como actual.
    boolean existsByEsActualTrueAndIdNot(UUID id);
}
