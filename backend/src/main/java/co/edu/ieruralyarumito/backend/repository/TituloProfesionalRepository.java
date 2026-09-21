package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Permite consultar títulos profesionales registrados.
public interface TituloProfesionalRepository extends JpaRepository<TituloProfesional, UUID> {

}