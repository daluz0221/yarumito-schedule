package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.AsignacionActividadResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionActividadRequest;
import co.edu.ieruralyarumito.backend.service.AsignacionActividadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Expone las actividades compensatorias asignadas a un docente.
@RestController
@RequestMapping("/api/docentes/{docenteId}/actividades")
public class AsignacionActividadController {

    private final AsignacionActividadService asignacionActividadService;

    public AsignacionActividadController(
            AsignacionActividadService asignacionActividadService) {

        this.asignacionActividadService = asignacionActividadService;
    }

    // Asigna una actividad institucional existente al docente indicado.
    @PostMapping
    public ResponseEntity<AsignacionActividadResponse> asignarActividad(
            @PathVariable UUID docenteId,
            @Valid @RequestBody CrearAsignacionActividadRequest request) {

        AsignacionActividadResponse response =
                asignacionActividadService
                        .asignarActividad(docenteId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista las actividades asignadas al docente indicado.
    @GetMapping
    public ResponseEntity<List<AsignacionActividadResponse>> listarActividades(
            @PathVariable UUID docenteId) {

        List<AsignacionActividadResponse> response =
                asignacionActividadService
                        .listarPorDocente(docenteId);

        return ResponseEntity.ok(response);
    }
}
