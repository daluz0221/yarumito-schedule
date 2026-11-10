package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.TurnoResponse;
import co.edu.ieruralyarumito.backend.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de turnos institucionales.
@RestController
@RequestMapping("/api/v1/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    // Inyección de dependencias mediante constructor.
    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    // Registra un nuevo turno institucional.
    @PostMapping
    public ResponseEntity<TurnoResponse> registrarTurno(
            @Valid @RequestBody CrearTurnoRequest request) {

        TurnoResponse response =
                turnoService.registrarTurno(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista los turnos registrados con paginación.
    @GetMapping
    public ResponseEntity<Page<TurnoResponse>> listarTurnos(
            Pageable pageable) {

        Page<TurnoResponse> response =
                turnoService.listarTurnos(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un turno por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> consultarTurno(
            @PathVariable UUID id) {

        TurnoResponse response =
                turnoService.consultarTurno(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza un turno existente.
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponse> actualizarTurno(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarTurnoRequest request) {

        TurnoResponse response =
                turnoService.actualizarTurno(id, request);

        return ResponseEntity.ok(response);
    }
}
