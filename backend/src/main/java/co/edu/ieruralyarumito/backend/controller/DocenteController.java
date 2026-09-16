package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.service.DocenteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.ieruralyarumito.backend.dto.CrearDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.DocenteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import co.edu.ieruralyarumito.backend.dto.ActualizarDocenteRequest;
import org.springframework.web.bind.annotation.PutMapping;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

// Expone los endpoints REST para la gestión de docentes.

@RestController
@RequestMapping("/api/v1/docentes")
public class DocenteController {

    // Servicio que contiene la lógica de negocio de docentes.
    private final DocenteService docenteService;

    // Inyección de dependencias mediante constructor.
    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    // Registra un nuevo docente.
    @PostMapping
    public ResponseEntity<DocenteResponse> registrarDocente(
            @Valid @RequestBody CrearDocenteRequest request) {

        DocenteResponse response = docenteService.registrarDocente(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista docentes con búsqueda, filtros y paginación.
    @GetMapping
    public ResponseEntity<Page<DocenteResponse>> listarDocentes(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) EstadoDocente estado,
            @RequestParam(required = false) TipoVinculacion tipoVinculacion,
            Pageable pageable) {

        Page<DocenteResponse> response = docenteService.listarDocentes(
                texto,
                areaId,
                estado,
                tipoVinculacion,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    // Consulta un docente por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<DocenteResponse> consultarDocente(
            @PathVariable UUID id) {

        DocenteResponse response = docenteService.consultarDocente(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza los datos básicos e institucionales de un docente.
    @PutMapping("/{id}")
    public ResponseEntity<DocenteResponse> actualizarDocente(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarDocenteRequest request) {

        DocenteResponse response = docenteService.actualizarDocente(id, request);

        return ResponseEntity.ok(response);
    }

    // TODO SCRUM-8:
    // Habilitar PATCH /{id}/estado cuando esté implementada la validación de
    // responsabilidades vigentes: asignaciones académicas, dirección de grupo
    // y actividades institucionales.
}
