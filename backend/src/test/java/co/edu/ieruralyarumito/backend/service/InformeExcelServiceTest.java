
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

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
 * HU-04 / SCRUM-31.
 *
 * Pruebas del generador de archivos Excel.
 * Abre el XLSX resultante mediante Apache POI.
 */
class InformeExcelServiceTest {

    private final InformeExcelService servicio =
            new InformeExcelService();

    @Test
    void debeGenerarArchivoXlsxValido() throws IOException {

        byte[] contenido = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        assertNotNull(contenido);
        assertTrue(contenido.length > 0);

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            assertEquals(1, libro.getNumberOfSheets());
            assertEquals("Planta docente",
                    libro.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void debeConservarDocentesYDatosRealesDelInforme()
            throws IOException {

        byte[] contenido = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals("Ana Perez",
                    hoja.getRow(6).getCell(0)
                            .getStringCellValue());

            assertEquals("Matematicas",
                    hoja.getRow(6).getCell(1)
                            .getStringCellValue());

            assertEquals("ACTIVO",
                    hoja.getRow(6).getCell(2)
                            .getStringCellValue());

            assertEquals("PLANTA",
                    hoja.getRow(6).getCell(3)
                            .getStringCellValue());

            assertEquals("Carlos Gomez",
                    hoja.getRow(7).getCell(0)
                            .getStringCellValue());

            assertEquals("Ciencias",
                    hoja.getRow(7).getCell(1)
                            .getStringCellValue());

            assertNull(hoja.getRow(8));
        }
    }

    @Test
    void debeGuardarTotalesYHorasComoNumeros()
            throws IOException {

        byte[] contenido = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(CellType.NUMERIC,
                    hoja.getRow(2).getCell(1).getCellType());

            assertEquals(2,
                    hoja.getRow(2).getCell(1)
                            .getNumericCellValue());

            assertEquals(CellType.NUMERIC,
                    hoja.getRow(3).getCell(1).getCellType());

            assertEquals(42,
                    hoja.getRow(3).getCell(1)
                            .getNumericCellValue());

            assertEquals(22,
                    hoja.getRow(6).getCell(4)
                            .getNumericCellValue());

            assertEquals(20,
                    hoja.getRow(7).getCell(4)
                            .getNumericCellValue());

            double sumaDetalle =
                    hoja.getRow(6).getCell(4).getNumericCellValue()
                            + hoja.getRow(7).getCell(4).getNumericCellValue();

            assertEquals(
                    hoja.getRow(3).getCell(1).getNumericCellValue(),
                    sumaDetalle
            );
        }
    }

    @Test
    void debeUbicarEncabezadoYAutofiltroCorrectamente()
            throws IOException {

        byte[] contenido = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals("Docente",
                    hoja.getRow(5).getCell(0)
                            .getStringCellValue());

            assertEquals("Area de nombramiento",
                    hoja.getRow(5).getCell(1)
                            .getStringCellValue());

            assertEquals("Horas semanales contratadas",
                    hoja.getRow(5).getCell(4)
                            .getStringCellValue());

            assertEquals(6,
                    hoja.getPaneInformation()
                            .getHorizontalSplitPosition());

            assertNotNull(hoja.getCTWorksheet().getAutoFilter());

            String referencia =
                    hoja.getCTWorksheet().getAutoFilter().getRef();

            assertEquals("A6:E8", referencia);
        }
    }

    @Test
    void debeAceptarTituloPersonalizadoDeCienCaracteres()
            throws IOException {

        String titulo = "A".repeat(100);

        byte[] contenido = servicio.exportarPlantaDocente(
                informeConDosDocentes(),
                titulo
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            assertEquals(
                    titulo,
                    libro.getSheetAt(0)
                            .getRow(0)
                            .getCell(0)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void debeRechazarTituloMayorACienCaracteres() {

        String titulo = "A".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.exportarPlantaDocente(
                        informeConDosDocentes(),
                        titulo
                )
        );
    }

    @Test
    void debeGenerarInformeVacioSinInventarFilas()
            throws IOException {

        InformePlantaDocenteResponse vacio =
                new InformePlantaDocenteResponse(
                        0,
                        0,
                        List.of()
                );

        byte[] contenido = servicio.exportarPlantaDocente(
                vacio, null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(contenido))) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(0,
                    hoja.getRow(2).getCell(1)
                            .getNumericCellValue());

            assertEquals(0,
                    hoja.getRow(3).getCell(1)
                            .getNumericCellValue());

            assertEquals("Docente",
                    hoja.getRow(5).getCell(0)
                            .getStringCellValue());

            assertNull(hoja.getRow(6));
            assertNull(hoja.getCTWorksheet().getAutoFilter());
        }
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
}
