package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas del hallazgo A2.
 *
 * Comprueba la presentación de metadatos
 * en los seis formatos de exportación:
 *
 * 1. Planta docente Excel.
 * 2. Áreas y asignaturas Excel.
 * 3. Perfil profesional Excel.
 * 4. Planta docente PDF.
 * 5. Áreas y asignaturas PDF.
 * 6. Perfil profesional PDF.
 *
 * No requiere base de datos, Docker ni autenticación.
 * Los valores utilizados son datos de prueba.
 */
class MetadatosInformeExportacionTest {

    private final InformeExcelService excel =
            new InformeExcelService();

    private final InformePdfService pdfPlanta =
            new InformePdfService();

    private final InformeAreasAsignaturasPdfService pdfAreas =
            new InformeAreasAsignaturasPdfService();

    private final InformePerfilProfesionalPdfService pdfPerfil =
            new InformePerfilProfesionalPdfService();

    private MetadatosInformeResponse metadatosEjemplo() {

        return new MetadatosInformeResponse(
                "Institución de Prueba",
                "Sede Principal",
                "SEDE-001",
                Map.of(
                        "Estado", "ACTIVO",
                        "Área", "Humanidades"
                ),
                LocalDateTime.of(
                        2026, 10, 12,
                        14, 30, 0
                ),
                "Usuario de Prueba"
        );
    }

    private InformePlantaDocenteResponse plantaVacia() {

        return new InformePlantaDocenteResponse(
                0,
                0,
                List.of()
        );
    }

    private InformeAreasAsignaturasResponse areasVacias() {

        return new InformeAreasAsignaturasResponse(
                0,
                0,
                List.of()
        );
    }

    private InformePerfilProfesionalResponse perfilVacio() {

        return new InformePerfilProfesionalResponse(
                LocalDate.of(2026, 10, 12),
                0,
                0,
                0,
                List.of()
        );
    }

    /**
     * A2 - Excel de planta docente.
     */
    @Test
    void debeIncluirMetadatosEnExcelPlantaDocente()
            throws IOException {

        byte[] archivo = excel.exportarPlantaDocente(
                plantaVacia(),
                null,
                metadatosEjemplo()
        );

        try (XSSFWorkbook libro = abrirExcel(archivo)) {

            assertEquals(
                    1,
                    libro.getNumberOfSheets()
            );

            verificarHojaExcel(
                    libro,
                    libro.getSheetAt(0)
            );
        }
    }

    /**
     * A2 - Excel de áreas y asignaturas.
     */
    @Test
    void debeIncluirMetadatosEnExcelAreasAsignaturas()
            throws IOException {

        byte[] archivo = excel.exportarAreasAsignaturas(
                areasVacias(),
                null,
                metadatosEjemplo()
        );

        try (XSSFWorkbook libro = abrirExcel(archivo)) {

            assertEquals(
                    1,
                    libro.getNumberOfSheets()
            );

            verificarHojaExcel(
                    libro,
                    libro.getSheetAt(0)
            );
        }
    }

    /**
     * A2 - Excel de perfil profesional.
     *
     * Ambas hojas deben tener sus metadatos.
     * No debe crearse una hoja anexa adicional.
     */
    @Test
    void debeIncluirMetadatosEnAmbasHojasExcelPerfil()
            throws IOException {

        byte[] archivo = excel.exportarPerfilProfesional(
                perfilVacio(),
                null,
                metadatosEjemplo()
        );

        try (XSSFWorkbook libro = abrirExcel(archivo)) {

            assertEquals(
                    2,
                    libro.getNumberOfSheets(),
                    "No debe aparecer una hoja anexa de metadatos"
            );

            verificarHojaExcel(
                    libro,
                    libro.getSheetAt(0)
            );

            verificarHojaExcel(
                    libro,
                    libro.getSheetAt(1)
            );
        }
    }

    /**
     * A2 - PDF de planta docente.
     */
    @Test
    void debeIncluirMetadatosAlFinalPdfPlanta()
            throws IOException {

        byte[] archivo = pdfPlanta.exportarPlantaDocente(
                plantaVacia(),
                null,
                metadatosEjemplo()
        );

        verificarPdf(archivo);
    }

    /**
     * A2 - PDF de áreas y asignaturas.
     */
    @Test
    void debeIncluirMetadatosAlFinalPdfAreas()
            throws IOException {

        byte[] archivo = pdfAreas.exportarAreasAsignaturas(
                areasVacias(),
                null,
                metadatosEjemplo()
        );

        verificarPdf(archivo);
    }

