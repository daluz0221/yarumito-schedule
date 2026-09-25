package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

// Permite consultar y verificar registros de Asignatura en la base de datos.
public interface AsignaturaRepository extends JpaRepository<Asignatura, UUID>,
        JpaSpecificationExecutor<Asignatura> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);
}
