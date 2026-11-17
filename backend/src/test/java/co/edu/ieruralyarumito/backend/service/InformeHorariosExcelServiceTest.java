
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosExcelServiceTest {

    private final InformeHorariosService informeService =
            new InformeHorariosService();

    private final InformeHorariosExcelService excelService =
            new InformeHorariosExcelService();

    @Test
    void generaExcelConTresHojas() throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeConDosClases(),
                null
        );

        assertTrue(archivo.length > 0);

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            assertEquals(3, libro.getNumberOfSheets());

            assertEquals(
                    "Horario semanal",
                    libro.getSheetAt(0).getSheetName()
            );

            assertNotNull(libro.getSheet("Detalle horarios"));
            assertNotNull(libro.getSheet("Carga academica"));
        }
    }

    @Test
    void exportaDiasYHorasExactas() throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeConDosClases(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            Sheet hoja = libro.getSheet("Detalle horarios");

            Row primeraClase = hoja.getRow(3);
            Row segundaClase = hoja.getRow(4);

            assertEquals(
                    "LUNES",
                    primeraClase.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "07:00",
                    primeraClase.getCell(1).getStringCellValue()
            );

            assertEquals(
                    "08:00",
                    primeraClase.getCell(2).getStringCellValue()
            );

            assertEquals(
                    "MARTES",
                    segundaClase.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "14:00",
                    segundaClase.getCell(1).getStringCellValue()
            );

            assertEquals(
                    "15:00",
                    segundaClase.getCell(2).getStringCellValue()
            );
        }
    }

    @Test
    void distingueContrajornadaYHorasExtras()
            throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeConDosClases(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            Sheet detalle = libro.getSheet("Detalle horarios");
            Sheet carga = libro.getSheet("Carga academica");

            assertEquals(
                    "REGULAR",
                    detalle.getRow(3).getCell(7)
                            .getStringCellValue()
            );

            assertEquals(
                    "CONTRAJORNADA",
                    detalle.getRow(4).getCell(7)
                            .getStringCellValue()
            );

            assertEquals(
                    "Si",
                    detalle.getRow(4).getCell(9)
                            .getStringCellValue()
            );

            Row resumen = carga.getRow(3);

            assertEquals(
                    1.0,
                    resumen.getCell(3).getNumericCellValue()
            );

            assertEquals(
                    1.0,
                    resumen.getCell(4).getNumericCellValue()
            );

            assertEquals(
                    2.0,
                    resumen.getCell(7).getNumericCellValue()
            );
        }
    }

    @Test
    void cuadriculaSemanalUbicaLasClasesCorrectamente()
            throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeConDosClases(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            Sheet hoja = libro.getSheet("Horario semanal");

            assertEquals(
                    "Lunes",
                    hoja.getRow(2).getCell(2)
                            .getStringCellValue()
            );

            assertEquals(
                    "Martes",
                    hoja.getRow(2).getCell(3)
                            .getStringCellValue()
            );

            // Primera franja: lunes, jornada regular.
            Row regular = hoja.getRow(3);

            assertEquals(
                    "Regular",
                    regular.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "07:00 - 08:00",
                    regular.getCell(1).getStringCellValue()
            );

            String lunes =
                    regular.getCell(2).getStringCellValue();

            assertTrue(lunes.contains("Matematicas"));
            assertTrue(lunes.contains("Docente de prueba"));
            assertTrue(lunes.contains("10A"));
            assertTrue(lunes.contains("Aula 1"));

            // Segunda franja: martes, contrajornada.
            Row contra = hoja.getRow(4);

            assertEquals(
                    "Contrajornada",
                    contra.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "14:00 - 15:00",
                    contra.getCell(1).getStringCellValue()
            );

            String martes =
                    contra.getCell(3).getStringCellValue();

            assertTrue(martes.contains("Matematicas"));
            assertTrue(martes.contains("Aula 2"));
            assertTrue(martes.contains("Hora extra"));
        }
    }

    @Test
    void conservaLosRegistrosEnLaHojaDetalle()
            throws Exception {

        byte[] archivo = excelService.exportarHorarios(
                informeConDosClases(),
                null
        );

        try (XSSFWorkbook libro = new XSSFWorkbook(
                new ByteArrayInputStream(archivo))) {

            Sheet detalle = libro.getSheet("Detalle horarios");

            assertEquals(
                    "Aula 1",
                    detalle.getRow(3).getCell(8)
                            .getStringCellValue()
            );

            assertEquals(
                    "Aula 2",
                    detalle.getRow(4).getCell(8)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void rechazaInformeNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> excelService.exportarHorarios(null, null)
        );
    }

    private InformeHorariosResponse informeConDosClases() {

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
                List.of(
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
                                "LUNES",
                                LocalTime.of(7, 0),
                                LocalTime.of(8, 0),
                                "REGULAR",
                                UUID.randomUUID(),
                                "Aula 1",
                                false
                        ),
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
                                "MARTES",
                                LocalTime.of(14, 0),
                                LocalTime.of(15, 0),
                                "CONTRAJORNADA",
                                UUID.randomUUID(),
                                "Aula 2",
                                true
                        )
                );

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
