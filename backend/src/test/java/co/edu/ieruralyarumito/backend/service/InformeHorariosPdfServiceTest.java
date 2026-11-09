package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosPdfServiceTest {

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    @Test
    void generaPdfValidoEnFormatoCartaHorizontal()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                crearInforme(2)
        );

        assertTrue(archivo.length > 0);

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            assertTrue(documento.getNumberOfPages() >= 1);

            PDRectangle pagina =
                    documento.getPage(0).getMediaBox();

            // Carta horizontal: 792 puntos de ancho.
            assertEquals(
                    792f,
                    pagina.getWidth(),
                    0.01f
            );

            // Carta horizontal: 612 puntos de alto.
            assertEquals(
                    612f,
                    pagina.getHeight(),
                    0.01f
            );

            assertTrue(
                    pagina.getWidth() > pagina.getHeight()
            );
        }
    }

    @Test
    void conservaLasHorasExactasDeLasClases()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                crearInforme(2)
        );

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            String contenido =
                    new PDFTextStripper().getText(documento);

            assertTrue(contenido.contains("07:00"));
            assertTrue(contenido.contains("08:00"));
            assertTrue(contenido.contains("CONTRAJORNADA"));
        }
    }

    @Test
    void mantieneContinuidadEnMultiplesPaginas()
            throws Exception {

        InformeHorariosResponse informe =
                crearInforme(100);

        byte[] archivo =
                pdfService.exportarHorarios(informe);

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            assertTrue(
                    documento.getNumberOfPages() > 1
            );

            String contenido =
                    new PDFTextStripper().getText(documento);

            assertTrue(
                    contenido.contains(
                            "Continuacion del informe"
                    )
            );

            assertTrue(
                    contenido.contains("Pagina 1 de ")
            );

            assertTrue(
                    contenido.contains(
                            "Pagina "
                                    + documento.getNumberOfPages()
                                    + " de "
                                    + documento.getNumberOfPages()
                    )
            );
        }
    }

    @Test
    void conservaTodasLasClasesSinPerderRegistros()
            throws Exception {

        InformeHorariosResponse informe =
                crearInforme(100);

        byte[] archivo =
                pdfService.exportarHorarios(informe);

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            String contenido =
                    new PDFTextStripper().getText(documento);

            for (int i = 0; i < 100; i++) {

                String identificador =
                        "AULA-" + i;

                assertTrue(
                        contenido.contains(identificador),
                        "Falta clase en " + identificador
                );
            }
        }
    }

    @Test
    void rechazaInformeNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pdfService.exportarHorarios(null)
        );
    }

    private InformeHorariosResponse crearInforme(
            int cantidadClases) {

        UUID anio = UUID.randomUUID();
        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anio,
                        null,
                        null,
                        null,
                        docente,
                        null,
                        grupo,
                        asignatura,
                        "TODAS",
                        null,
                        "PUBLICADO"
                );

        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        for (int i = 0; i < cantidadClases; i++) {

            boolean contrajornada = i % 2 != 0;

            LocalTime inicio = contrajornada
                    ? LocalTime.of(14, 0)
                    : LocalTime.of(7, 0);

            clases.add(
                    new InformeHorarioClaseDetalleResponse(
                            horario,
                            1,
                            "PUBLICADO",
                            docente,
                            "Docente de prueba",
                            grupo,
                            "10",
                            "10A",
                            asignatura,
                            "Matematicas",
                            contrajornada
                                    ? "MARTES" : "LUNES",
                            inicio,
                            inicio.plusHours(1),
                            contrajornada
                                    ? "CONTRAJORNADA"
                                    : "REGULAR",
                            UUID.randomUUID(),
                            "AULA-" + i,
                            contrajornada
                    )
            );
        }

        return informeService.construirInforme(
                anio,
                "2026",
                "I.E. Rural Yarumito",
                "Principal",
                filtros,
                clases
        );
    }
}
