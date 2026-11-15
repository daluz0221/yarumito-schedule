package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InformeHorarioGestorIntegracionServiceTest {

    @Test
    void debeConsultarDirectamenteElGestorYGenerarInformePublicado() {

        UUID grupoId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService servicio =
                new InformeHorarioGestorIntegracionService(
                        horarioService,
                        informesService
                );

        HorarioGrupoResponse tablero =
                crearTablero(
                        grupoId,
                        EstadoHorario.PUBLICADO
                );

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(tablero);

        servicio.generarPublicados(
                List.of(grupoId),
                "I.E. Rural Yarumito",
                filtros
        );

        verify(horarioService, times(1))
                .obtenerTablero(grupoId);

        verify(informesService).construirInforme(
                eq(tablero.getAnioEscolarId()),
                eq("2026"),
                eq("I.E. Rural Yarumito"),
                eq("Sede Principal"),
                same(filtros),
                argThat(List::isEmpty)
        );
    }

    @Test
    void debeRechazarHorarioBorradorCuandoSeSolicitanPublicados() {

        UUID grupoId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService servicio =
                new InformeHorarioGestorIntegracionService(
                        horarioService,
                        informesService
                );

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(
                        crearTablero(
                                grupoId,
                                EstadoHorario.BORRADOR
                        )
                );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> servicio.generarPublicados(
                                List.of(grupoId),
                                "I.E. Rural Yarumito",
                                filtros
                        )
                );

        assertTrue(
                error.getMessage().contains("PUBLICADO")
        );

        verify(horarioService, times(1))
                .obtenerTablero(grupoId);

        verifyNoInteractions(informesService);
    }

    @Test
    void debeConvertirClaseOficialDelGestorAlDetalleDeInforme() {

        UUID grupoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService servicio =
                new InformeHorarioGestorIntegracionService(
                        horarioService,
                        informesService
                );

        ClaseProgramadaResponse clase =
                new ClaseProgramadaResponse();

        clase.setDocenteId(docenteId);
        clase.setDocenteNombre("Docente de prueba");

        clase.setAsignaturaId(asignaturaId);
        clase.setAsignaturaNombre("Matematicas");

        clase.setDia(DiasSemana.LUNES);

        clase.setHoraInicio(
                LocalTime.of(7, 0)
        );

        clase.setHoraFin(
                LocalTime.of(7, 55)
        );

        clase.setAulaId(UUID.randomUUID());
        clase.setAulaNombre("Aula 1");

        clase.setEsHoraExtra(false);

        HorarioGrupoResponse tablero =
                crearTablero(
                        grupoId,
                        EstadoHorario.PUBLICADO
                );

        tablero.setHorarioId(horarioId);
        tablero.setClasesProgramadas(
                List.of(clase)
        );

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(tablero);

        servicio.generarPublicados(
                List.of(grupoId),
                "I.E. Rural Yarumito",
                filtros
        );

        verify(informesService).construirInforme(
                eq(tablero.getAnioEscolarId()),
                eq("2026"),
                eq("I.E. Rural Yarumito"),
                eq("Sede Principal"),
                same(filtros),
                argThat(detalle ->
                        detalle.size() == 1
                                && horarioId.equals(
                                detalle.get(0).horarioId())
                                && grupoId.equals(
                                detalle.get(0).grupoId())
                                && docenteId.equals(
                                detalle.get(0).docenteId())
                                && asignaturaId.equals(
                                detalle.get(0).asignaturaId())
                                && "LUNES".equals(
                                detalle.get(0).diaSemana())
                                && "REGULAR".equals(
                                detalle.get(0).jornada())
                                && "PUBLICADO".equals(
                                detalle.get(0).estadoHorario())
                )
        );
    }

    @Test
    void debeEnviarHorarioPublicadoAlServicioDeInformes() {

        UUID grupoId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService servicio =
                new InformeHorarioGestorIntegracionService(
                        horarioService,
                        informesService
                );

        ClaseProgramadaResponse clase =
                new ClaseProgramadaResponse();

        clase.setDocenteId(UUID.randomUUID());
        clase.setDocenteNombre("Docente de prueba");

        clase.setAsignaturaId(UUID.randomUUID());
        clase.setAsignaturaNombre("Matematicas");

        clase.setDia(DiasSemana.LUNES);
        clase.setHoraInicio(LocalTime.of(7, 0));
        clase.setHoraFin(LocalTime.of(7, 55));

        clase.setAulaId(UUID.randomUUID());
        clase.setAulaNombre("Aula 1");

        clase.setEsHoraExtra(false);

        HorarioGrupoResponse tablero =
                crearTablero(
                        grupoId,
                        EstadoHorario.PUBLICADO
                );

        tablero.setAnioEscolarId(anioId);
        tablero.setClasesProgramadas(
                List.of(clase)
        );

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(tablero);

        servicio.generarPublicados(
                List.of(grupoId),
                "I.E. Rural Yarumito",
                filtros
        );

        verify(informesService).construirInforme(
                eq(anioId),
                eq("2026"),
                eq("I.E. Rural Yarumito"),
                eq("Sede Principal"),
                same(filtros),
                argThat(detalle ->
                        detalle.size() == 1
                                && "PUBLICADO".equals(
                                detalle.get(0).estadoHorario())
                                && grupoId.equals(
                                detalle.get(0).grupoId())
                                && "Matematicas".equals(
                                detalle.get(0).nombreAsignatura())
                )
        );

        verify(horarioService, times(1))
                .obtenerTablero(grupoId);
    }

    private HorarioGrupoResponse crearTablero(
            UUID grupoId,
            EstadoHorario estado) {

        HorarioGrupoResponse tablero =
                new HorarioGrupoResponse();

        tablero.setGrupoId(grupoId);
        tablero.setGrupoCodigo("1101");
        tablero.setGradoNombre("Undecimo");

        tablero.setHorarioId(UUID.randomUUID());
        tablero.setVersionHorario(1);

        tablero.setAnioEscolarId(
                UUID.randomUUID()
        );

        tablero.setAnioEscolar(2026);

        tablero.setSedeNombre(
                "Sede Principal"
        );

        tablero.setTurnoNombre(
                "Mañana"
        );

        tablero.setEstadoHorario(estado);

        tablero.setClasesProgramadas(
                List.of()
        );

        return tablero;
    }
}