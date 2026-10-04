package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAulaRequest;
import co.edu.ieruralyarumito.backend.dto.AulaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAulaRequest;
import co.edu.ieruralyarumito.backend.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(
            AulaService aulaService) {

        this.aulaService = aulaService;
    }

    @PostMapping
    public ResponseEntity<AulaResponse> registrarAula(
            @Valid @RequestBody CrearAulaRequest request) {

        AulaResponse response =
                aulaService.registrarAula(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaResponse> consultarAula(
            @PathVariable UUID id) {

        AulaResponse response =
                aulaService.consultarAula(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AulaResponse>> listarPorSede(
            @RequestParam UUID sedeId) {

        List<AulaResponse> response =
                aulaService.listarPorSede(sedeId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AulaResponse> actualizarAula(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarAulaRequest request) {

        AulaResponse response =
                aulaService.actualizarAula(
                        id,
                        request);

        return ResponseEntity.ok(response);
    }

    // Registra una nueva versión histórica del aula.
    // El aula anterior queda inactiva y se crea una nueva activa.
    @PostMapping("/{id}/nueva-version")
    public ResponseEntity<AulaResponse> registrarCambioReal(
            @PathVariable UUID id,
            @Valid @RequestBody CrearAulaRequest request) {

        AulaResponse response =
                aulaService.registrarCambioReal(
                        id,
                        request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Solicita la eliminación física de un aula.
    //
    // La eliminación está sujeta a las reglas de negocio del servicio.
    // Mientras el modelo no permita demostrar que el aula fue creada
    // por error y nunca utilizada, la operación se rechaza de forma
    // conservadora para preservar el historial académico.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAula(
            @PathVariable UUID id) {

        aulaService.eliminarAula(id);

        return ResponseEntity.noContent()
                .build();
    }
}
