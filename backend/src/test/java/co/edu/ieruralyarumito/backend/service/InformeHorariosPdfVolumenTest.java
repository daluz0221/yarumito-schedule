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

class InformeHorariosPdfVolumenTest {

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    private final InformeHorariosPdfService pdfService =
            new InformeHorariosPdfService();

    @Test
    void generaHorarioDeUnDocente() throws Exception {

        InformeHorariosResponse informe =
                construirInforme(5);

        byte[] pdf = pdfService.exportarHorarios(informe);

        String texto = extraerTexto(pdf);

        assertTrue(texto.contains("HORARIO SEMANAL"));
        assertTrue(texto.contains("Docente-001"));
        assertTrue(texto.contains("Asignatura-005"));
        assertEquals(5, informe.totalClasesProgramadas());
    }

    @Test
    void conservaTodasLasClasesDeUnGrupo()
            throws Exception {

        InformeHorariosResponse informe =
                construirInforme(25);

        byte[] pdf = pdfService.exportarHorarios(informe);
        String texto = extraerTexto(pdf);

        for (int i = 1; i <= 25; i++) {

            assertTrue(
                    texto.contains(codigo("Asignatura", i)),
                    "No aparece la asignatura " + i
            );
        }
    }

    @Test
    void conservaRegistrosEnInformeInstitucionalExtenso()
            throws Exception {

        int cantidad = 250;

        InformeHorariosResponse informe =
                construirInforme(cantidad);

        byte[] pdf = pdfService.exportarHorarios(informe);

        try (PDDocument documento = Loader.loadPDF(pdf)) {

            assertTrue(documento.getNumberOfPages() > 1);

            String texto = new PDFTextStripper()
                    .getText(documento);

            for (int i = 1; i <= cantidad; i++) {

                assertTrue(
                        texto.contains(codigo("Asignatura", i)),
                        "Falta Asignatura-" + i
                );

                assertTrue(
                        texto.contains(codigo("Docente", i)),
                        "Falta Docente-" + i
                );
            }

            assertTrue(texto.contains("Pagina 1 de "));

            assertTrue(
                    texto.contains(
                            "Pagina "
                                    + documento.getNumberOfPages()
                                    + " de "
                                    + documento.getNumberOfPages()
                    )
            );
        }
    }

    @Test
    void numeraTodasLasPaginasDelInforme()
            throws Exception {

        byte[] pdf = pdfService.exportarHorarios(
                construirInforme(150)
        );

        try (PDDocument documento = Loader.loadPDF(pdf)) {

            int totalPaginas = documento.getNumberOfPages();

            assertTrue(totalPaginas > 1);

            PDFTextStripper extractor =
                    new PDFTextStripper();

            for (int pagina = 1;
                 pagina <= totalPaginas;
                 pagina++) {

                extractor.setStartPage(pagina);
                extractor.setEndPage(pagina);

                String textoPagina =
                        extractor.getText(documento);

                assertTrue(
                        textoPagina.contains(
                                "Pagina " + pagina
                                        + " de " + totalPaginas
                        ),
                        "La pagina " + pagina
                                + " no tiene numeracion correcta."
                );
            }
        }
    }

    @Test
    void conservaSeparacionDeJornadas()
            throws Exception {

        byte[] pdf = pdfService.exportarHorarios(
                construirInforme(40)
        );

        String texto = extraerTexto(pdf);

        assertTrue(texto.contains("REGULAR"));
        assertTrue(texto.contains("CONTRAJORNADA"));

        assertTrue(texto.contains("07:00 - 08:00"));
        assertTrue(texto.contains("14:00 - 15:00"));
    }

    private String extraerTexto(byte[] archivo)
            throws Exception {

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            return new PDFTextStripper().getText(documento);
        }
    }

    private InformeHorariosResponse construirInforme(
            int cantidad) {

        UUID anioEscolarId = UUID.randomUUID();

        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anioEscolarId,
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

            String dia = switch (i % 5) {
                case 0 -> "VIERNES";
                case 1 -> "LUNES";
                case 2 -> "MARTES";
                case 3 -> "MIERCOLES";
                default -> "JUEVES";
            };

            clases.add(
                    new InformeHorarioClaseDetalleResponse(
                            UUID.randomUUID(),
                            1,
                            "PUBLICADO",
                            UUID.randomUUID(),
                            codigo("Docente", i),
                            UUID.randomUUID(),
                            "10",
                            "10A",
                            UUID.randomUUID(),
                            codigo("Asignatura", i),
                            dia,
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
                            codigo("Aula", i),
                            i % 3 == 0
                    )
            );
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

    private String codigo(String prefijo, int numero) {

        return "%s-%03d".formatted(prefijo, numero);
    }
}
