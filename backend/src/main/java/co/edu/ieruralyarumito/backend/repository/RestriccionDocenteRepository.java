package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Permite gestionar las restricciones de disponibilidad de los docentes.
public interface RestriccionDocenteRepository
        extends JpaRepository<RestriccionDocente, UUID> {

    // Busca la condición efectiva de un docente
    // para un año, día y franja específicos.
    Optional<RestriccionDocente>
    findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
            UUID docenteId,
            UUID anioEscolarId,
            DiaSemana dia,
            UUID franjaHorariaId);

    // Verifica si ya existe una condición para
    // la misma combinación docente-año-día-franja.
    boolean existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
            UUID docenteId,
            UUID anioEscolarId,
            DiaSemana dia,
            UUID franjaHorariaId);

    // Lista todas las restricciones de un docente
    // para un año escolar.
    List<RestriccionDocente>
    findByDocente_IdAndAnioEscolar_Id(
            UUID docenteId,
            UUID anioEscolarId);

    // Lista las restricciones registradas para un año escolar.
    List<RestriccionDocente>
    findByAnioEscolar_Id(
            UUID anioEscolarId);
}
