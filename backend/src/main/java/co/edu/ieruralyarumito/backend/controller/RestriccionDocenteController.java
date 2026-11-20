package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;
import co.edu.ieruralyarumito.backend.service.RestriccionDocenteService;

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
// de restricciones docentes.
@RestController
@RequestMapping("/api/v1/restricciones-docentes")
public class RestriccionDocenteController {

    private final RestriccionDocenteService restriccionDocenteService;

    public RestriccionDocenteController(
            RestriccionDocenteService restriccionDocenteService) {

        this.restriccionDocenteService =
                restriccionDocenteService;
    }

    // Registra una nueva restricción docente.
    @PostMapping
    public ResponseEntity<RestriccionDocenteResponse> registrarRestriccion(
            @Valid @RequestBody CrearRestriccionDocenteRequest request) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.registrarRestriccion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Consulta una restricción por identificador,
    // incluyendo las restricciones retiradas.
    @GetMapping("/{id}")
    public ResponseEntity<RestriccionDocenteResponse> consultarRestriccion(
            @PathVariable UUID id) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.consultarRestriccion(id);

        return ResponseEntity.ok(response);
    }

    // Lista las restricciones activas de un docente
    // para un año escolar.
    @GetMapping("/docente/{docenteId}/anio/{anioEscolarId}")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarPorDocenteYAnio(
            @PathVariable UUID docenteId,
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarPorDocenteYAnio(
                        docenteId,
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // Lista las restricciones activas de un año escolar.
    @GetMapping("/anio/{anioEscolarId}")
    public ResponseEntity<List<RestriccionDocenteResponse>> listarPorAnio(
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarPorAnio(anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta las restricciones retiradas
    // de un docente durante un año escolar.
    @GetMapping("/docente/{docenteId}/anio/{anioEscolarId}/retiradas")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarRetiradasPorDocenteYAnio(
            @PathVariable UUID docenteId,
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarRetiradasPorDocenteYAnio(
                        docenteId,
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta las restricciones retiradas
    // de todos los docentes durante un año escolar.
    @GetMapping("/anio/{anioEscolarId}/retiradas")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarRetiradasPorAnio(
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarRetiradasPorAnio(
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta el histórico completo de un docente.
    // Incluye restricciones activas y retiradas.
    @GetMapping("/docente/{docenteId}/anio/{anioEscolarId}/historico")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarHistoricoPorDocenteYAnio(
            @PathVariable UUID docenteId,
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarHistoricoPorDocenteYAnio(
                        docenteId,
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // H-04: consulta el histórico completo de un año escolar.
    @GetMapping("/anio/{anioEscolarId}/historico")
    public ResponseEntity<List<RestriccionDocenteResponse>>
    listarHistoricoPorAnio(
            @PathVariable UUID anioEscolarId) {

        List<RestriccionDocenteResponse> response =
                restriccionDocenteService.listarHistoricoPorAnio(
                        anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // Actualiza una restricción docente activa.
    @PutMapping("/{id}")
    public ResponseEntity<RestriccionDocenteResponse> actualizarRestriccion(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarRestriccionDocenteRequest request) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.actualizarRestriccion(
                        id,
                        request);

        return ResponseEntity.ok(response);
    }

    // H-04: retira una restricción docente
    // sin eliminar físicamente su registro.
    @PatchMapping("/{id}/retirar")
    public ResponseEntity<RestriccionDocenteResponse> retirarRestriccion(
            @PathVariable UUID id) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.retirarRestriccion(id);

        return ResponseEntity.ok(response);
    }

    // H-04: reactiva una restricción retirada
    // verificando nuevamente las reglas de negocio.
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<RestriccionDocenteResponse> reactivarRestriccion(
            @PathVariable UUID id) {

        RestriccionDocenteResponse response =
                restriccionDocenteService.reactivarRestriccion(id);

        return ResponseEntity.ok(response);
    }
}
