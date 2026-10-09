package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.service.AsignaturaService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Expone las asignaturas de un área para el catálogo académico.
@RestController
@RequestMapping("/api/v1/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    public ResponseEntity<List<AsignaturaResponse>> listarPorArea(@RequestParam UUID areaId) {
        return ResponseEntity.ok(asignaturaService.listarPorArea(areaId));
    }
}
