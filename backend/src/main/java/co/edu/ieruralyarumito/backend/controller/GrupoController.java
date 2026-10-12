package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.GrupoResponse;
import co.edu.ieruralyarumito.backend.service.GrupoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Expone los endpoints REST para la gestión de grupos escolares.
@RestController
@RequestMapping("/api/v1/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    // Inyección de dependencias mediante constructor.
    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    // Registra un nuevo grupo escolar.
    @PostMapping
    public ResponseEntity<GrupoResponse> registrarGrupo(
            @Valid @RequestBody CrearGrupoRequest request) {

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Lista los grupos registrados con paginación.
    @GetMapping
    public ResponseEntity<Page<GrupoResponse>> listarGrupos(
            Pageable pageable) {

        Page<GrupoResponse> response =
                grupoService.listarGrupos(pageable);

        return ResponseEntity.ok(response);
    }

    // Consulta un grupo por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponse> consultarGrupo(
            @PathVariable UUID id) {

        GrupoResponse response =
                grupoService.consultarGrupo(id);

        return ResponseEntity.ok(response);
    }

    // Actualiza un grupo existente.
    @PutMapping("/{id}")
    public ResponseEntity<GrupoResponse> actualizarGrupo(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarGrupoRequest request) {

        GrupoResponse response =
                grupoService.actualizarGrupo(id, request);

        return ResponseEntity.ok(response);
    }
}
