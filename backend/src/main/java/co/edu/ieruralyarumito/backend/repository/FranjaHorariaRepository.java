package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Permite consultar y administrar franjas horarias.
public interface FranjaHorariaRepository
        extends JpaRepository<FranjaHoraria, UUID> {

    // Consulta todas las franjas pertenecientes a un turno
    // ordenadas por el orden visual.
    // Se utiliza, entre otras cosas, para validar solapamientos temporales.
    List<FranjaHoraria> findByTurno_IdOrderByOrdenAsc(
            UUID turnoId);

    // Verifica si ya existe un número académico dentro del turno.
    boolean existsByTurno_IdAndNumero(
            UUID turnoId,
            int numero);

    // Verifica si ya existe un orden visual dentro del turno.
    boolean existsByTurno_IdAndOrden(
            UUID turnoId,
            int orden);

    // Verifica duplicidad de número al actualizar,
    // excluyendo la franja actual.
    boolean existsByTurno_IdAndNumeroAndIdNot(
            UUID turnoId,
            int numero,
            UUID id);

    // Verifica duplicidad de orden al actualizar,
    // excluyendo la franja actual.
    boolean existsByTurno_IdAndOrdenAndIdNot(
            UUID turnoId,
            int orden,
            UUID id);
}
