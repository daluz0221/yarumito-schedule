package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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
 * Pruebas unitarias del generador PDF
 * del informe de planta docente.
 *
 * No requiere Spring Boot ni PostgreSQL.
 */
class InformePdfServiceTest {

    private final InformePdfService servicio =
            new InformePdfService();

    @Test
    void debeGenerarPdfValido() throws IOException {

        byte[] archivo = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            assertTrue(documento.getNumberOfPages() >= 1);

            PDRectangle pagina =
                    documento.getPage(0).getMediaBox();

            assertTrue(pagina.getWidth() > pagina.getHeight());
        }
    }

    @Test
    void debeIncluirTituloTotalesYEncabezados()
            throws IOException {

        byte[] archivo = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains(
                    "Informe de planta docente y capacidad academica"
            ));

            assertTrue(texto.contains(
                    "Cantidad de docentes: 2"
            ));

            assertTrue(texto.contains(
                    "Horas semanales contratadas: 42"
            ));

            assertTrue(texto.contains("Docente"));
            assertTrue(texto.contains("Area de nombramiento"));
            assertTrue(texto.contains("Vinculacion"));
        }
    }

    @Test
    void debeConservarLosDatosDeTodosLosDocentes()
            throws IOException {

        byte[] archivo = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("Ana Perez"));
            assertTrue(texto.contains("Carlos Gomez"));

            assertTrue(texto.contains("Matematicas"));
            assertTrue(texto.contains("Ciencias"));

            assertTrue(texto.contains("ACTIVO"));
            assertTrue(texto.contains("PLANTA"));
            assertTrue(texto.contains("PROVISIONAL"));
        }
    }

    @Test
    void debePaginarSinOmitirDocentes()
            throws IOException {

        int cantidad = 100;

        List<InformePlantaDocenteDetalleResponse> docentes =
                new ArrayList<>();

        for (int i = 1; i <= cantidad; i++) {

            docentes.add(
                    new InformePlantaDocenteDetalleResponse(
                            UUID.randomUUID(),
                            "DocentePrueba" + i,
                            "Matematicas",
                            EstadoDocente.ACTIVO,
                            TipoVinculacion.PLANTA,
                            22
                    )
            );
        }

        InformePlantaDocenteResponse informe =
                new InformePlantaDocenteResponse(
                        cantidad,
                        2200,
                        docentes
                );

        byte[] archivo = servicio.exportarPlantaDocente(
                informe,
                "Informe de prueba de paginacion"
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            int totalPaginas = documento.getNumberOfPages();

            assertTrue(
                    totalPaginas > 1,
                    "El informe debe ocupar varias paginas"
            );

            String textoCompleto = extraerTexto(documento);

            for (int i = 1; i <= cantidad; i++) {

                assertTrue(
                        textoCompleto.contains(
                                "DocentePrueba" + i
                        ),
                        "Falta el docente " + i
                );
            }

            for (int pagina = 1;
                 pagina <= totalPaginas;
                 pagina++) {

                String textoPagina = extraerPagina(
                        documento,
                        pagina
                );

                assertTrue(
                        textoPagina.contains("Docente"),
                        "Falta el encabezado en pagina " + pagina
                );

                assertTrue(
                        textoPagina.contains(
                                "Pagina " + pagina
                                        + " de " + totalPaginas
                        ),
                        "Falta la numeracion de pagina "
                                + pagina
                );
            }
        }
    }

    @Test
    void debeAdmitirTituloDeCienCaracteres()
            throws IOException {

        String titulo = "T".repeat(100);

        byte[] archivo = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                titulo
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento)
                    .replaceAll("\\s+", "");

            assertTrue(texto.contains(titulo));
        }
    }

    @Test
    void debeRechazarTituloSuperiorACienCaracteres() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.exportarPlantaDocente(
                        informeConDosDocentes(),
                        "T".repeat(101)
                )
        );
    }

    @Test
    void debeGenerarPdfSinDocentes()
            throws IOException {

        InformePlantaDocenteResponse vacio =
                new InformePlantaDocenteResponse(
                        0,
                        0,
                        List.of()
                );

        byte[] archivo = servicio.exportarPlantaDocente(
                vacio,
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertEquals(1, documento.getNumberOfPages());

            assertTrue(texto.contains(
                    "Cantidad de docentes: 0"
            ));

            assertTrue(texto.contains(
                    "Horas semanales contratadas: 0"
            ));

            assertTrue(texto.contains(
                    "No se encontraron docentes"
            ));
        }
    }

    private String extraerTexto(PDDocument documento)
            throws IOException {

        return new PDFTextStripper().getText(documento);
    }

    private String extraerPagina(
            PDDocument documento,
            int numeroPagina) throws IOException {

        PDFTextStripper extractor = new PDFTextStripper();

        extractor.setStartPage(numeroPagina);
        extractor.setEndPage(numeroPagina);

        return extractor.getText(documento);
    }

    private InformePlantaDocenteResponse informeConDosDocentes() {

        InformePlantaDocenteDetalleResponse ana =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        "Ana Perez",
                        "Matematicas",
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA,
                        22
                );

        InformePlantaDocenteDetalleResponse carlos =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        "Carlos Gomez",
                        "Ciencias",
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PROVISIONAL,
                        20
                );

        return new InformePlantaDocenteResponse(
                2,
                42,
                List.of(ana, carlos)
        );
    }

    @Test
    void debeConservarTildesYEnesEnElPdf() throws IOException {

        InformePlantaDocenteDetalleResponse docente =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        "María Muñoz",
                        "Matemáticas y Educación",
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA,
                        22
                );

        InformePlantaDocenteResponse informe =
                new InformePlantaDocenteResponse(
                        1,
                        22,
                        List.of(docente)
                );

        byte[] archivo = servicio.exportarPlantaDocente(
                informe,
                "Relación de docentes"
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(
                    texto.contains("María Muñoz"),
                    "El PDF debe conservar el nombre con tildes y ñ"
            );

            assertTrue(
                    texto.contains("Matemáticas y Educación"),
                    "El PDF debe conservar los nombres de áreas"
            );

            assertTrue(
                    texto.contains("Relación de docentes"),
                    "El título personalizado debe conservar la tilde"
            );
        }
    }
}
