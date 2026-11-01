package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarSedeRequest;
import co.edu.ieruralyarumito.backend.dto.CrearSedeRequest;
import co.edu.ieruralyarumito.backend.dto.SedeResponse;
import co.edu.ieruralyarumito.backend.service.SedeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de sedes.
@RestController
@RequestMapping("/api/v1/sedes")
public class SedeController {

    private final SedeService sedeService;

    // Inyección de dependencias mediante constructor.
    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    // Registra una nueva sede.
    @PostMapping
    public ResponseEntity<SedeResponse> registrarSede(
            @Valid @RequestBody CrearSedeRequest request) {

        SedeResponse response =
                sedeService.registrarSede(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista las sedes con paginación.
    @GetMapping
    public ResponseEntity<Page<SedeResponse>> listarSedes(
            Pageable pageable) {

        Page<SedeResponse> response =
                sedeService.listarSedes(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta una sede por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<SedeResponse> consultarSede(
            @PathVariable UUID id) {

        SedeResponse response =
                sedeService.consultarSede(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza una sede existente.
    @PutMapping("/{id}")
    public ResponseEntity<SedeResponse> actualizarSede(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarSedeRequest request) {

        SedeResponse response =
                sedeService.actualizarSede(id, request);

        return ResponseEntity.ok(response);
    }
}
