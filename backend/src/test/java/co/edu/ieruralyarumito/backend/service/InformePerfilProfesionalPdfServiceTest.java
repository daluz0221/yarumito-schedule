package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeIdoneidadDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeTituloProfesionalDetalleResponse;
import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas del PDF de perfiles profesionales
 * e idoneidades docentes.
 *
 * Verifica:
 * - Generación del archivo PDF.
 * - Totales y fecha de referencia.
 * - Integridad de títulos e idoneidades.
 * - Vigencias y valores opcionales.
 * - Informes sin resultados.
 * - Paginación.
 * - Títulos personalizados.
 * - Generación consecutiva.
 * - Caracteres españoles.
 * - Separación del anexo técnico.
 *
 * No requiere Spring Boot ni PostgreSQL.
 */
class InformePerfilProfesionalPdfServiceTest {

    private final InformePerfilProfesionalPdfService servicio =
            new InformePerfilProfesionalPdfService();

    private static final LocalDate FECHA_REFERENCIA =
            LocalDate.of(2026, 10, 11);

    @Test
    void debeGenerarDocumentoPdfValido() throws IOException {

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                null
        );

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            assertTrue(documento.getNumberOfPages() >= 1);

            PDRectangle pagina =
                    documento.getPage(0).getMediaBox();

            assertTrue(
                    pagina.getWidth() > pagina.getHeight(),
                    "El PDF debe tener orientación horizontal"
            );

