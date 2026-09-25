package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignaturaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignaturaRequest;
import co.edu.ieruralyarumito.backend.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @PostMapping
    public ResponseEntity<AsignaturaResponse> registrarAsignatura(
            @Valid @RequestBody CrearAsignaturaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(asignaturaService.registrarAsignatura(request));
    }

    @GetMapping
    public Page<AsignaturaResponse> listarAsignaturas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) UUID areaId,
            Pageable pageable) {

        return asignaturaService.listarAsignaturas(
                texto,
                activa,
                areaId,
                pageable);
    }

    @GetMapping("/{id}")
    public AsignaturaResponse consultarAsignatura(
            @PathVariable UUID id) {

        return asignaturaService.consultarAsignatura(id);
    }

    @PutMapping("/{id}")
    public AsignaturaResponse actualizarAsignatura(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarAsignaturaRequest request) {

        return asignaturaService.actualizarAsignatura(id, request);
    }

    @PatchMapping("/{id}/estado")
    public AsignaturaResponse cambiarEstado(
            @PathVariable UUID id,
            @RequestParam boolean activa) {

        return asignaturaService.cambiarEstado(id, activa);
    }
}
