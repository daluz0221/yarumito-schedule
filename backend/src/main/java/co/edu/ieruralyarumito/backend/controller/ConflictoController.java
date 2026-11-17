package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.service.ConflictoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Expone consultas de conflictos materializados del horario.
@RestController
@RequestMapping("/api/v1/conflictos")
public class ConflictoController {

    private final ConflictoService conflictoService;

    public ConflictoController(
            ConflictoService conflictoService) {

        this.conflictoService =
                conflictoService;
    }

    // Lista todo el historial de conflictos de un horario.
    @GetMapping("/horario/{horarioId}")
    public ResponseEntity<List<ConflictoResponse>> listarPorHorario(
            @PathVariable UUID horarioId) {

        return ResponseEntity.ok(
                conflictoService.listarPorHorario(
                        horarioId));
    }

    // Lista únicamente los conflictos pendientes.
    @GetMapping("/horario/{horarioId}/pendientes")
    public ResponseEntity<List<ConflictoResponse>> listarPendientes(
            @PathVariable UUID horarioId) {

        return ResponseEntity.ok(
                conflictoService.listarPendientes(
                        horarioId));
    }

    // Lista conflictos filtrados por severidad.
    @GetMapping("/horario/{horarioId}/severidad")
    public ResponseEntity<List<ConflictoResponse>> listarPorSeveridad(
            @PathVariable UUID horarioId,
            @RequestParam SeveridadConflicto severidad) {

        return ResponseEntity.ok(
                conflictoService.listarPorSeveridad(
                        horarioId,
                        severidad));
    }

    // Marca como resuelto un conflicto existente.
    @PatchMapping("/{conflictoId}/resuelto")
    public ResponseEntity<ConflictoResponse> marcarResuelto(
            @PathVariable UUID conflictoId) {

        return ResponseEntity.ok(
                conflictoService.marcarResuelto(
                        conflictoId));
    }
}
