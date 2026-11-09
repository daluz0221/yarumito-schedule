package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAsignaturaDetalleResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de exportacion PDF del informe
 * de areas y asignaturas institucionales.
 */
class InformeAreasAsignaturasPdfServiceTest {

    private final InformeAreasAsignaturasPdfService servicio =
            new InformeAreasAsignaturasPdfService();

    @Test
    void debeGenerarPdfValidoConTotales() throws IOException {

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informeEjemplo(), null
        );

        assertTrue(archivo.length > 0);

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            assertTrue(documento.getNumberOfPages() >= 1);

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("Cantidad de areas: 2"));
            assertTrue(texto.contains("Cantidad de asignaturas: 3"));
            assertTrue(texto.contains("Pagina 1 de"));
        }
    }

    @Test
    void debeConservarAreasYAsignaturas() throws IOException {

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informeEjemplo(), null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("Matematicas"));
            assertTrue(texto.contains("Ciencias"));
            assertTrue(texto.contains("Algebra"));
            assertTrue(texto.contains("Geometria"));
            assertTrue(texto.contains("Biologia"));

            assertTrue(texto.contains("MAT"));
            assertTrue(texto.contains("CIE"));
            assertTrue(texto.contains("Asignaturas: 2"));
            assertTrue(texto.contains("Asignaturas: 1"));
        }
    }

    @Test
    void debeMostrarAreasSinAsignaturas() throws IOException {

        InformeAreaDetalleResponse filosofia = crearArea(
                "Filosofia", "FIL", List.of()
        );

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        1, 0, List.of(filosofia)
                );

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informe, null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("Filosofia"));
            assertTrue(texto.contains("Cantidad de asignaturas: 0"));
            assertTrue(texto.contains("Sin asignaturas registradas"));
        }
    }

    @Test
    void debeGenerarPdfSinResultados() throws IOException {

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        0, 0, List.of()
                );

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informe, null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            assertEquals(1, documento.getNumberOfPages());

            String texto = extraerTexto(documento);

            assertTrue(texto.contains(
                    "No se encontraron areas para los filtros aplicados"
            ));
        }
    }

    @Test
    void debePaginarSinOmitirAsignaturas() throws IOException {

        List<InformeAsignaturaDetalleResponse> asignaturas =
                new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            asignaturas.add(crearAsignatura(
                    "AsignaturaPrueba" + i,
                    "ASG" + i,
                    true, false, false
            ));
        }

        InformeAreaDetalleResponse area = crearArea(
                "Area de prueba", "APR", asignaturas
        );

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        1, 100, List.of(area)
                );

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informe, null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            int paginas = documento.getNumberOfPages();

            assertTrue(paginas > 1);

            String texto = extraerTexto(documento);

            for (int i = 1; i <= 100; i++) {
                assertTrue(
                        texto.contains("AsignaturaPrueba" + i),
                        "Falta la asignatura " + i
                );
            }

            for (int pagina = 1; pagina <= paginas; pagina++) {
                PDFTextStripper extractor = new PDFTextStripper();
                extractor.setStartPage(pagina);
                extractor.setEndPage(pagina);

                String textoPagina = extractor.getText(documento);

                assertTrue(textoPagina.contains("Area de prueba"));
                assertTrue(textoPagina.contains(
                        "Pagina " + pagina + " de " + paginas
                ));
            }
        }
    }

    @Test
    void debeValidarTituloPersonalizado() throws IOException {

        String tituloValido = "T".repeat(100);

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informeEjemplo(), tituloValido
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento)
                    .replaceAll("\\s+", "");

            assertTrue(texto.contains(tituloValido));
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.exportarAreasAsignaturas(
                        informeEjemplo(), "T".repeat(101)
                )
        );
    }

    @Test
    void debePermitirGenerarDosPdfConLaMismaInstancia()
            throws IOException {

        byte[] primero = servicio.exportarAreasAsignaturas(
                informeEjemplo(), null
        );

        byte[] segundo = servicio.exportarAreasAsignaturas(
                informeEjemplo(), null
        );

        try (PDDocument documento1 = Loader.loadPDF(primero);
             PDDocument documento2 = Loader.loadPDF(segundo)) {

            assertTrue(documento1.getNumberOfPages() > 0);
            assertTrue(documento2.getNumberOfPages() > 0);

            assertTrue(extraerTexto(documento2).contains("Algebra"));
        }
    }

    private String extraerTexto(PDDocument documento)
            throws IOException {

        return new PDFTextStripper().getText(documento);
    }

    private InformeAreasAsignaturasResponse informeEjemplo() {

        InformeAreaDetalleResponse matematicas = crearArea(
                "Matematicas",
                "MAT",
                List.of(
                        crearAsignatura(
                                "Algebra", "ALG",
                                true, false, true
                        ),
                        crearAsignatura(
                                "Geometria", "GEO",
                                false, false, false
                        )
                )
        );

        InformeAreaDetalleResponse ciencias = crearArea(
                "Ciencias",
                "CIE",
                List.of(
                        crearAsignatura(
                                "Biologia", "BIO",
                                true, true, false
                        )
                )
        );

        return new InformeAreasAsignaturasResponse(
                2, 3, List.of(matematicas, ciencias)
        );
    }

    private InformeAreaDetalleResponse crearArea(
            String nombre,
            String codigo,
            List<InformeAsignaturaDetalleResponse> asignaturas) {

        return new InformeAreaDetalleResponse(
                UUID.randomUUID(),
                nombre,
                codigo,
                true,
                true,
                false,
                asignaturas.size(),
                asignaturas
        );
    }

    private InformeAsignaturaDetalleResponse crearAsignatura(
            String nombre,
            String codigo,
            boolean activa,
            boolean mediaTecnica,
            boolean idoneidadEstricta) {

        return new InformeAsignaturaDetalleResponse(
                UUID.randomUUID(),
                nombre,
                codigo,
                activa,
                mediaTecnica,
                idoneidadEstricta,
                false
        );
    }

    @Test
    void debeConservarCaracteresEspanolesEnAreasYAsignaturas()
            throws IOException {

        InformeAreaDetalleResponse educacionArtistica = crearArea(
                "Educación Artística",
                "ART",
                List.of(
                        crearAsignatura(
                                "Diseño y expresión",
                                "DIS",
                                true,
                                false,
                                false
                        )
                )
        );

        InformeAreaDetalleResponse matematicas = crearArea(
                "Matemáticas",
                "MAT",
                List.of(
                        crearAsignatura(
                                "Geometría",
                                "GEO",
                                true,
                                false,
                                false
                        )
                )
        );

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        2,
                        2,
                        List.of(educacionArtistica, matematicas)
                );

        byte[] archivo = servicio.exportarAreasAsignaturas(
                informe,
                "Relación de áreas y asignaturas"
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("Educación Artística"));
            assertTrue(texto.contains("Diseño y expresión"));
            assertTrue(texto.contains("Matemáticas"));
            assertTrue(texto.contains("Geometría"));
            assertTrue(texto.contains("Relación de áreas y asignaturas"));
        }
    }
}
