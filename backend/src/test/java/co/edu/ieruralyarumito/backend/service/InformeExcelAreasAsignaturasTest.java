package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAsignaturaDetalleResponse;

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
 * Pruebas unitarias de la exportacion Excel
 * del informe de areas y asignaturas.
 *
 * Verifica el contenido del archivo XLSX
 * abriendolo nuevamente mediante Apache POI.
 */
class InformeExcelAreasAsignaturasTest {

    private final InformeExcelService servicio =
            new InformeExcelService();

    @Test
    void debeGenerarUnArchivoXlsxValido() throws IOException {

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informeEjemplo(),
                null
        );

        assertNotNull(contenido);
        assertTrue(contenido.length > 0);

        try (XSSFWorkbook libro = abrir(contenido)) {

            assertEquals(1, libro.getNumberOfSheets());

            assertEquals(
                    "Areas y asignaturas",
                    libro.getSheetAt(0).getSheetName()
            );

            assertEquals(
                    "Informe de areas y asignaturas institucionales",
                    libro.getSheetAt(0).getRow(0)
                            .getCell(0).getStringCellValue()
            );
        }
    }

    @Test
    void debeConservarLosTotalesComoValoresNumericos()
            throws IOException {

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informeEjemplo(),
                null
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    CellType.NUMERIC,
                    hoja.getRow(2).getCell(1).getCellType()
            );

            assertEquals(
                    2,
                    hoja.getRow(2).getCell(1)
                            .getNumericCellValue()
            );

            assertEquals(
                    3,
                    hoja.getRow(3).getCell(1)
                            .getNumericCellValue()
            );
        }
    }

    @Test
    void debeExportarTodasLasAsignaturasYSusAtributos()
            throws IOException {

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informeEjemplo(),
                null
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            // Primera asignatura de Matematicas.
            assertEquals(
                    "Matematicas",
                    hoja.getRow(6).getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "MAT",
                    hoja.getRow(6).getCell(1)
                            .getStringCellValue()
            );

            assertEquals(
                    "ACTIVA",
                    hoja.getRow(6).getCell(2)
                            .getStringCellValue()
            );

            assertEquals(
                    2,
                    hoja.getRow(6).getCell(5)
                            .getNumericCellValue()
            );

            assertEquals(
                    "Algebra",
                    hoja.getRow(6).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "ALG",
                    hoja.getRow(6).getCell(7)
                            .getStringCellValue()
            );

            assertEquals(
                    "ACTIVA",
                    hoja.getRow(6).getCell(8)
                            .getStringCellValue()
            );

            assertEquals(
                    "Si",
                    hoja.getRow(6).getCell(10)
                            .getStringCellValue()
            );

            // Segunda asignatura de Matematicas.
            assertEquals(
                    "Geometria",
                    hoja.getRow(7).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "INACTIVA",
                    hoja.getRow(7).getCell(8)
                            .getStringCellValue()
            );

            // Tercera asignatura, correspondiente a Ciencias.
            assertEquals(
                    "Biologia",
                    hoja.getRow(8).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "Si",
                    hoja.getRow(8).getCell(9)
                            .getStringCellValue()
            );

            assertEquals(
                    "Si",
                    hoja.getRow(8).getCell(11)
                            .getStringCellValue()
            );

            assertNull(hoja.getRow(9));
        }
    }

    @Test
    void debeConservarAreaSinAsignaturas()
            throws IOException {

        InformeAreaDetalleResponse area =
                crearArea(
                        "Filosofia",
                        "FIL",
                        true,
                        List.of()
                );

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        1,
                        0,
                        List.of(area)
                );

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informe,
                null
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    "Filosofia",
                    hoja.getRow(6).getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    0,
                    hoja.getRow(6).getCell(5)
                            .getNumericCellValue()
            );

            assertNull(hoja.getRow(6).getCell(6));
            assertNull(hoja.getRow(6).getCell(7));

            assertEquals(
                    0,
                    hoja.getRow(3).getCell(1)
                            .getNumericCellValue()
            );
        }
    }

    @Test
    void debeUbicarCorrectamenteEncabezadoYAutofiltro()
            throws IOException {

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informeEjemplo(),
                null
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    "Area",
                    hoja.getRow(5).getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Asignatura",
                    hoja.getRow(5).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "Requiere docente exclusivo",
                    hoja.getRow(5).getCell(11)
                            .getStringCellValue()
            );

            assertNotNull(hoja.getPaneInformation());

            assertEquals(
                    6,
                    hoja.getPaneInformation()
                            .getHorizontalSplitPosition()
            );

            assertNotNull(
                    hoja.getCTWorksheet().getAutoFilter()
            );

            assertEquals(
                    "A6:L9",
                    hoja.getCTWorksheet()
                            .getAutoFilter().getRef()
            );
        }
    }

    @Test
    void debeValidarLongitudDelTituloPersonalizado()
            throws IOException {

        String tituloValido = "A".repeat(100);

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informeEjemplo(),
                tituloValido
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            assertEquals(
                    tituloValido,
                    libro.getSheetAt(0)
                            .getRow(0).getCell(0)
                            .getStringCellValue()
            );
        }

        String tituloInvalido = "A".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.exportarAreasAsignaturas(
                        informeEjemplo(),
                        tituloInvalido
                )
        );
    }

    @Test
    void debeGenerarInformeVacioSinFilasInventadas()
            throws IOException {

        InformeAreasAsignaturasResponse informe =
                new InformeAreasAsignaturasResponse(
                        0,
                        0,
                        List.of()
                );

        byte[] contenido = servicio.exportarAreasAsignaturas(
                informe,
                null
        );

        try (XSSFWorkbook libro = abrir(contenido)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            assertEquals(
                    0,
                    hoja.getRow(2).getCell(1)
                            .getNumericCellValue()
            );

            assertEquals(
                    0,
                    hoja.getRow(3).getCell(1)
                            .getNumericCellValue()
            );

            assertEquals(
                    "Area",
                    hoja.getRow(5).getCell(0)
                            .getStringCellValue()
            );

            assertNull(hoja.getRow(6));

            assertNull(
                    hoja.getCTWorksheet().getAutoFilter()
            );
        }
    }

    private XSSFWorkbook abrir(byte[] contenido)
            throws IOException {

        return new XSSFWorkbook(
                new ByteArrayInputStream(contenido)
        );
    }

    private InformeAreasAsignaturasResponse informeEjemplo() {

        InformeAsignaturaDetalleResponse algebra =
                crearAsignatura(
                        "Algebra",
                        "ALG",
                        true,
                        false,
                        true,
                        false
                );

        InformeAsignaturaDetalleResponse geometria =
                crearAsignatura(
                        "Geometria",
                        "GEO",
                        false,
                        false,
                        false,
                        false
                );

        InformeAsignaturaDetalleResponse biologia =
                crearAsignatura(
                        "Biologia",
                        "BIO",
                        true,
                        true,
                        false,
                        true
                );

        InformeAreaDetalleResponse matematicas =
                crearArea(
                        "Matematicas",
                        "MAT",
                        true,
                        List.of(algebra, geometria)
                );

        InformeAreaDetalleResponse ciencias =
                crearArea(
                        "Ciencias",
                        "CIE",
                        true,
                        List.of(biologia)
                );

        return new InformeAreasAsignaturasResponse(
                2,
                3,
                List.of(matematicas, ciencias)
        );
    }

    private InformeAreaDetalleResponse crearArea(
            String nombre,
            String codigo,
            boolean activa,
            List<InformeAsignaturaDetalleResponse> asignaturas) {

        return new InformeAreaDetalleResponse(
                UUID.randomUUID(),
                nombre,
                codigo,
                activa,
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
            boolean idoneidadEstricta,
            boolean docenteExclusivo) {

        return new InformeAsignaturaDetalleResponse(
                UUID.randomUUID(),
                nombre,
                codigo,
                activa,
                mediaTecnica,
                idoneidadEstricta,
                docenteExclusivo
        );
    }
}
