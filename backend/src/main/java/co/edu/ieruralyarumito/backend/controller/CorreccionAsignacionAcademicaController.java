package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.service.CorreccionAsignacionAcademicaService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

// H-04: operaciones protegidas de corrección y retiro.
// No modifica los endpoints actuales de creación y confirmación.
@RestController
@RequestMapping("/api/asignaciones")
public class CorreccionAsignacionAcademicaController {

    private final CorreccionAsignacionAcademicaService servicio;

    public CorreccionAsignacionAcademicaController(
            CorreccionAsignacionAcademicaService servicio) {

        this.servicio = servicio;
    }

    // Actualiza únicamente una asignación en BORRADOR.
    @PutMapping("/{id}")
    public ResponseEntity<AsignacionAcademicaResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarAsignacionAcademicaRequest request,
            Principal principal) {

        AsignacionAcademicaResponse response =
                servicio.actualizar(
                        id, request, principal.getName());

        return ResponseEntity.ok(response);
    }

    // Retira una asignación en BORRADOR sin clases asociadas.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> retirar(
            @PathVariable UUID id,
            Principal principal) {

        servicio.retirar(id, principal.getName());

        return ResponseEntity.noContent().build();
    }
}
