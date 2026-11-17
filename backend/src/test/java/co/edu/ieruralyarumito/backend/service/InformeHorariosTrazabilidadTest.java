
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosTrazabilidadTest {

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    private final InformeHorariosExcelService excelService =
            new InformeHorariosExcelService();

    @Test
    void pdfIncluyeTrazabilidadAlFinal() throws Exception {
        byte[] archivo = pdfService.exportarHorarios(
                informeDePrueba(),
                metadatosDePrueba()
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = new PDFTextStripper()
                    .getText(documento);

            assertTrue(texto.contains(
                    "TRAZABILIDAD DEL INFORME"));

            assertTrue(texto.contains(
                    "I.E. Rural Yarumito"));

            assertTrue(texto.contains(
                    "Principal"));

            assertTrue(texto.contains(
                    "2026-10-24 15:00:00"));

            assertTrue(texto.contains(
                    "Rector de prueba"));
        }
    }

    @Test
    void excelIncluyeTrazabilidadEnLasTresHojas()
            throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeDePrueba(),
                metadatosDePrueba()
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            assertEquals(3, libro.getNumberOfSheets());

            assertNotNull(libro.getSheet("Horario semanal"));
            assertNotNull(libro.getSheet("Detalle horarios"));
            assertNotNull(libro.getSheet("Carga academica"));

            for (int i = 0; i < libro.getNumberOfSheets(); i++) {

                Sheet hoja = libro.getSheetAt(i);
                String contenido = extraerTexto(hoja);

                assertTrue(
                        contenido.contains("I.E. Rural Yarumito"),
                        "Falta institucion en " + hoja.getSheetName()
                );

                assertTrue(
                        contenido.contains("Principal"),
                        "Falta sede en " + hoja.getSheetName()
                );

                assertTrue(
                        contenido.contains("2026-10-24 15:00:00"),
                        "Falta fecha de generacion en "
                                + hoja.getSheetName()
                );

                assertTrue(
                        contenido.contains("Rector de prueba"),
                        "Falta usuario en " + hoja.getSheetName()
                );

                assertTrue(
                        contenido.contains("Filtro docente"),
                        "Faltan filtros en " + hoja.getSheetName()
                );

                assertNotNull(
                        libro.getPrintArea(i),
                        "Falta area de impresion en "
                                + hoja.getSheetName()
                );
            }
        }
    }

    @Test
    void exportacionesFuncionanSinMetadatos()
            throws Exception {

        InformeHorariosResponse informe =
                informeDePrueba();

        assertTrue(
                pdfService.exportarHorarios(
                        informe, null
                ).length > 0
        );

        assertTrue(
                excelService.exportarHorarios(
                        informe, null
                ).length > 0
        );
    }

    private String extraerTexto(Sheet hoja) {
        StringBuilder texto = new StringBuilder();

        hoja.forEach(fila ->
                fila.forEach(celda ->
                        texto.append(celda.toString())
                                .append('\n')
                )
        );

        return texto.toString();
    }

    private InformeHorariosResponse informeDePrueba() {
        UUID anio = UUID.randomUUID();

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anio,
                        null,
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 4, 3),
                        null,
                        null,
                        null,
                        null,
                        "TODAS",
                        null,
                        "PUBLICADO"
                );

        return informeService.construirInforme(
                anio,
                "2026",
                "I.E. Rural Yarumito",
                "Principal",
                filtros,
                List.of()
        );
    }

    private MetadatosInformeResponse metadatosDePrueba() {
        return new MetadatosInformeResponse(
                "I.E. Rural Yarumito",
                "Principal",
                "SEDE-PRINCIPAL",
                Map.of(
                        "docente", "Todos",
                        "grado", "Todos",
                        "jornada", "Todas",
                        "rango", "02/02/2026 - 03/04/2026"
                ),
                LocalDateTime.of(
                        2026, 10, 24, 15, 0, 0
                ),
                "Rector de prueba"
        );
    }
}
