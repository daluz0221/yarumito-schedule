package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;


class InformeHorariosPdfVisualTest {

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    private static final Path ARCHIVO = Path.of(
            "target", "evidencias",
            "horario-semanal-validacion.pdf"
    );

    private static final String[] DIAS = {
            "LUNES", "MARTES", "MIERCOLES",
            "JUEVES", "VIERNES"
    };

    private static final LocalTime[] INICIOS = {
            LocalTime.of(6, 30),
            LocalTime.of(7, 25),
            LocalTime.of(8, 20),
            LocalTime.of(9, 45),
            LocalTime.of(10, 40),
            LocalTime.of(11, 35)
    };

    private static final LocalTime[] FINALES = {
            LocalTime.of(7, 25),
            LocalTime.of(8, 20),
            LocalTime.of(9, 15),
            LocalTime.of(10, 40),
            LocalTime.of(11, 35),
            LocalTime.of(12, 30)
    };

    // Horario institucional 2026, grupo 701.
    // Cada fila corresponde a una franja.
    // Cada columna corresponde a lunes-viernes.
    // Los nombres ampliados deben cotejarse
    // con el Catálogo Académico antes de publicar.

    private static final String[][] ASIGNATURAS_701 = {
            {
                    "INGLÉS",
                    "LENGUA CASTELLANA",
                    "MATEMÁTICAS",
                    "CIENCIAS NATURALES",
                    "ÉTICA"
            },
            {
                    "CIENCIAS NATURALES",
                    "LENGUA CASTELLANA",
                    "MATEMÁTICAS",
                    "CIENCIAS NATURALES",
                    "EDUCACIÓN FÍSICA"
            },
            {
                    "CIENCIAS NATURALES",
                    "MATEMÁTICAS",
                    "INGLÉS",
                    "MATEMÁTICAS",
                    "EDUCACIÓN FÍSICA"
            },
            {
                    "RELIGIÓN",
                    "MATEMÁTICAS",
                    "CIENCIAS SOCIALES",
                    "CIENCIAS SOCIALES",
                    "CIENCIAS SOCIALES"
            },
            {
                    "LENGUA CASTELLANA",
                    "CIENCIAS NATURALES",
                    "LENGUA CASTELLANA",
                    "EDUCACIÓN ARTÍSTICA",
                    "TECNOLOGÍA"
            },
            {
                    "CIENCIAS SOCIALES",
                    "EMPRENDIMIENTO",
                    "LENGUA CASTELLANA",
                    "EDUCACIÓN ARTÍSTICA",
                    "TECNOLOGÍA"
            }
    };

