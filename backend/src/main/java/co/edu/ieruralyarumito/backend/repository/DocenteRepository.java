package co.edu.ieruralyarumito.backend.repository;

// Importa la entidad Docente y las herramientas necesarias de Spring Data JPA.
import co.edu.ieruralyarumito.backend.entity.Docente; // Entidad Docente
import org.springframework.data.jpa.repository.JpaRepository; // Repositorio base de Spring Data JPA
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Permite consultas dinámicas con filtros

// Importa UUID para identificar los docentes.
import java.util.UUID; // Tipo del identificador

// Permite realizar operaciones de persistencia sobre Docente.
public interface DocenteRepository extends JpaRepository<Docente, UUID>,
        JpaSpecificationExecutor<Docente> {

    // Verifica si ya existe un docente con el número de documento indicado.
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Verifica duplicidad de documento excluyendo al docente que se está actualizando.
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, UUID id);
}
