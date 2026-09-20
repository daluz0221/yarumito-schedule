package co.edu.ieruralyarumito.backend.repository;

// Importa la entidad Asignatura.
import co.edu.ieruralyarumito.backend.entity.Asignatura;

// Importa el repositorio base de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa UUID, que es el tipo del identificador de Asignatura.
import java.util.UUID;

// Permite consultar y verificar registros de Asignatura en la base de datos.
public interface AsignaturaRepository extends JpaRepository<Asignatura, UUID> {

}

