package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.service.AsignacionAcademicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

// Expone los endpoints REST de la asignación académica.
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionAcademicaController {

    private final AsignacionAcademicaService asignacionAcademicaService;

    public AsignacionAcademicaController(
            AsignacionAcademicaService asignacionAcademicaService) {

        this.asignacionAcademicaService = asignacionAcademicaService;
    }

    // Registra una nueva asignación académica en estado BORRADOR.
    @PostMapping
    public ResponseEntity<AsignacionAcademicaResponse> crearAsignacion(
            @Valid @RequestBody CrearAsignacionAcademicaRequest request,
            Principal principal) {

        AsignacionAcademicaResponse response =
                asignacionAcademicaService
                        .crearAsignacionAcademica(
                                request,
                                principal.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista las asignaciones en estado BORRADOR.
    @GetMapping("/borradores")
    public ResponseEntity<List<AsignacionAcademicaResponse>> listarBorradores() {

        List<AsignacionAcademicaResponse> response =
                asignacionAcademicaService.listarBorradores();

        return ResponseEntity.ok(response);
    }

    // Consulta una asignación académica por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<AsignacionAcademicaResponse> consultarAsignacion(
            @PathVariable UUID id) {

        AsignacionAcademicaResponse response =
                asignacionAcademicaService
                        .consultarAsignacionAcademica(id);

        return ResponseEntity.ok(response);
    }

    // Lista las asignaciones de un docente.
    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<AsignacionAcademicaResponse>> listarPorDocente(
            @PathVariable UUID docenteId) {

        List<AsignacionAcademicaResponse> response =
                asignacionAcademicaService
                        .listarPorDocente(docenteId);

        return ResponseEntity.ok(response);
    }

    // Lista las asignaciones de un grupo.
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<AsignacionAcademicaResponse>> listarPorGrupo(
            @PathVariable UUID grupoId) {

        List<AsignacionAcademicaResponse> response =
                asignacionAcademicaService
                        .listarPorGrupo(grupoId);

        return ResponseEntity.ok(response);
    }

    // Confirma una asignación que se encuentra en estado BORRADOR.
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<AsignacionAcademicaResponse> confirmarAsignacion(
            @PathVariable UUID id) {

        AsignacionAcademicaResponse response =
                asignacionAcademicaService
                        .confirmarAsignacionAcademica(id);

        return ResponseEntity.ok(response);
    }
}
