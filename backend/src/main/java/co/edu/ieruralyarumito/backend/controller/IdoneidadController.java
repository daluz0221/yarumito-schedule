package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.dto.ActualizarIdoneidadRequest;
import co.edu.ieruralyarumito.backend.service.IdoneidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.UUID;
import java.util.List;

// Expone los endpoints REST para la gestión de idoneidades docentes.
@RestController
@RequestMapping("/api/v1/idoneidades")
public class IdoneidadController {

    // Servicio que contiene la lógica de negocio de idoneidades.
    private final IdoneidadService idoneidadService;

    // Inyección de dependencias mediante constructor.
    public IdoneidadController(IdoneidadService idoneidadService) {
        this.idoneidadService = idoneidadService;
    }

    // Registra una nueva idoneidad docente.
    @PostMapping
    public ResponseEntity<IdoneidadResponse> registrarIdoneidad(
            @Valid @RequestBody CrearIdoneidadRequest request) {

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
    // Consulta una idoneidad por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<IdoneidadResponse> consultarIdoneidad(
            @PathVariable UUID id) {

        IdoneidadResponse response =
                idoneidadService.consultarIdoneidad(id);

        return ResponseEntity.ok(response);
    }

    // Lista todas las idoneidades registradas.
    @GetMapping
    public ResponseEntity<List<IdoneidadResponse>> listarIdoneidades() {

        List<IdoneidadResponse> response =
                idoneidadService.listarIdoneidades();

        return ResponseEntity.ok(response);
    }

    // Actualiza los datos permitidos de una idoneidad existente.
    @PutMapping("/{id}")
    public ResponseEntity<IdoneidadResponse> actualizarIdoneidad(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarIdoneidadRequest request) {

        IdoneidadResponse response =
                idoneidadService.actualizarIdoneidad(id, request);

        return ResponseEntity.ok(response);
    }

}

