package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ParametroInstitucionalResponse;
import co.edu.ieruralyarumito.backend.service.ParametroInstitucionalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Expone los endpoints REST de consulta para los parámetros institucionales.
@RestController
@RequestMapping("/api/v1/parametros-institucionales")
public class ParametroInstitucionalController {

    private final ParametroInstitucionalService parametroInstitucionalService;

    // Inyección de dependencias mediante constructor.
    public ParametroInstitucionalController(
            ParametroInstitucionalService parametroInstitucionalService) {

        this.parametroInstitucionalService =
                parametroInstitucionalService;
    }

    // Lista los parámetros institucionales asociados a un año escolar.
    @GetMapping("/anio/{anioEscolarId}")
    public ResponseEntity<List<ParametroInstitucionalResponse>> listarPorAnio(
            @PathVariable UUID anioEscolarId) {

        List<ParametroInstitucionalResponse> response =
                parametroInstitucionalService.listarPorAnio(anioEscolarId);

        return ResponseEntity.ok(response);
    }

    // Consulta un parámetro específico por año escolar y clave.
    @GetMapping("/anio/{anioEscolarId}/clave/{clave}")
    public ResponseEntity<ParametroInstitucionalResponse> consultarPorAnioYClave(
            @PathVariable UUID anioEscolarId,
            @PathVariable String clave) {

        ParametroInstitucionalResponse response =
                parametroInstitucionalService.consultarPorAnioYClave(
                        anioEscolarId,
                        clave);

        return ResponseEntity.ok(response);
    }
}
