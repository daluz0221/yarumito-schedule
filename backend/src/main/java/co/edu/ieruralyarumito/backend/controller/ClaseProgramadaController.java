package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.service.HorarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

// Expone las acciones de programación de bloques en la rejilla.
@RestController
@RequestMapping("/api/clases-programadas")
public class ClaseProgramadaController {

    private final HorarioService horarioService;

    public ClaseProgramadaController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    // Ubica una asignación académica en la rejilla.
    @PostMapping
    public ResponseEntity<ClaseProgramadaResponse> crearClaseProgramada(
            @Valid @RequestBody CrearClaseProgramadaRequest request,
            Principal principal) {

        ClaseProgramadaResponse response =
                horarioService.crearClaseProgramada(
                        request,
                        principal.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Mueve un bloque existente sin alterar su asignación académica.
    @PutMapping("/{id}")
    public ResponseEntity<ClaseProgramadaResponse> actualizarClaseProgramada(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarClaseProgramadaRequest request) {

        return ResponseEntity.ok(
                horarioService.actualizarClaseProgramada(id, request));
    }

    // Quita un bloque del horario. Su hora vuelve a quedar pendiente.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarClaseProgramada(
            @PathVariable UUID id) {

        horarioService.eliminarClaseProgramada(id);

        return ResponseEntity.noContent().build();
    }
}
