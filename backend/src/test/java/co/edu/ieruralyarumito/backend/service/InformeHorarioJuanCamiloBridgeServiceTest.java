package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;
import java.util.function.Function;

class InformeHorarioJuanCamiloBridgeServiceTest {

    @Test
    void debeConsultarElGestorYRechazarConfirmadaComoPublicado() {

        UUID grupoId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService integracion =
                new InformeHorarioGestorIntegracionService(
                        informesService);

        InformeHorarioJuanCamiloBridgeService puente =
                new InformeHorarioJuanCamiloBridgeService(
                        horarioService, integracion);

        HorarioGrupoResponse tablero =
                crearTablero(
                        grupoId,
                        EstadoAsignacionAcademica.CONFIRMADA);

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(tablero);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> puente.generarPublicados(
                        List.of(grupoId),
                        "I.E. Rural Yarumito",
                        filtros)
        );

        assertTrue(
                error.getMessage().contains("CONFIRMADA")
        );

        verify(horarioService, times(1))
                .obtenerTablero(grupoId);

        verifyNoInteractions(informesService);
    }

    @Test
    void debeRechazarBorradorEnInformePublicado() {

        UUID grupoId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        InformeHorarioGestorIntegracionService integracion =
                new InformeHorarioGestorIntegracionService(
                        informesService);

        InformeHorarioJuanCamiloBridgeService puente =
                new InformeHorarioJuanCamiloBridgeService(
                        horarioService, integracion);

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(crearTablero(
                        grupoId,
                        EstadoAsignacionAcademica.BORRADOR));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> puente.generarPublicados(
                        List.of(grupoId),
                        "I.E. Rural Yarumito",
                        filtros)
        );

        assertTrue(
                error.getMessage().contains("PUBLICADO")
        );

        verify(horarioService, times(1))
                .obtenerTablero(grupoId);

        verifyNoInteractions(informesService);
    }

    private HorarioGrupoResponse crearTablero(
            UUID grupoId,
            EstadoAsignacionAcademica estado) {

        HorarioGrupoResponse tablero =
                new HorarioGrupoResponse();

        tablero.setGrupoId(grupoId);
        tablero.setGrupoCodigo("701");
        tablero.setGradoNombre("Septimo");

        tablero.setHorarioId(UUID.randomUUID());
        tablero.setVersionHorario(1);

        tablero.setAnioEscolarId(UUID.randomUUID());
        tablero.setAnioEscolar(2026);

        tablero.setSedeNombre("Sede Principal");
        tablero.setTurnoNombre("Mañana");

        tablero.setEstadoHorario(estado);
        tablero.setClasesProgramadas(List.of());

        return tablero;
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void debeConvertirUnaClaseDelGestorAlContratoDeInformes() {

        UUID grupoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        HorarioService horarioService =
                mock(HorarioService.class);

        InformeHorarioGestorIntegracionService integracion =
                mock(InformeHorarioGestorIntegracionService.class);

        InformeHorarioJuanCamiloBridgeService puente =
                new InformeHorarioJuanCamiloBridgeService(
                        horarioService, integracion);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        ClaseProgramadaResponse clase =
                new ClaseProgramadaResponse();

        clase.setDocenteId(docenteId);
        clase.setDocenteNombre("Docente de prueba");
        clase.setAsignaturaId(asignaturaId);
        clase.setAsignaturaNombre("Matematicas");
        clase.setDia(DiasSemana.LUNES);
        clase.setHoraInicio(LocalTime.of(7, 0));
        clase.setHoraFin(LocalTime.of(7, 55));
        clase.setEsHoraExtra(false);

        HorarioGrupoResponse tablero =
                new HorarioGrupoResponse();

        tablero.setGrupoId(grupoId);
        tablero.setGrupoCodigo("1101");
        tablero.setGradoNombre("Undecimo");
        tablero.setHorarioId(horarioId);
        tablero.setVersionHorario(1);
        tablero.setAnioEscolarId(UUID.randomUUID());
        tablero.setAnioEscolar(2026);
        tablero.setSedeNombre("Sede Principal");
        tablero.setTurnoNombre("Mañana");
        tablero.setEstadoHorario(
                EstadoAsignacionAcademica.BORRADOR);
        tablero.setClasesProgramadas(List.of(clase));

        when(horarioService.obtenerTablero(grupoId))
                .thenReturn(tablero);

        // Capturamos la consulta que el puente entrega
        // a nuestro servicio de integracion.
        puente.generarPublicados(
                List.of(grupoId),
                "I.E. Rural Yarumito",
                filtros
        );

        ArgumentCaptor<Function> consulta =
                ArgumentCaptor.forClass(Function.class);

        verify(integracion).generarInformePublicados(
                eq(List.of(grupoId)),
                eq("I.E. Rural Yarumito"),
                same(filtros),
                consulta.capture()
        );

        var resultado =
                (InformeHorarioGestorIntegracionService.TableroGestor)
                        consulta.getValue().apply(grupoId);

        assertEquals(grupoId, resultado.grupoId());
        assertEquals(horarioId, resultado.horarioId());
        assertEquals("1101", resultado.nombreGrupo());
        assertEquals("BORRADOR", resultado.estadoHorario());
        assertEquals("REGULAR", resultado.jornada());

        assertEquals(1, resultado.clases().size());

        var claseConvertida = resultado.clases().get(0);

        assertEquals(docenteId, claseConvertida.docenteId());
        assertEquals(asignaturaId, claseConvertida.asignaturaId());
        assertEquals("LUNES", claseConvertida.diaSemana());
        assertEquals(
                LocalTime.of(7, 55),
                claseConvertida.horaFin()
        );

        verify(horarioService).obtenerTablero(grupoId);
    }


    @Test
    void debeEnviarHorarioPublicadoAlServicioDeInformes() {

        UUID grupoId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();

        InformeHorariosService informesService =
                mock(InformeHorariosService.class);

        InformeHorarioGestorIntegracionService integracion =
                new InformeHorarioGestorIntegracionService(
                        informesService);

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        var clase =
                new InformeHorarioGestorIntegracionService.ClaseGestor(
                        UUID.randomUUID(),
                        "Docente de prueba",
                        UUID.randomUUID(),
                        "Matematicas",
                        "LUNES",
                        LocalTime.of(7, 0),
                        LocalTime.of(7, 55),
                        UUID.randomUUID(),
                        "Aula 1",
                        false
                );

        var tablero =
                new InformeHorarioGestorIntegracionService.TableroGestor(
                        UUID.randomUUID(),
                        1,
                        "PUBLICADO",
                        grupoId,
                        "Undecimo",
                        "1101",
                        anioId,
                        "2026",
                        "Sede Principal",
                        "REGULAR",
                        List.of(clase)
                );

        integracion.generarInformePublicados(
                List.of(grupoId),
                "I.E. Rural Yarumito",
                filtros,
                id -> tablero
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
    }

}
