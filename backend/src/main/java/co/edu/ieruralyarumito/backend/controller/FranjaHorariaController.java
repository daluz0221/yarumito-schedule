package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarFranjaHorariaRequest;
import co.edu.ieruralyarumito.backend.dto.CrearFranjaHorariaRequest;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.service.FranjaHorariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

// Expone los endpoints REST para la gestión de franjas horarias.
@RestController
@RequestMapping("/api/v1/franjas-horarias")
public class FranjaHorariaController {

    private final FranjaHorariaService franjaHorariaService;

    public FranjaHorariaController(
            FranjaHorariaService franjaHorariaService) {

        this.franjaHorariaService = franjaHorariaService;
    }

    // Registra una nueva franja horaria.
    @PostMapping
    public ResponseEntity<FranjaHorariaResponse> registrarFranjaHoraria(
            @Valid @RequestBody CrearFranjaHorariaRequest request) {

        FranjaHorariaResponse response =
                franjaHorariaService
                        .registrarFranjaHoraria(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Consulta una franja horaria por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<FranjaHorariaResponse> consultarFranjaHoraria(
            @PathVariable UUID id) {

        FranjaHorariaResponse response =
                franjaHorariaService
                        .consultarFranjaHoraria(id);

        return ResponseEntity.ok(response);
    }

    // Lista las franjas pertenecientes a un turno.
    @GetMapping
    public ResponseEntity<List<FranjaHorariaResponse>> listarPorTurno(
            @RequestParam UUID turnoId) {

        List<FranjaHorariaResponse> response =
                franjaHorariaService
                        .listarPorTurno(turnoId);

        return ResponseEntity.ok(response);
    }

    // Actualiza una franja horaria existente.
    @PutMapping("/{id}")
    public ResponseEntity<FranjaHorariaResponse> actualizarFranjaHoraria(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarFranjaHorariaRequest request) {

        FranjaHorariaResponse response =
                franjaHorariaService
                        .actualizarFranjaHoraria(
                                id,
                                request);

        return ResponseEntity.ok(response);
    }
}
