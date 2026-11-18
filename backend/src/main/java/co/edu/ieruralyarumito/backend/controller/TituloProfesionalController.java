package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.service.TituloProfesionalService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Expone los endpoints REST para la gestión
// de títulos profesionales de los docentes.
@RestController
@RequestMapping("/api/v1")
public class TituloProfesionalController {

    private final TituloProfesionalService tituloProfesionalService;

    public TituloProfesionalController(
            TituloProfesionalService tituloProfesionalService) {

        this.tituloProfesionalService = tituloProfesionalService;
    }

    // Registra un nuevo título profesional para un docente.
    @PostMapping("/titulos-profesionales")
    public ResponseEntity<TituloProfesionalResponse> registrarTituloProfesional(
            @Valid @RequestBody CrearTituloProfesionalRequest request) {

        TituloProfesionalResponse response =
                tituloProfesionalService.registrarTituloProfesional(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Consulta un título profesional por su identificador.
    // Incluye títulos retirados.
    @GetMapping("/titulos-profesionales/{id}")
    public ResponseEntity<TituloProfesionalResponse> consultarTituloProfesional(
            @PathVariable UUID id) {

        TituloProfesionalResponse response =
                tituloProfesionalService.consultarTituloProfesional(id);

        return ResponseEntity.ok(response);
    }

    // Conserva la consulta original de todos los títulos
    // de un docente, incluyendo activos y retirados.
    @GetMapping("/docentes/{docenteId}/titulos-profesionales")
    public ResponseEntity<List<TituloProfesionalResponse>> listarTitulosPorDocente(
            @PathVariable UUID docenteId) {

        List<TituloProfesionalResponse> response =
                tituloProfesionalService.listarTitulosPorDocente(docenteId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta únicamente los títulos activos.
    @GetMapping("/docentes/{docenteId}/titulos-profesionales/activos")
    public ResponseEntity<List<TituloProfesionalResponse>>
    listarTitulosActivosPorDocente(
            @PathVariable UUID docenteId) {

        List<TituloProfesionalResponse> response =
                tituloProfesionalService.listarTitulosActivosPorDocente(
                        docenteId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta el histórico de títulos retirados.
    @GetMapping("/docentes/{docenteId}/titulos-profesionales/retirados")
    public ResponseEntity<List<TituloProfesionalResponse>>
    listarTitulosRetiradosPorDocente(
            @PathVariable UUID docenteId) {

        List<TituloProfesionalResponse> response =
                tituloProfesionalService.listarTitulosRetiradosPorDocente(
                        docenteId);

        return ResponseEntity.ok(response);
    }

    // Actualiza los datos permitidos de un título activo.
    @PutMapping("/titulos-profesionales/{id}")
    public ResponseEntity<TituloProfesionalResponse> actualizarTituloProfesional(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarTituloProfesionalRequest request) {

        TituloProfesionalResponse response =
                tituloProfesionalService.actualizarTituloProfesional(
                        id,
                        request);

        return ResponseEntity.ok(response);
    }

    // H-04: retira un título profesional sin eliminar
    // su registro ni las idoneidades relacionadas.
    @PatchMapping("/titulos-profesionales/{id}/retirar")
    public ResponseEntity<TituloProfesionalResponse> retirarTituloProfesional(
            @PathVariable UUID id) {

        TituloProfesionalResponse response =
                tituloProfesionalService.retirarTituloProfesional(id);

        return ResponseEntity.ok(response);
    }

    // H-04: reactiva un título profesional retirado.
    @PatchMapping("/titulos-profesionales/{id}/reactivar")
    public ResponseEntity<TituloProfesionalResponse> reactivarTituloProfesional(
            @PathVariable UUID id) {

        TituloProfesionalResponse response =
                tituloProfesionalService.reactivarTituloProfesional(id);

        return ResponseEntity.ok(response);
    }
}