    /**
     * A2 - PDF de perfil profesional.
     */
    @Test
    void debeIncluirMetadatosAlFinalPdfPerfil()
            throws IOException {

        byte[] archivo = pdfPerfil.exportarPerfilProfesional(
                perfilVacio(),
                null,
                metadatosEjemplo()
        );

        verificarPdf(archivo);
    }

    /**
     * Comprueba que la nota está en la misma hoja,
     * que contiene los campos previstos y que
     * está comprendida en el área de impresión.
     */
    private void verificarHojaExcel(
            XSSFWorkbook libro,
            Sheet hoja) {

        String texto = obtenerTextoHoja(hoja);

        verificarContenidoMetadatos(texto);

        int ultimaFila = hoja.getLastRowNum();

        assertTrue(
                ultimaFila > 5,
                "Los metadatos deben aparecer después de la tabla"
        );

        Row filaFinal = hoja.getRow(ultimaFila);

        assertNotNull(
                filaFinal,
                "Debe existir la última fila de metadatos"
        );

        assertNotNull(
                filaFinal.getCell(0),
                "La nota debe estar alineada al margen izquierdo"
        );

        assertTrue(
                filaFinal.getCell(0)
                        .getStringCellValue()
                        .contains("Generado por: Usuario de Prueba"),
                "La identidad debe aparecer al final de la hoja"
        );

        assertEquals(
                org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT,
                filaFinal.getCell(0)
                        .getCellStyle()
                        .getAlignment(),
                "La nota debe estar alineada a la izquierda"
        );

        assertTrue(
                filaFinal.getCell(0)
                        .getCellStyle()
                        .getFontIndex() >= 0,
                "La nota debe tener un estilo definido"
        );

        int indiceHoja = libro.getSheetIndex(hoja);

        String areaImpresion =
                libro.getPrintArea(indiceHoja);

        assertNotNull(
                areaImpresion,
                "El Excel debe configurar un área de impresión"
        );

        assertTrue(
                areaImpresion.contains(
                        "$" + (ultimaFila + 1)
                ),
                "El área de impresión debe incluir "
                        + "la última fila de metadatos. Área: "
                        + areaImpresion
        );
    }

    /**
     * Comprueba que el PDF contiene los metadatos
     * y que estos aparecen en su última página.
     */
    private void verificarPdf(byte[] archivo)
            throws IOException {

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            int totalPaginas =
                    documento.getNumberOfPages();

            assertTrue(
                    totalPaginas >= 1,
                    "El PDF debe tener al menos una página"
            );

            PDFTextStripper extractor =
                    new PDFTextStripper();

            String textoCompleto =
                    extractor.getText(documento);

            verificarContenidoMetadatos(textoCompleto);

            extractor.setStartPage(totalPaginas);
            extractor.setEndPage(totalPaginas);

            String ultimaPagina =
                    extractor.getText(documento);

            verificarContenidoMetadatos(ultimaPagina);
        }
    }

    /**
     * Valida los campos suministrados por HU-04
     * sin consultar servicios externos.
     */
    private void verificarContenidoMetadatos(
            String texto) {

        assertTrue(
                texto.contains(
                        "Institución: Institución de Prueba"
                ),
                "Falta la identificación institucional"
        );

        assertTrue(
                texto.contains(
                        "Sede: Sede Principal"
                ),
                "Falta la sede"
        );

        assertTrue(
                texto.contains(
                        "Código de sede: SEDE-001"
                ),
                "Falta el código de sede"
        );

        assertTrue(
                texto.contains(
                        "Filtro Estado: ACTIVO"
                ),
                "Falta el filtro de estado"
        );

        assertTrue(
                texto.contains(
                        "Filtro Área: Humanidades"
                ),
                "Falta el filtro de área"
        );

        assertTrue(
                texto.contains(
                        "Fecha de generación: 2026-10-12 14:30:00"
                ),
                "Falta la fecha y hora de generación"
        );

        assertTrue(
                texto.contains(
                        "Generado por: Usuario de Prueba"
                ),
                "Falta la identidad del generador"
        );
    }

    private XSSFWorkbook abrirExcel(
            byte[] archivo) throws IOException {

        return new XSSFWorkbook(
                new ByteArrayInputStream(archivo)
        );
    }

    private String obtenerTextoHoja(Sheet hoja) {

        StringBuilder texto = new StringBuilder();

        for (Row fila : hoja) {

            for (org.apache.poi.ss.usermodel.Cell celda
                    : fila) {

                if (celda.getCellType()
                        == org.apache.poi.ss.usermodel.CellType.STRING) {

                    texto.append(
                            celda.getStringCellValue()
                    );

                    texto.append('\n');
                }
            }
        }

        return texto.toString();
    }
}
