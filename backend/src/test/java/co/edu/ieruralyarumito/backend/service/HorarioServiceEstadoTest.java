package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica que el tablero del Gestor exponga el estado del Horario
 * (BORRADOR/PUBLICADO/ARCHIVADO) y no el estado de la asignación
 * académica (CONFIRMADA). HU-04 consume este valor para sus informes.
 */
class HorarioServiceEstadoTest {

    @Test
    void exponeBorradorCuandoNoHayHorarioNiAsignaciones() {

        assertEquals(
                EstadoHorario.BORRADOR,
                HorarioService.determinarEstadoHorario(
                        null, List.of())
        );
    }

    @Test
    void exponeBorradorCuandoFaltaConfirmarAsignaciones() {

        Horario horario = new Horario();
        horario.setEstado(EstadoHorario.BORRADOR);

        assertEquals(
                EstadoHorario.BORRADOR,
                HorarioService.determinarEstadoHorario(
                        horario,
                        List.of(
                                asignacion(EstadoAsignacionAcademica.CONFIRMADA),
                                asignacion(EstadoAsignacionAcademica.BORRADOR)
                        ))
        );
    }

    @Test
    void exponePublicadoCuandoTodasLasAsignacionesEstanConfirmadas() {

        Horario horario = new Horario();
        horario.setEstado(EstadoHorario.BORRADOR);

        assertEquals(
                EstadoHorario.PUBLICADO,
                HorarioService.determinarEstadoHorario(
                        horario,
                        List.of(
                                asignacion(EstadoAsignacionAcademica.CONFIRMADA),
                                asignacion(EstadoAsignacionAcademica.CONFIRMADA)
                        ))
        );
    }

    @Test
    void conservaPublicadoAunqueElHorarioNoTengaAsignaciones() {

        Horario horario = new Horario();
        horario.setEstado(EstadoHorario.PUBLICADO);

        assertEquals(
                EstadoHorario.PUBLICADO,
                HorarioService.determinarEstadoHorario(
                        horario, List.of())
        );
    }

    @Test
    void exponeArchivadoPorEncimaDeLasAsignacionesConfirmadas() {

        Horario horario = new Horario();
        horario.setEstado(EstadoHorario.ARCHIVADO);

        assertEquals(
                EstadoHorario.ARCHIVADO,
                HorarioService.determinarEstadoHorario(
                        horario,
                        List.of(
                                asignacion(EstadoAsignacionAcademica.CONFIRMADA)
                        ))
        );
    }

    private AsignacionAcademica asignacion(
            EstadoAsignacionAcademica estado) {

        AsignacionAcademica asignacion =
                new AsignacionAcademica();

        asignacion.setEstado(estado);

        return asignacion;
    }
}
