package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Grado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Acceso a persistencia para la entidad Grado.
public interface GradoRepository extends JpaRepository<Grado, UUID> {
}