            assertTrue(
                    extraerTexto(documento).contains(
                            "Informe de perfiles profesionales"
                    )
            );
        }
    }

    @Test
    void debeConservarFechaReferenciaYTotales()
            throws IOException {

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains(
                    "Fecha de referencia: 2026-10-11"
            ));

            assertTrue(texto.contains(
                    "Cantidad de docentes: 1"
            ));

            assertTrue(texto.contains(
                    "Cantidad de títulos: 2"
            ));

            assertTrue(texto.contains(
                    "Cantidad de idoneidades: 3"
            ));
        }
    }

    @Test
    void debeConservarTitulosSinMultiplicarlos()
            throws IOException {

        InformePerfilProfesionalResponse informe =
                informeEjemplo();

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe,
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertEquals(
                    1,
                    contarApariciones(
                            texto,
                            "Licenciatura en Literatura"
                    ),
                    "La licenciatura no debe duplicarse"
            );

            assertEquals(
                    1,
                    contarApariciones(
                            texto,
                            "Maestría en Educación"
                    ),
                    "La maestría no debe duplicarse"
            );

            for (InformeTituloProfesionalDetalleResponse titulo
                    : informe.docentes().get(0).titulos()) {

                assertTrue(
                        texto.contains(titulo.tituloId().toString()),
                        "Falta el identificador del título "
                                + titulo.tituloId()
                );
            }
        }
    }

    @Test
    void debeConservarIdoneidadesYVigencias()
            throws IOException {

        InformePerfilProfesionalResponse informe =
                informeEjemplo();

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe,
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("PRINCIPAL"));
            assertTrue(texto.contains("AUTORIZADA"));
            assertTrue(texto.contains("EXCEPCIONAL"));

            assertTrue(texto.contains("2025-01-01"));
            assertTrue(texto.contains("2027-01-01"));
            assertTrue(texto.contains("2025-12-31"));

            assertTrue(texto.contains("Literatura"));
            assertTrue(texto.contains("No aplica"));

            for (InformeIdoneidadDetalleResponse idoneidad
                    : informe.docentes().get(0).idoneidades()) {

                assertTrue(
                        texto.contains(
                                idoneidad.idoneidadId().toString()
                        ),
                        "Falta la idoneidad "
                                + idoneidad.idoneidadId()
                );

                assertTrue(
                        texto.contains(
                                idoneidad.areaId().toString()
                        ),
                        "Falta el identificador del área "
                                + idoneidad.areaId()
                );
            }
        }
    }


    @Test
    void debeIdentificarDocenteEnContinuacionesDeIdoneidades()
            throws IOException {

        List<InformeIdoneidadDetalleResponse> idoneidades =
                new ArrayList<>();

        for (int i = 1; i <= 100; i++) {

            idoneidades.add(
                    new InformeIdoneidadDetalleResponse(
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            String.format("AreaPrueba%03d", i),
                            null,
                            null,
                            TipoIdoneidad.PRINCIPAL,
                            null,
                            LocalDate.of(2025, 1, 1),
                            null,
                            true
                    )
            );
        }

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        UUID.randomUUID(),
                        "María Pérez",
                        "Humanidades",
                        List.of(),
                        idoneidades
                );

        var informe = new InformePerfilProfesionalResponse(
                FECHA_REFERENCIA,
                1,
                0,
                100,
                List.of(docente)
        );

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe, null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            boolean encontroContinuacion = false;

            for (int pagina = 1;
                 pagina <= documento.getNumberOfPages();
                 pagina++) {

                String textoPagina =
                        extraerPagina(documento, pagina);

                if (textoPagina.contains(
                        "Idoneidades (continuación)")) {

                    encontroContinuacion = true;

                    assertTrue(
                            textoPagina.contains("Docente: María Pérez"),
                            "Falta el docente en la página " + pagina
                    );

                    assertTrue(
                            textoPagina.contains(
                                    "Área de nombramiento: Humanidades"),
                            "Falta el área en la página " + pagina
                    );
                }
            }

            assertTrue(
                    encontroContinuacion,
                    "La prueba debe provocar al menos una continuación"
            );

            String textoCompleto = extraerTexto(documento);

            for (int i = 1; i <= 100; i++) {

                assertEquals(
                        1,
                        contarApariciones(
                                textoCompleto,
                                String.format("AreaPrueba%03d", i)
                        ),
                        "Idoneidad duplicada u omitida: " + i
                );
            }
        }
    }

    @Test
    void debeGenerarInformeVacio() throws IOException {

        InformePerfilProfesionalResponse vacio =
                new InformePerfilProfesionalResponse(
                        FECHA_REFERENCIA,
                        0,
                        0,
                        0,
                        List.of()
                );

        byte[] archivo = servicio.exportarPerfilProfesional(
                vacio,
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            assertEquals(
                    1,
                    documento.getNumberOfPages(),
                    "Un informe vacío debe ocupar una página"
            );

            String texto = extraerTexto(documento);

            assertTrue(texto.contains(
                    "Cantidad de docentes: 0"
            ));

            assertTrue(texto.contains(
                    "Cantidad de títulos: 0"
            ));

            assertTrue(texto.contains(
                    "Cantidad de idoneidades: 0"
            ));

            assertTrue(texto.contains(
                    "No se encontraron docentes"
            ));
        }
    }

    @Test
    void debePaginarSinPerderTitulos() throws IOException {

        UUID docenteId = UUID.randomUUID();

        List<InformeTituloProfesionalDetalleResponse> titulos =
                new ArrayList<>();

        for (int i = 1; i <= 80; i++) {

            titulos.add(
                    new InformeTituloProfesionalDetalleResponse(
                            UUID.randomUUID(),
                            String.format("TituloPrueba%03d", i),
                            NivelTituloProfesional.LICENCIATURA,
                            "Universidad de Prueba",
                            2020
                    )
            );
        }

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        docenteId,
                        "Docente de Paginación",
                        "Humanidades",
                        titulos,
                        List.of()
                );

        InformePerfilProfesionalResponse informe =
                new InformePerfilProfesionalResponse(
                        FECHA_REFERENCIA,
                        1,
                        80,
                        0,
                        List.of(docente)
                );

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe,
                null
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            int totalPaginas = documento.getNumberOfPages();

            assertTrue(
                    totalPaginas > 1,
                    "El documento debe tener varias páginas"
            );

            String texto = extraerTexto(documento);

            for (int i = 1; i <= 80; i++) {

                String nombre = String.format(
                        "TituloPrueba%03d", i
                );

                assertEquals(
                        1,
                        contarApariciones(texto, nombre),
                        "Título duplicado u omitido: " + nombre
                );
            }

            for (int i = 1; i <= totalPaginas; i++) {

                String textoPagina =
                        extraerPagina(documento, i);

                if (textoPagina.contains(
                        "Títulos profesionales (continuación)")) {

                    assertTrue(
                            textoPagina.contains(
                                    "Docente: Docente de Paginación"),
                            "Falta identificar al docente en la página " + i
                    );

                    assertTrue(
                            textoPagina.contains(
                                    "Área de nombramiento: Humanidades"),
                            "Falta el área del docente en la página " + i
                    );
                }

                assertTrue(
                        textoPagina.contains(
                                "Página " + i
                                        + " de " + totalPaginas
                        ),
                        "Falta la numeración en la página " + i
                );
            }
        }
    }

    @Test
    void debeValidarTituloPersonalizado() throws IOException {

        String tituloValido = "T".repeat(100);

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                tituloValido
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento)
                    .replaceAll("\\s+", "");

            assertTrue(
                    texto.contains(tituloValido),
                    "Debe conservar el título de 100 caracteres"
            );
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.exportarPerfilProfesional(
                        informeEjemplo(),
                        "T".repeat(101)
                )
        );
    }

    @Test
    void debeGenerarDosPdfConLaMismaInstancia()
            throws IOException {

        byte[] primero = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                null
        );

        byte[] segundo = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                null
        );

        try (PDDocument documento1 = Loader.loadPDF(primero);
             PDDocument documento2 = Loader.loadPDF(segundo)) {

            assertTrue(documento1.getNumberOfPages() > 0);
            assertTrue(documento2.getNumberOfPages() > 0);

            assertTrue(
                    extraerTexto(documento1).contains(
                            "Títulos profesionales"
                    )
            );

            assertTrue(
                    extraerTexto(documento2).contains(
                            "Idoneidades"
                    )
            );

            assertTrue(
                    extraerTexto(documento2).contains(
                            "ANEXO TÉCNICO"
                    )
            );
        }
    }

    @Test
    void debeConservarCaracteresEspanolesEnPerfilProfesional()
            throws IOException {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID();

        InformeTituloProfesionalDetalleResponse titulo =
                new InformeTituloProfesionalDetalleResponse(
                        tituloId,
                        "Licenciatura en Educación",
                        NivelTituloProfesional.LICENCIATURA,
                        "Universidad de Antioquia",
                        2024
                );

        InformeIdoneidadDetalleResponse idoneidad =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaId,
                        "Ciencias Sociales",
                        asignaturaId,
                        "Geografía",
                        TipoIdoneidad.PRINCIPAL,
                        tituloId,
                        LocalDate.of(2025, 1, 1),
                        null,
                        true
                );

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        docenteId,
                        "María Muñoz",
                        "Educación Artística",
                        List.of(titulo),
                        List.of(idoneidad)
                );

        InformePerfilProfesionalResponse informe =
                new InformePerfilProfesionalResponse(
                        FECHA_REFERENCIA,
                        1,
                        1,
                        1,
                        List.of(docente)
                );

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe,
                "Relación de títulos e idoneidades"
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            String texto = extraerTexto(documento);

            assertTrue(texto.contains("María Muñoz"));

            assertTrue(texto.contains(
                    "Licenciatura en Educación"
            ));

            assertTrue(texto.contains(
                    "Educación Artística"
            ));

            assertTrue(texto.contains("Geografía"));

            assertTrue(texto.contains(
                    "Relación de títulos e idoneidades"
            ));
        }
    }

    @Test
    void debeSepararInformeAdministrativoDelAnexoTecnico()
            throws IOException {

        InformePerfilProfesionalResponse informe =
                informeEjemplo();

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe,
                "Informe de perfiles profesionales"
        );

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            int totalPaginas = documento.getNumberOfPages();

            assertTrue(
                    totalPaginas >= 2,
                    "El anexo debe comenzar en otra página"
            );

            int primeraPaginaAnexo = -1;

            for (int pagina = 1;
                 pagina <= totalPaginas;
                 pagina++) {

                String textoPagina =
                        extraerPagina(documento, pagina);

                if (textoPagina.contains(
                        "ANEXO TÉCNICO - IDENTIFICADORES DE REGISTROS"
                )) {
                    primeraPaginaAnexo = pagina;
                    break;
                }
            }

            assertTrue(
                    primeraPaginaAnexo > 1,
                    "El anexo debe comenzar después del informe"
            );

            String textoAdministrativo = extraerRango(
                    documento,
                    1,
                    primeraPaginaAnexo - 1
            );

            String textoAnexo = extraerRango(
                    documento,
                    primeraPaginaAnexo,
                    totalPaginas
            );

            var docente = informe.docentes().get(0);

            assertTrue(
                    textoAdministrativo.contains(
                            docente.nombreCompleto()
                    )
            );

            assertTrue(
                    textoAdministrativo.contains(
                            "Licenciatura en Literatura"
                    )
            );

            assertTrue(
                    textoAdministrativo.contains(
                            "Maestría en Educación"
                    )
            );

            assertFalse(
                    textoAdministrativo.contains(
                            docente.docenteId().toString()
                    ),
                    "El UUID del docente no debe aparecer "
                            + "en el informe administrativo"
            );

            assertTrue(
                    textoAnexo.contains(
                            docente.docenteId().toString()
                    ),
                    "Falta el UUID del docente en el anexo"
            );

            for (InformeTituloProfesionalDetalleResponse titulo
                    : docente.titulos()) {

                String id = titulo.tituloId().toString();

                assertFalse(
                        textoAdministrativo.contains(id),
                        "El UUID del título aparece "
                                + "en el informe administrativo"
                );

                assertTrue(
                        textoAnexo.contains(id),
                        "Falta el UUID del título en el anexo"
                );
            }

            for (InformeIdoneidadDetalleResponse idoneidad
                    : docente.idoneidades()) {

                String id =
                        idoneidad.idoneidadId().toString();

                assertFalse(
                        textoAdministrativo.contains(id),
                        "El UUID de la idoneidad aparece "
                                + "en el informe administrativo"
                );

                assertTrue(
                        textoAnexo.contains(id),
                        "Falta el UUID de la idoneidad"
                );

                assertTrue(
                        textoAnexo.contains(
                                idoneidad.areaId().toString()
                        ),
                        "Falta el UUID del área"
                );

                if (idoneidad.asignaturaId() != null) {

                    assertTrue(
                            textoAnexo.contains(
                                    idoneidad.asignaturaId().toString()
                            ),
                            "Falta el UUID de la asignatura"
                    );
                }

                if (idoneidad.tituloSoporteId() != null) {

                    assertTrue(
                            textoAnexo.contains(
                                    idoneidad.tituloSoporteId().toString()
                            ),
                            "Falta el UUID del título de soporte"
                    );
                }
            }
        }
    }

    private int contarApariciones(
            String texto,
            String buscado) {

        int apariciones = 0;
        int posicion = 0;

        while ((posicion = texto.indexOf(
                buscado, posicion)) >= 0) {

            apariciones++;
            posicion += buscado.length();
        }

        return apariciones;
    }

    private String extraerTexto(PDDocument documento)
            throws IOException {

        return new PDFTextStripper().getText(documento);
    }

    private String extraerPagina(
            PDDocument documento,
            int numeroPagina) throws IOException {

        return extraerRango(
                documento,
                numeroPagina,
                numeroPagina
        );
    }

    private String extraerRango(
            PDDocument documento,
            int paginaInicial,
            int paginaFinal) throws IOException {

        PDFTextStripper extractor =
                new PDFTextStripper();

        extractor.setStartPage(paginaInicial);
        extractor.setEndPage(paginaFinal);

        return extractor.getText(documento);
    }

    private InformePerfilProfesionalResponse informeEjemplo() {

        UUID docenteId = UUID.randomUUID();

        UUID areaHumanidadesId = UUID.randomUUID();
        UUID areaSocialesId = UUID.randomUUID();

        UUID licenciaturaId = UUID.randomUUID();

        InformeTituloProfesionalDetalleResponse licenciatura =
                new InformeTituloProfesionalDetalleResponse(
                        licenciaturaId,
                        "Licenciatura en Literatura",
                        NivelTituloProfesional.LICENCIATURA,
                        "Universidad de Prueba",
                        2020
                );

        InformeTituloProfesionalDetalleResponse maestria =
                new InformeTituloProfesionalDetalleResponse(
                        UUID.randomUUID(),
                        "Maestría en Educación",
                        NivelTituloProfesional.MAESTRIA,
                        "Universidad de Prueba",
                        2024
                );

        InformeIdoneidadDetalleResponse vigente =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaHumanidadesId,
                        "Humanidades",
                        UUID.randomUUID(),
                        "Literatura",
                        TipoIdoneidad.PRINCIPAL,
                        licenciaturaId,
                        LocalDate.of(2025, 1, 1),
                        null,
                        true
                );

        InformeIdoneidadDetalleResponse futura =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaSocialesId,
                        "Ciencias Sociales",
                        null,
                        null,
                        TipoIdoneidad.AUTORIZADA,
                        null,
                        LocalDate.of(2027, 1, 1),
                        null,
                        false
                );

        InformeIdoneidadDetalleResponse vencida =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaSocialesId,
                        "Ciencias Sociales",
                        null,
                        null,
                        TipoIdoneidad.EXCEPCIONAL,
                        null,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2025, 12, 31),
                        false
                );

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        docenteId,
                        "Juan Pérez",
                        "Humanidades",
                        List.of(licenciatura, maestria),
                        List.of(vigente, futura, vencida)
                );

        return new InformePerfilProfesionalResponse(
                FECHA_REFERENCIA,
                1,
                2,
                3,
                List.of(docente)
        );
    }
}
