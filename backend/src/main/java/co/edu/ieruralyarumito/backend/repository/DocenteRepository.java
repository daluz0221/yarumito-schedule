package co.edu.ieruralyarumito.backend.repository;

// Importa la entidad Docente y las herramientas necesarias de Spring Data JPA.
import co.edu.ieruralyarumito.backend.entity.Docente; // Entidad Docente
import org.springframework.data.jpa.repository.JpaRepository; // Repositorio base de Spring Data JPA

// Importa UUID para identificar los docentes.
import java.util.UUID; // Tipo del identificador

// Permite realizar operaciones de persistencia sobre Docente.
public interface DocenteRepository extends JpaRepository<Docente, UUID> {
}



