package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;
import co.edu.ieruralyarumito.backend.service.RestriccionDocenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Expone los endpoints REST para la gestión de restricciones docentes.
@RestController
@RequestMapping("/api/v1/restricciones-docentes")
public class RestriccionDocenteController {

    private final RestriccionDocenteService restriccionDocenteService;

    // Inyección de dependencias mediante constructor.
    public RestriccionDocenteController(
            RestriccionDocenteService restriccionDocenteService) {

        this.restriccionDocenteService =
                restriccionDocenteService;
    }

    // Registra una nueva restricción docente.
    @PostMapping
    public ResponseEntity<RestriccionDocenteResponse> registrarRestriccion(
            @Valid @RequestBody CrearRestriccionDocenteRequest request) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.registrarRestriccion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Consulta una restricción por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<RestriccionDocenteResponse> consultarRestriccion(
            @PathVariable UUID id) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.consultarRestriccion(id);

        return ResponseEntity.ok(response);
    }

    // Lista las restricciones de un docente para un año escolar.
    @GetMapping("/docente/{docenteId}/anio/{anioEscolarId}")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarPorDocenteYAnio(
            @PathVariable UUID docenteId,
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarPorDocenteYAnio(
                        docenteId,
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // Lista todas las restricciones de un año escolar.
    @GetMapping("/anio/{anioEscolarId}")
    public ResponseEntity<List<RestriccionDocenteResponse>> listarPorAnio(
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarPorAnio(anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // Actualiza la condición de una restricción existente.
    @PutMapping("/{id}")
    public ResponseEntity<RestriccionDocenteResponse> actualizarRestriccion(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarRestriccionDocenteRequest request) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.actualizarRestriccion(
                        id,
                        request);

        return ResponseEntity.ok(response);
    }
}
