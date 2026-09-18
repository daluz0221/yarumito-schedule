package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

// Permite consultar y administrar registros de idoneidad.
public interface IdoneidadRepository extends JpaRepository<Idoneidad, UUID> {

    // Consulta las idoneidades asociadas a un docente.
    List<Idoneidad> findByDocente_Id(UUID docenteId);
}