package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCargaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InformeHorariosCargaSemanalH07Test {

    private final InformeHorariosService informesService =
            new InformeHorariosService();

    private final InformeHorariosExcelService excelService =
            new InformeHorariosExcelService();

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    private UUID anioId;
    private UUID docenteId;
    private UUID grupoId;
    private UUID asignaturaId;
    private UUID horarioId;

    private InformeHorariosResponse informe;

    @BeforeEach
    void prepararInforme() {

        anioId = UUID.randomUUID();
        docenteId = UUID.randomUUID();
        grupoId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();
        horarioId = UUID.randomUUID();

        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        String[] dias = {
                "LUNES",
                "MARTES",
                "MIERCOLES",
                "JUEVES"
        };

        for (int i = 0; i < 32; i++) {

            InformeHorarioClaseDetalleResponse clase =
                    mock(InformeHorarioClaseDetalleResponse.class);

            int indiceDia = i / 8;
            int indiceFranja = i % 8;

            LocalTime inicio =
                    LocalTime.of(7, 0)
                            .plusMinutes(indiceFranja * 60L);

            LocalTime fin = inicio.plusMinutes(55);

            lenient().when(clase.horarioId())
                    .thenReturn(horarioId);

            lenient().when(clase.versionHorario())
                    .thenReturn(1);

            lenient().when(clase.estadoHorario())
                    .thenReturn("BORRADOR");

            lenient().when(clase.docenteId())
                    .thenReturn(docenteId);

            lenient().when(clase.nombreDocente())
                    .thenReturn("DOCENTE PRUEBA H07");

            lenient().when(clase.grupoId())
                    .thenReturn(grupoId);

            lenient().when(clase.nombreGrupo())
                    .thenReturn("GRUPO PRUEBA");

            lenient().when(clase.grado())
                    .thenReturn("SEXTO");

            lenient().when(clase.asignaturaId())
                    .thenReturn(asignaturaId);

            lenient().when(clase.nombreAsignatura())
                    .thenReturn("MATEMATICAS");

            lenient().when(clase.diaSemana())
                    .thenReturn(dias[indiceDia]);

            lenient().when(clase.horaInicio())
                    .thenReturn(inicio);

            lenient().when(clase.horaFin())
                    .thenReturn(fin);

            lenient().when(clase.jornada())
                    .thenReturn("REGULAR");

            lenient().when(clase.nombreAula())
                    .thenReturn("AULA 1");

            // Las primeras 22 clases son ordinarias.
            // Las últimas 10 son extras.
            lenient().when(clase.esHoraExtra())
                    .thenReturn(i >= 22);

            clases.add(clase);
        }

        InformeHorariosFiltroRequest filtros =
                mock(InformeHorariosFiltroRequest.class);

        when(filtros.tieneRangoFechasValido())
                .thenReturn(true);

        informe = informesService.construirInforme(
                anioId,
                "2026",
                "I. E. Rural Yarumito",
                "Sede Principal",
                filtros,
                clases
        );
    }

    @Test
    void H07_resumenDebeConservar22OrdinariasY10Extras() {

        assertEquals(
                32,
                informe.totalClasesProgramadas()
        );

        assertEquals(
                32,
                informe.horasAcademicasProgramadas()
        );

        assertEquals(
                1,
                informe.resumenCargaHoraria().size()
        );

        InformeHorarioCargaResponse carga =
                informe.resumenCargaHoraria().get(0);

        assertEquals(22, carga.horasOrdinarias());
        assertEquals(10, carga.horasExtras());
        assertEquals(32, carga.totalHorasAcademicas());

        assertEquals(
                carga.totalHorasAcademicas(),
                carga.horasOrdinarias() + carga.horasExtras()
        );
    }

    @Test
    void H07_excelDebeExportar22OrdinariasY10Extras()
            throws IOException {

        byte[] archivo =
                excelService.exportarHorarios(informe, null);

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            Sheet hoja = libro.getSheet("Carga academica");

            assertNotNull(hoja);

            // El servicio escribe encabezados en fila 2
            // y datos desde fila 3.
            Row fila = hoja.getRow(3);

            assertNotNull(fila);

            assertEquals(
                    22,
                    fila.getCell(3).getNumericCellValue(),
                    0.001
            );

            assertEquals(
                    10,
                    fila.getCell(4).getNumericCellValue(),
                    0.001
            );

            assertEquals(
                    32,
                    fila.getCell(7).getNumericCellValue(),
                    0.001
            );
        }
    }

    @Test
    void H07_pdfDebeMostrar22OrdinariasY10Extras()
            throws IOException {

        byte[] archivo =
                pdfService.exportarHorarios(informe);

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            String texto =
                    new PDFTextStripper().getText(documento);

            assertTrue(
                    texto.contains("Ordinarias: 22"),
                    "El PDF debe mostrar las 22 horas ordinarias."
            );

            assertTrue(
                    texto.contains("Extras: 10"),
                    "El PDF debe mostrar las 10 horas extra."
            );

            assertTrue(
                    texto.contains("Total: 32"),
                    "El PDF debe mostrar las 32 horas totales."
            );
        }
    }
}
