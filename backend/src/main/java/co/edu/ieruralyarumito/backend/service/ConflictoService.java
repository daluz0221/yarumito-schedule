package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.entity.Conflicto;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.ConflictoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Materializa y consulta los conflictos detectados por el Gestor de Horarios.
// Este servicio no vuelve a ejecutar las reglas de detección del horario.
@Service
public class ConflictoService {

    private final ConflictoRepository conflictoRepository;

    public ConflictoService(
            ConflictoRepository conflictoRepository) {

        this.conflictoRepository =
                conflictoRepository;
    }

    // Registra el resultado de una validación realizada por el Gestor.
    @Transactional
    public ConflictoResponse registrarConflicto(
            Horario horario,
            TipoConflicto tipo,
            String mensaje) {

        if (horario == null) {
            throw new IllegalArgumentException(
                    "El horario es obligatorio");
        }

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo de conflicto es obligatorio");
        }

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del conflicto es obligatorio");
        }

        Conflicto conflicto =
                new Conflicto();

        conflicto.setHorario(horario);
        conflicto.setTipo(tipo);
        conflicto.setSeveridad(
                determinarSeveridad(tipo));
        conflicto.setMensaje(
                mensaje.trim());
        conflicto.setResuelto(false);
        conflicto.setFechaDeteccion(
                LocalDateTime.now());

        Conflicto guardado =
                conflictoRepository.save(conflicto);

        return convertirAResponse(guardado);
    }

    // Lista todo el historial de conflictos de un horario.
    @Transactional(readOnly = true)
    public List<ConflictoResponse> listarPorHorario(
            UUID horarioId) {

        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El horario es obligatorio");
        }

        return conflictoRepository
                .findByHorario_IdOrderByFechaDeteccionDesc(
                        horarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Lista solamente los conflictos que continúan pendientes.
    @Transactional(readOnly = true)
    public List<ConflictoResponse> listarPendientes(
            UUID horarioId) {

        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El horario es obligatorio");
        }

        return conflictoRepository
                .findByHorario_IdAndResueltoOrderByFechaDeteccionDesc(
                        horarioId,
                        false)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Lista los conflictos de un horario según su severidad.
    @Transactional(readOnly = true)
    public List<ConflictoResponse> listarPorSeveridad(
            UUID horarioId,
            SeveridadConflicto severidad) {

        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El horario es obligatorio");
        }

        if (severidad == null) {
            throw new IllegalArgumentException(
                    "La severidad es obligatoria");
        }

        return conflictoRepository
                .findByHorario_IdAndSeveridadOrderByFechaDeteccionDesc(
                        horarioId,
                        severidad)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Marca como resuelto un conflicto previamente materializado.
    @Transactional
    public ConflictoResponse marcarResuelto(
            UUID conflictoId) {

        Conflicto conflicto =
                obtenerConflicto(conflictoId);

        conflicto.setResuelto(true);

        Conflicto actualizado =
                conflictoRepository.save(conflicto);

        return convertirAResponse(actualizado);
    }

    private Conflicto obtenerConflicto(
            UUID conflictoId) {

        return conflictoRepository
                .findById(conflictoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El conflicto no existe"));
    }

    // La severidad se deriva del tipo de resultado detectado.
    private SeveridadConflicto determinarSeveridad(
            TipoConflicto tipo) {

        return switch (tipo) {

            case DOCENTE_OCUPADO,
                 AULA_OCUPADA ->
                    SeveridadConflicto.ERROR;

            case CAPACIDAD_AULA ->
                    SeveridadConflicto.ADVERTENCIA;
        };
    }

    private ConflictoResponse convertirAResponse(
            Conflicto conflicto) {

        ConflictoResponse response =
                new ConflictoResponse();

        response.setId(
                conflicto.getId());

        response.setHorarioId(
                conflicto.getHorario().getId());

        response.setTipo(
                conflicto.getTipo());

        response.setSeveridad(
                conflicto.getSeveridad());

        response.setMensaje(
                conflicto.getMensaje());

        response.setResuelto(
                conflicto.isResuelto());

        response.setFechaDeteccion(
                conflicto.getFechaDeteccion());

        return response;
    }
}