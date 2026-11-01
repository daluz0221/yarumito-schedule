package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Gestiona la persistencia de las sedes institucionales.
public interface SedeRepository extends JpaRepository<Sede, UUID> {

    // Verifica si ya existe una sede con el mismo código.
    boolean existsByCodigo(String codigo);

    // Verifica duplicidad de código excluyendo la sede que se está actualizando.
    boolean existsByCodigoAndIdNot(String codigo, UUID id);

    // Verifica si ya existe una sede marcada como principal.
    boolean existsByEsPrincipalTrue();

    // Verifica si existe otra sede marcada como principal.
    boolean existsByEsPrincipalTrueAndIdNot(UUID id);
}

