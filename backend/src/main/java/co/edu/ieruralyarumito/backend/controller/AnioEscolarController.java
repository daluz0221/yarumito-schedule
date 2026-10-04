package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.dto.AnioEscolarResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.service.AnioEscolarService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de años escolares.
@RestController
@RequestMapping("/api/v1/anios-escolares")
public class AnioEscolarController {

    private final AnioEscolarService anioEscolarService;

    // Inyección de dependencias mediante constructor.
    public AnioEscolarController(AnioEscolarService anioEscolarService) {
        this.anioEscolarService = anioEscolarService;
    }

    // Registra un nuevo año escolar.
    @PostMapping
    public ResponseEntity<AnioEscolarResponse> registrarAnioEscolar(
            @Valid @RequestBody CrearAnioEscolarRequest request) {

        AnioEscolarResponse response =
                anioEscolarService.registrarAnioEscolar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista los años escolares con paginación.
    @GetMapping
    public ResponseEntity<Page<AnioEscolarResponse>> listarAniosEscolares(
            Pageable pageable) {

        Page<AnioEscolarResponse> response =
                anioEscolarService.listarAniosEscolares(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un año escolar por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<AnioEscolarResponse> consultarAnioEscolar(
            @PathVariable UUID id) {

        AnioEscolarResponse response =
                anioEscolarService.consultarAnioEscolar(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza un año escolar existente.
    @PutMapping("/{id}")
    public ResponseEntity<AnioEscolarResponse> actualizarAnioEscolar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarAnioEscolarRequest request) {

        AnioEscolarResponse response =
                anioEscolarService.actualizarAnioEscolar(id, request);

        return ResponseEntity.ok(response);
    }
}