    @Test
    void generaExcelSemanalDelGrupo701() throws Exception {

        InformeHorariosResponse informe =
                construirInformeVisual701();

        byte[] excel =
                new InformeHorariosExcelService()
                        .exportarHorarioSemanalPorGrupo(informe);

        // Comprobar el contenido antes de guardar.
        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(excel))) {

            assertEquals(1, libro.getNumberOfSheets());

            var hoja = libro.getSheetAt(0);

            assertEquals("Horario semanal", hoja.getSheetName());

            assertEquals(
                    "Clase",
                    hoja.getRow(2).getCell(0).getStringCellValue()
            );

            assertEquals(
                    "Horario",
                    hoja.getRow(2).getCell(1).getStringCellValue()
            );

            assertEquals(
                    "Lunes",
                    hoja.getRow(2).getCell(2).getStringCellValue()
            );

            assertEquals(
                    "Viernes",
                    hoja.getRow(2).getCell(6).getStringCellValue()
            );

            assertEquals(
                    "09:15 - 09:45",
                    hoja.getRow(6).getCell(1).getStringCellValue()
            );

            assertEquals(
                    "DESCANSO",
                    hoja.getRow(6).getCell(2).getStringCellValue()
            );

            assertEquals(
                    6,
                    (int) hoja.getRow(9).getCell(0)
                            .getNumericCellValue()
            );
        }

        Path archivo = Path.of(
                "target",
                "evidencias",
                "horario-semanal-701.xlsx"
        );

        Files.createDirectories(archivo.getParent());
        Files.write(archivo, excel);

        System.out.println(
                "Excel generado: " + archivo.toAbsolutePath()
        );
    }

    @Test
    void generaPdfParaInspeccionVisual() throws Exception {

        InformeHorariosResponse informe =
                construirInformeVisual701();

        MetadatosInformeResponse metadatos =
                new MetadatosInformeResponse(
                        "I.E. Rural Yarumito",
                        "Principal",
                        "SEDE-PRINCIPAL",
                        Map.of(
                                "anio", "2026",
                                "jornada", "REGULAR",
                                "estado", "PUBLICADO"
                        ),
                        LocalDateTime.of(
                                2026, 11, 6, 10, 0
                        ),
                        "Usuario de prueba"
                );

        byte[] pdf = pdfService.exportarHorarioSemanalPorGrupo(
                informe
        );

        Files.createDirectories(ARCHIVO.getParent());
        Files.write(ARCHIVO, pdf);

        assertTrue(Files.exists(ARCHIVO));
        assertTrue(Files.size(ARCHIVO) > 0);

        assertEquals(
                30,
                informe.totalClasesProgramadas()
        );

        try (PDDocument documento = Loader.loadPDF(pdf)) {

            int paginas = documento.getNumberOfPages();

            assertTrue(paginas >= 1);

            String texto = new PDFTextStripper()
                    .getText(documento);

            assertTrue(texto.contains("HORARIO SEMANAL"));
            assertTrue(texto.contains("701"));
            assertFalse(texto.contains("DETALLE DE CLASES"));
            assertFalse(texto.contains("RESUMEN DE CARGA"));
            assertFalse(texto.contains("TRAZABILIDAD"));

            for (int pagina = 0; pagina < paginas; pagina++) {

                assertEquals(
                        792f,
                        documento.getPage(pagina)
                                .getMediaBox().getWidth(),
                        0.1f
                );

                assertEquals(
                        612f,
                        documento.getPage(pagina)
                                .getMediaBox().getHeight(),
                        0.1f
                );
            }

            System.out.println(
                    "PDF generado: "
                            + ARCHIVO.toAbsolutePath()
            );

            System.out.println(
                    "Paginas generadas: " + paginas
            );

            System.out.println(
                    "Grupo 701 - Clases: "
                            + informe.totalClasesProgramadas()
            );
        }
    }

    private InformeHorariosResponse construirInformeVisual701() {

        UUID anioEscolarId = UUID.randomUUID();

        // Mismo horario y grupo para las 30 clases.
        UUID horarioId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anioEscolarId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        grupoId,
                        null,
                        "REGULAR",
                        null,
                        "PUBLICADO"
                );

        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        for (int franja = 0;
             franja < INICIOS.length;
             franja++) {

            for (int dia = 0;
                 dia < DIAS.length;
                 dia++) {

                String asignatura =
                        ASIGNATURAS_701[franja][dia];

                UUID asignaturaId =
                        UUID.nameUUIDFromBytes(
                                asignatura.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

                clases.add(
                        new InformeHorarioClaseDetalleResponse(
                                horarioId,
                                1,
                                "PUBLICADO",
                                null,
                                "DOCENTE NO INFORMADO",
                                grupoId,
                                "7",
                                "701",
                                asignaturaId,
                                asignatura,
                                DIAS[dia],
                                INICIOS[franja],
                                FINALES[franja],
                                "REGULAR",
                                null,
                                "AULA NO INFORMADA",
                                false
                        )
                );
            }
        }

        return informeService.construirInforme(
                anioEscolarId,
                "2026",
                "I.E. Rural Yarumito",
                "Principal",
                filtros,
                clases
        );
    }
}
