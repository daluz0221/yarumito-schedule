package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarGradoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGradoRequest;
import co.edu.ieruralyarumito.backend.dto.GradoResponse;
import co.edu.ieruralyarumito.backend.service.GradoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de grados escolares.
@RestController
@RequestMapping("/api/v1/grados")
public class GradoController {

    private final GradoService gradoService;

    // Inyección de dependencias mediante constructor.
    public GradoController(GradoService gradoService) {
        this.gradoService = gradoService;
    }

    // Registra un nuevo grado escolar.
    @PostMapping
    public ResponseEntity<GradoResponse> registrarGrado(
            @Valid @RequestBody CrearGradoRequest request) {

        GradoResponse response =
                gradoService.registrarGrado(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista los grados registrados con paginación.
    @GetMapping
    public ResponseEntity<Page<GradoResponse>> listarGrados(
            Pageable pageable) {

        Page<GradoResponse> response =
                gradoService.listarGrados(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un grado por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<GradoResponse> consultarGrado(
            @PathVariable UUID id) {

        GradoResponse response =
                gradoService.consultarGrado(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza un grado existente.
    @PutMapping("/{id}")
    public ResponseEntity<GradoResponse> actualizarGrado(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarGradoRequest request) {

        GradoResponse response =
                gradoService.actualizarGrado(id, request);

        return ResponseEntity.ok(response);
    }
}
