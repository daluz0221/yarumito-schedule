package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAreaRequest;
import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAreaRequest;
import co.edu.ieruralyarumito.backend.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de áreas.
@RestController
@RequestMapping("/api/v1/areas")
public class AreaController {

    private final AreaService areaService;

    // Inyección de dependencias mediante constructor.
    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    // Registra una nueva área.
    @PostMapping
    public ResponseEntity<AreaResponse> registrarArea(
            @Valid @RequestBody CrearAreaRequest request) {

        AreaResponse response = areaService.registrarArea(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista áreas con búsqueda, filtro por estado y paginación.
    @GetMapping
    public ResponseEntity<Page<AreaResponse>> listarAreas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activa,
            Pageable pageable) {

        Page<AreaResponse> response =
                areaService.listarAreas(texto, activa, pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un área por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> consultarArea(
            @PathVariable UUID id) {

        AreaResponse response = areaService.consultarArea(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza los datos de un área existente.
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> actualizarArea(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarAreaRequest request) {

        AreaResponse response =
                areaService.actualizarArea(id, request);

        return ResponseEntity.ok(response);
    }

    // Activa o desactiva un área sin eliminarla físicamente.
    @PatchMapping("/{id}/estado")
    public ResponseEntity<AreaResponse> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam boolean activa) {

        AreaResponse response =
                areaService.cambiarEstado(id, activa);

        return ResponseEntity.ok(response);
    }
}