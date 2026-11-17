package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.ActividadInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Gestiona la persistencia de las actividades institucionales.
public interface ActividadInstitucionalRepository
        extends JpaRepository<ActividadInstitucional, UUID> {
}
