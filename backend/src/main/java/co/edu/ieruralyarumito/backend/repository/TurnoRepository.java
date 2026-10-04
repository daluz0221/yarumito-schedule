package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TurnoRepository extends JpaRepository<Turno, UUID> {

    boolean existsByNombreIgnoreCase(String nombre);
}
