package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.TituloProfesional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Gestiona la persistencia de los títulos profesionales
// registrados para los docentes.
public interface TituloProfesionalRepository
        extends JpaRepository<TituloProfesional, UUID> {

    // Consulta todos los títulos profesionales de un docente.
    // Incluye títulos activos y retirados para conservar
    // la compatibilidad con los informes históricos.
    List<TituloProfesional> findByDocente_Id(
            UUID docenteId);

    // H-04: consulta únicamente los títulos activos
    // de un docente.
    List<TituloProfesional> findByDocente_IdAndActivoTrue(
            UUID docenteId);

    // H-04: consulta los títulos retirados
    // sin perder su información histórica.
    List<TituloProfesional> findByDocente_IdAndActivoFalse(
            UUID docenteId);
}
