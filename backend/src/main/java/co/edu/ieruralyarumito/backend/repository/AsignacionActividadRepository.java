package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.AsignacionActividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de las actividades asignadas a los docentes.
public interface AsignacionActividadRepository
        extends JpaRepository<AsignacionActividad, UUID> {

    // Lista las actividades compensatorias asignadas a un docente.
    // La entidad nombra la relación como "docenteId".
    List<AsignacionActividad> findByDocenteId_Id(UUID docenteId);
}
