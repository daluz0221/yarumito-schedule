package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.service.TituloProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Expone los endpoints REST para la gestión de títulos profesionales.
@RestController
@RequestMapping("/api/v1")
public class TituloProfesionalController {

    // Servicio que contiene la lógica de negocio de títulos profesionales.
    private final TituloProfesionalService tituloProfesionalService;

    // Inyección de dependencias mediante constructor.
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
    @GetMapping("/titulos-profesionales/{id}")
    public ResponseEntity<TituloProfesionalResponse> consultarTituloProfesional(
            @PathVariable UUID id) {

        TituloProfesionalResponse response =
                tituloProfesionalService.consultarTituloProfesional(id);

        return ResponseEntity.ok(response);
    }

    // Lista los títulos profesionales asociados a un docente.
    @GetMapping("/docentes/{docenteId}/titulos-profesionales")
    public ResponseEntity<List<TituloProfesionalResponse>> listarTitulosPorDocente(
            @PathVariable UUID docenteId) {

        List<TituloProfesionalResponse> response =
                tituloProfesionalService.listarTitulosPorDocente(docenteId);

        return ResponseEntity.ok(response);
    }

    // Actualiza los datos permitidos de un título profesional existente.
    @PutMapping("/titulos-profesionales/{id}")
    public ResponseEntity<TituloProfesionalResponse> actualizarTituloProfesional(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarTituloProfesionalRequest request) {

        TituloProfesionalResponse response =
                tituloProfesionalService.actualizarTituloProfesional(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
