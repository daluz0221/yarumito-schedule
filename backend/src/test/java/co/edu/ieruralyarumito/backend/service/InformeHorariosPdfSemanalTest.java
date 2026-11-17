package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosPdfSemanalTest {

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    @Test
    void incluyeCuadriculaSemanalYDetalle() throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                construirInforme(2)
        );

        String texto = extraerTexto(archivo);

        assertTrue(texto.contains("HORARIO SEMANAL"));
        assertTrue(texto.contains("Lunes"));
        assertTrue(texto.contains("Martes"));
        assertTrue(texto.contains("Miercoles"));
        assertTrue(texto.contains("Jueves"));
        assertTrue(texto.contains("Viernes"));

        assertTrue(texto.contains("DETALLE DE CLASES"));
        assertTrue(texto.contains("RESUMEN DE CARGA"));
    }

    @Test
    void conservaLasHorasExactasYLasJornadas()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                construirInforme(2)
        );

        String texto = extraerTexto(archivo);

        assertTrue(texto.contains("07:00 - 08:00"));
        assertTrue(texto.contains("14:00 - 15:00"));

        assertTrue(texto.contains("REGULAR"));
        assertTrue(texto.contains("CONTRAJORNADA"));
    }

    @Test
    void conservaClasesSimultaneas()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                construirInforme(4)
        );

        String texto = extraerTexto(archivo);

        assertTrue(texto.contains("Asignatura 1"));
        assertTrue(texto.contains("Asignatura 2"));
        assertTrue(texto.contains("Asignatura 3"));
        assertTrue(texto.contains("Asignatura 4"));
    }

    @Test
    void continuaElHorarioEnVariasPaginas()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                construirInforme(100)
        );

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            assertTrue(
                    documento.getNumberOfPages() > 1,
                    "El PDF debe generar varias paginas."
            );

            PDFTextStripper extractor =
                    new PDFTextStripper();

            String texto = extractor.getText(documento);

            assertTrue(
                    texto.contains("Pagina 1 de "
                            + documento.getNumberOfPages())
            );

            assertTrue(
                    texto.contains("Pagina "
                            + documento.getNumberOfPages()
                            + " de "
                            + documento.getNumberOfPages())
            );

            assertTrue(texto.contains("Asignatura 100"));
        }
    }

    @Test
    void utilizaFormatoCartaHorizontal()
            throws Exception {

        byte[] archivo = pdfService.exportarHorarios(
                construirInforme(2)
        );

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            assertEquals(
                    792f,
                    documento.getPage(0)
                            .getMediaBox().getWidth(),
                    0.1f
            );

            assertEquals(
                    612f,
                    documento.getPage(0)
                            .getMediaBox().getHeight(),
                    0.1f
            );
        }
    }

    private String extraerTexto(byte[] archivo)
            throws Exception {

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            return new PDFTextStripper()
                    .getText(documento);
        }
    }

    private InformeHorariosResponse construirInforme(
            int cantidad) {

        UUID anioEscolar = UUID.randomUUID();

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anioEscolar,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "TODAS",
                        null,
                        "PUBLICADO"
                );

        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        for (int i = 1; i <= cantidad; i++) {

            boolean contrajornada = i % 2 == 0;

            clases.add(
                    new InformeHorarioClaseDetalleResponse(
                            UUID.randomUUID(),
                            1,
                            "PUBLICADO",
                            UUID.randomUUID(),
                            "Docente " + i,
                            UUID.randomUUID(),
                            "10",
                            "Grupo " + i,
                            UUID.randomUUID(),
                            "Asignatura " + i,
                            contrajornada
                                    ? "MARTES"
                                    : "LUNES",
                            contrajornada
                                    ? LocalTime.of(14, 0)
                                    : LocalTime.of(7, 0),
                            contrajornada
                                    ? LocalTime.of(15, 0)
                                    : LocalTime.of(8, 0),
                            contrajornada
                                    ? "CONTRAJORNADA"
                                    : "REGULAR",
                            UUID.randomUUID(),
                            "Aula " + i,
                            i % 3 == 0
                    )
            );
        }

        return informeService.construirInforme(
                anioEscolar,
                "2026",
                "I.E. Rural Yarumito",
                "Principal",
                filtros,
                clases
        );
    }
}
