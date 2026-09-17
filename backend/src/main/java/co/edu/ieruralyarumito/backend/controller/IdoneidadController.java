package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.service.IdoneidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

