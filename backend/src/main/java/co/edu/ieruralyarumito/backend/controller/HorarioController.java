package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.service.AsignacionAcademicaService;
import co.edu.ieruralyarumito.backend.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Expone el tablero de construcción de horario por grupo (M-11).
@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;
    private final AsignacionAcademicaService asignacionAcademicaService;

    public HorarioController(
            HorarioService horarioService,
            AsignacionAcademicaService asignacionAcademicaService) {
        this.horarioService = horarioService;
        this.asignacionAcademicaService = asignacionAcademicaService;
    }

    // Devuelve la rejilla, los contadores y las asignaciones pendientes
    // de un grupo.
    @GetMapping("/grupos/{grupoId}")
    public ResponseEntity<HorarioGrupoResponse> obtenerTablero(
            @PathVariable UUID grupoId) {

        asignacionAcademicaService
                .sincronizarGruposDelMismoGrado(grupoId);

        return ResponseEntity.ok(
                horarioService.obtenerTablero(grupoId));
    }
}
