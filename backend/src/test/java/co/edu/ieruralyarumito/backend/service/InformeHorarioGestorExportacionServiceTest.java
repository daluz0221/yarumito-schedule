package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InformeHorarioGestorExportacionServiceTest {

    @Test
    void debeConectarIntegracionConExportacionPdf() {

        UUID grupoId = UUID.randomUUID();
        String institucion = "I.E. Rural Yarumito";

        var integracion = mock(
                InformeHorarioGestorIntegracionService.class
        );

        var pdf = mock(InformeHorariosPdfService.class);
        var excel = mock(InformeHorariosExcelService.class);

        var filtros = mock(InformeHorariosFiltroRequest.class);
        var informe = mock(InformeHorariosResponse.class);

        byte[] contenidoPdf = {1, 2, 3};

        when(integracion.generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        )).thenReturn(informe);

        when(integracion.obtenerDescansosOficiales(grupoId))
                .thenReturn(List.of());

        when(pdf.exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        )).thenReturn(contenidoPdf);

        var servicio =
                new InformeHorarioGestorExportacionService(
                        integracion,
                        pdf,
                        excel
                );

        byte[] resultado =
                servicio.exportarPdfInstitucionalPorGrupo(
                        grupoId,
                        institucion,
                        filtros
                );

        assertArrayEquals(contenidoPdf, resultado);

        verify(integracion).generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        );

        verify(integracion)
                .obtenerDescansosOficiales(grupoId);

        verify(pdf).exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        );

        verifyNoInteractions(excel);
    }

    @Test
    void debeConectarIntegracionConExportacionExcel() {

        UUID grupoId = UUID.randomUUID();
        String institucion = "I.E. Rural Yarumito";

        var integracion = mock(
                InformeHorarioGestorIntegracionService.class
        );

        var pdf = mock(InformeHorariosPdfService.class);
        var excel = mock(InformeHorariosExcelService.class);

        var filtros = mock(InformeHorariosFiltroRequest.class);
        var informe = mock(InformeHorariosResponse.class);

        byte[] contenidoExcel = {4, 5, 6};

        when(integracion.generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        )).thenReturn(informe);

        when(integracion.obtenerDescansosOficiales(grupoId))
                .thenReturn(List.of());

        when(excel.exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        )).thenReturn(contenidoExcel);

        var servicio =
                new InformeHorarioGestorExportacionService(
                        integracion,
                        pdf,
                        excel
                );

        byte[] resultado =
                servicio.exportarExcelInstitucionalPorGrupo(
                        grupoId,
                        institucion,
                        filtros
                );

        assertArrayEquals(contenidoExcel, resultado);

        verify(integracion).generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        );

        verify(integracion)
                .obtenerDescansosOficiales(grupoId);

        verify(excel).exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        );

        verifyNoInteractions(pdf);
    }

    @Test
    void debeGenerarPdfYExcelRealesDesdeElGestor() {

        UUID grupoId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();

        HorarioService gestor =
                mock(HorarioService.class);

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
                new HorarioGrupoResponse();

        tablero.setGrupoId(grupoId);
        tablero.setGrupoCodigo("1101");
        tablero.setGradoNombre("Undecimo");

        tablero.setHorarioId(UUID.randomUUID());
        tablero.setVersionHorario(1);

        tablero.setAnioEscolarId(anioId);
        tablero.setAnioEscolar(2026);

        tablero.setSedeNombre("Sede Principal");
        tablero.setTurnoNombre("Mañana");

        tablero.setEstadoHorario(
                EstadoHorario.BORRADOR
        );

        tablero.setClasesProgramadas(
                List.of(clase)
        );

        when(gestor.obtenerTablero(grupoId))
                .thenReturn(tablero);

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anioId,
                        null,
                        LocalDate.of(2026, 11, 2),
                        LocalDate.of(2026, 11, 6),
                        null,
                        null,
                        grupoId,
                        null,
                        "TODAS",
                        null,
                        "BORRADOR"
                );

        InformeHorariosService informes =
                new InformeHorariosService();

        InformeHorarioGestorIntegracionService integracion =
                new InformeHorarioGestorIntegracionService(
                        gestor,
                        informes
                );

        InformeHorarioGestorExportacionService exportacion =
                new InformeHorarioGestorExportacionService(
                        integracion,
                        new InformeHorariosPdfService(),
                        new InformeHorariosExcelService()
                );

        byte[] pdf =
                exportacion.exportarPdfInstitucionalPorGrupo(
                        grupoId,
                        "I.E. Rural Yarumito",
                        filtros
                );

        byte[] excel =
                exportacion.exportarExcelInstitucionalPorGrupo(
                        grupoId,
                        "I.E. Rural Yarumito",
                        filtros
                );

        assertNotNull(pdf);
        assertTrue(pdf.length > 5);

        assertEquals(
                "%PDF-",
                new String(
                        pdf,
                        0,
                        5,
                        StandardCharsets.US_ASCII
                )
        );

        assertNotNull(excel);
        assertTrue(excel.length > 2);

        assertEquals('P', (char) excel[0]);
        assertEquals('K', (char) excel[1]);

        verify(gestor, times(4))
                .obtenerTablero(grupoId);
    }
}