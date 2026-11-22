package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-04 - Mejora de Planta Docente.
 *
 * Comprueba la exportacion de carga academica
 * en PDF y Excel.
 *
 * Verifica tambien la compatibilidad del
 * informe original sin año escolar.
 *
 * No utiliza base de datos.
 */
class InformePlantaDocenteExportacionCargaTest {

    private final InformePdfService pdfService =
            new InformePdfService();

    private final InformeExcelService excelService =
            new InformeExcelService();

    /**
     * Verifica que el PDF incluya el año escolar
     * y los totales de carga docente.
     */
    @Test
    void debeMostrarCargaDocenteEnPdf()
            throws IOException {

        InformePlantaDocenteResponse informe =
                crearInformeConCarga();

        byte[] archivo =
                pdfService.exportarPlantaDocente(
                        informe,
                        null
                );

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (PDDocument documento =
                     Loader.loadPDF(archivo)) {

            assertTrue(documento.getNumberOfPages() >= 1);

            String texto =
                    new PDFTextStripper()
                            .getText(documento);

            assertTrue(
                    texto.contains("Año escolar: 2026")
            );

            assertTrue(
                    texto.contains(
                            "Horas semanales contratadas: 22"
                    )
            );

            assertTrue(
                    texto.contains(
                            "Horas semanales asignadas: 27"
                    )
            );

            assertTrue(
                    texto.contains(
                            "Horas semanales extras: 5"
                    )
            );

            assertTrue(
                    texto.contains("Docente Prueba")
            );

            assertTrue(
                    documento.getPage(0)
                            .getMediaBox()
                            .getWidth()
                            >
                            documento.getPage(0)
                                    .getMediaBox()
                                    .getHeight()
            );
        }
    }

    /**
     * Verifica las siete columnas del Excel,
     * los totales, los datos numericos
     * y el rango del autofiltro.
     */
    @Test
    void debeMostrarSieteColumnasEnExcel()
            throws IOException {

        InformePlantaDocenteResponse informe =
                crearInformeConCarga();

        byte[] archivo =
                excelService.exportarPlantaDocente(
                        informe,
                        null
                );

        try (XSSFWorkbook libro =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(
                                     archivo
                             )
                     )) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    "Año escolar: 2026",
                    hoja.getRow(1)
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Horas semanales asignadas",
                    hoja.getRow(5)
                            .getCell(5)
                            .getStringCellValue()
            );

            assertEquals(
                    "Horas semanales extras",
                    hoja.getRow(5)
                            .getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "Docente Prueba",
                    hoja.getRow(6)
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    CellType.NUMERIC,
                    hoja.getRow(6)
                            .getCell(5)
                            .getCellType()
            );

            assertEquals(
                    22,
                    hoja.getRow(6)
                            .getCell(4)
                            .getNumericCellValue()
            );

            assertEquals(
                    27,
                    hoja.getRow(6)
                            .getCell(5)
                            .getNumericCellValue()
            );

            assertEquals(
                    5,
                    hoja.getRow(6)
                            .getCell(6)
                            .getNumericCellValue()
            );

            assertEquals(
                    27,
                    hoja.getRow(4)
                            .getCell(1)
                            .getNumericCellValue()
            );

            assertEquals(
                    5,
                    hoja.getRow(4)
                            .getCell(3)
                            .getNumericCellValue()
            );

            assertEquals(
                    "A6:G7",
                    hoja.getCTWorksheet()
                            .getAutoFilter()
                            .getRef()
            );
        }
    }

    /**
     * Comprueba que las dos exportaciones
     * mantienen el comportamiento original
     * cuando no se selecciona año escolar.
     */
    @Test
    void debeConservarExportacionesOriginales()
            throws IOException {

        InformePlantaDocenteDetalleResponse docente =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        "Docente Prueba",
                        "Ciencias Naturales",
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

        byte[] pdf =
                pdfService.exportarPlantaDocente(
                        informe,
                        null
                );

        try (PDDocument documento =
                     Loader.loadPDF(pdf)) {

            String texto =
                    new PDFTextStripper()
                            .getText(documento);

            assertTrue(
                    texto.contains(
                            "Horas semanales contratadas: 22"
                    )
            );

            assertFalse(
                    texto.contains(
                            "Horas semanales asignadas:"
                    )
            );

            assertFalse(
                    texto.contains(
                            "Horas semanales extras:"
                    )
            );
        }

        byte[] excel =
                excelService.exportarPlantaDocente(
                        informe,
                        null
                );

        try (XSSFWorkbook libro =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(
                                     excel
                             )
                     )) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    "Horas semanales contratadas",
                    hoja.getRow(5)
                            .getCell(4)
                            .getStringCellValue()
            );

            assertNull(
                    hoja.getRow(5).getCell(5)
            );

            assertEquals(
                    "A6:E7",
                    hoja.getCTWorksheet()
                            .getAutoFilter()
                            .getRef()
            );
        }
    }

    /**
     * Datos de prueba:
     *
     * 22 horas contratadas.
     * 27 horas asignadas.
     * 5 horas extras.
     * Año escolar 2026.
     */
    private InformePlantaDocenteResponse
    crearInformeConCarga() {

        InformePlantaDocenteDetalleResponse docente =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        "Docente Prueba",
                        "Ciencias Naturales",
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA,
                        22,
                        27,
                        5
                );

        return new InformePlantaDocenteResponse(
                1,
                22,
                List.of(docente),
                UUID.randomUUID(),
                2026,
                27,
                5
        );
    }
}
