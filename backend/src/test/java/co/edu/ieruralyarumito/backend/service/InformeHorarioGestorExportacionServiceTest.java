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

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de integracion de exportaciones PDF y Excel
 * con el Gestor de Horarios de Juan Camilo.
 *
 * Ambas exportaciones reciben descansos oficiales.
 *
 * No modifica PostgreSQL ni el Gestor.
 */
class InformeHorarioGestorExportacionServiceTest {

    @Test
    void debeConectarGestorConExportacionPdf() {

        UUID grupoId = UUID.randomUUID();
        String institucion = "I.E. Rural Yarumito";

        var puente = mock(
                InformeHorarioJuanCamiloBridgeService.class
        );

        var pdf = mock(InformeHorariosPdfService.class);
        var excel = mock(InformeHorariosExcelService.class);

        var filtros = mock(InformeHorariosFiltroRequest.class);
        var informe = mock(InformeHorariosResponse.class);

        byte[] contenidoPdf = {1, 2, 3};

        when(puente.generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        )).thenReturn(informe);

        /*
         * El Gestor no informa descansos en esta prueba.
         * No se deben inventar descansos.
         */
        when(puente.obtenerDescansosOficiales(grupoId))
                .thenReturn(List.of());

        /*
         * PDF ahora recibe el informe junto con
         * la lista de descansos oficiales.
         */
        when(pdf.exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        )).thenReturn(contenidoPdf);

        var servicio =
                new InformeHorarioGestorExportacionService(
                        puente,
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

        verify(puente).generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        );

        verify(puente)
                .obtenerDescansosOficiales(grupoId);

        verify(pdf).exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        );

        verifyNoInteractions(excel);
    }

    @Test
    void debeConectarGestorConExportacionExcel() {

        UUID grupoId = UUID.randomUUID();
        String institucion = "I.E. Rural Yarumito";

        var puente = mock(
                InformeHorarioJuanCamiloBridgeService.class
        );

        var pdf = mock(InformeHorariosPdfService.class);
        var excel = mock(InformeHorariosExcelService.class);

        var filtros = mock(InformeHorariosFiltroRequest.class);
        var informe = mock(InformeHorariosResponse.class);

        byte[] contenidoExcel = {4, 5, 6};

        when(puente.generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        )).thenReturn(informe);

        /*
         * El Gestor no informa descansos en esta prueba.
         * Una lista vacia no autoriza inventar horarios.
         */
        when(puente.obtenerDescansosOficiales(grupoId))
                .thenReturn(List.of());

        /*
         * Excel recibe las franjas oficiales.
         */
        when(excel.exportarHorarioSemanalPorGrupo(
                informe,
                List.of()
        )).thenReturn(contenidoExcel);

        var servicio =
                new InformeHorarioGestorExportacionService(
                        puente,
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

        verify(puente).generarInstitucional(
                List.of(grupoId),
                institucion,
                filtros
        );

        verify(puente)
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

        /*
         * Solo se simula el servicio del Gestor.
         *
         * La construccion del informe y las
         * exportaciones PDF y Excel son reales.
         */
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

        /*
         * BORRADOR permite comprobar el informe
         * institucional sin simular una publicacion
         * que el Gestor todavia no proporciona.
         *
         * No sustituye el estado PUBLICADO.
         */
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
                        informes
                );

        InformeHorarioJuanCamiloBridgeService puente =
                new InformeHorarioJuanCamiloBridgeService(
                        gestor,
                        integracion
                );

        InformeHorarioGestorExportacionService exportacion =
                new InformeHorarioGestorExportacionService(
                        puente,
                        new InformeHorariosPdfService(),
                        new InformeHorariosExcelService()
                );

        /*
         * Exportaciones reales.
         *
         * Ambas consultan los descansos oficiales
         * por medio del puente de integracion.
         */
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

        /*
         * Comprobacion del archivo PDF real.
         */
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

        /*
         * XLSX es un archivo ZIP.
         * Se comprueba su firma inicial.
         */
        assertNotNull(excel);
        assertTrue(excel.length > 2);

        assertEquals('P', (char) excel[0]);
        assertEquals('K', (char) excel[1]);

        /*
         * Consulta 1: generar PDF institucional.
         * Consulta 2: obtener descansos para PDF.
         * Consulta 3: generar Excel institucional.
         * Consulta 4: obtener descansos para Excel.
         *
         * Cada exportacion consulta el tablero
         * y posteriormente sus descansos oficiales.
         */
        verify(gestor, times(4))
                .obtenerTablero(grupoId);
    }
}
