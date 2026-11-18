package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.CrearPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.PlanEstudiosResponse;
import co.edu.ieruralyarumito.backend.service.PlanEstudiosService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión del plan de estudios.
@RestController
@RequestMapping("/api/v1/planes-estudio")
public class PlanEstudiosController {

    private final PlanEstudiosService planEstudiosService;

    public PlanEstudiosController(
            PlanEstudiosService planEstudiosService) {

        this.planEstudiosService = planEstudiosService;
    }

    // Registra una asignatura dentro del plan de estudios.
    @PostMapping
    public ResponseEntity<PlanEstudiosResponse> registrarPlanEstudios(
            @Valid @RequestBody CrearPlanEstudiosRequest request) {

        PlanEstudiosResponse response =
                planEstudiosService.registrarPlanEstudios(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista los registros activos del plan de estudios.
    @GetMapping
    public ResponseEntity<Page<PlanEstudiosResponse>> listarPlanEstudios(
            Pageable pageable) {

        Page<PlanEstudiosResponse> response =
                planEstudiosService.listarPlanEstudios(pageable);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta el histórico de asignaturas retiradas.
    @GetMapping("/retirados")
    public ResponseEntity<Page<PlanEstudiosResponse>> listarPlanesRetirados(
            Pageable pageable) {

        Page<PlanEstudiosResponse> response =
                planEstudiosService.listarPlanesRetirados(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un registro por su identificador,
    // incluyendo los registros retirados.
    @GetMapping("/{id}")
    public ResponseEntity<PlanEstudiosResponse> consultarPlanEstudios(
            @PathVariable UUID id) {

        PlanEstudiosResponse response =
                planEstudiosService.consultarPlanEstudios(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza un registro activo del plan de estudios.
    @PutMapping("/{id}")
    public ResponseEntity<PlanEstudiosResponse> actualizarPlanEstudios(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarPlanEstudiosRequest request) {

        PlanEstudiosResponse response =
                planEstudiosService.actualizarPlanEstudios(
                        id,
                        request);

        return ResponseEntity.ok(response);
    }

    // H-04: retira una asignatura del plan de estudios
    // sin eliminar su información histórica.
    @PatchMapping("/{id}/retirar")
    public ResponseEntity<PlanEstudiosResponse> retirarPlanEstudios(
            @PathVariable UUID id) {

        PlanEstudiosResponse response =
                planEstudiosService.retirarPlanEstudios(id);

        return ResponseEntity.ok(response);
    }

    // H-04: reactiva una asignatura anteriormente retirada.
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<PlanEstudiosResponse> reactivarPlanEstudios(
            @PathVariable UUID id) {

        PlanEstudiosResponse response =
                planEstudiosService.reactivarPlanEstudios(id);

        return ResponseEntity.ok(response);
    }
}
