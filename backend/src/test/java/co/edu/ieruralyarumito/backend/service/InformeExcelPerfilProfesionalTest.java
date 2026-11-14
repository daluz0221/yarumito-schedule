package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeIdoneidadDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeTituloProfesionalDetalleResponse;
import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de exportacion XLSX del informe
 * de perfiles profesionales e idoneidades.
 *
 * No utiliza PostgreSQL ni contexto Spring.
 */
class InformeExcelPerfilProfesionalTest {

    private final InformeExcelService servicio =
            new InformeExcelService();

    @Test
    void debeGenerarDosHojasExcelValidas() throws IOException {

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(), null
        );

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);

        try (XSSFWorkbook libro = abrir(archivo)) {

            assertEquals(2, libro.getNumberOfSheets());

            assertEquals(
                    "Titulos profesionales",
                    libro.getSheetAt(0).getSheetName()
            );

            assertEquals(
                    "Idoneidades",
                    libro.getSheetAt(1).getSheetName()
            );
        }
    }

    @Test
    void debeConservarTotalesNumericosEnAmbasHojas()
            throws IOException {

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(), null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            for (int i = 0; i < 2; i++) {

                XSSFSheet hoja = libro.getSheetAt(i);

                assertEquals(
                        CellType.NUMERIC,
                        hoja.getRow(2).getCell(1).getCellType()
                );

                assertEquals(
                        1,
                        hoja.getRow(2).getCell(1)
                                .getNumericCellValue()
                );

                assertEquals(
                        2,
                        hoja.getRow(3).getCell(1)
                                .getNumericCellValue()
                );

                assertEquals(
                        3,
                        hoja.getRow(4).getCell(1)
                                .getNumericCellValue()
                );
            }
        }
    }

    @Test
    void debeExportarTitulosSinMultiplicarlos()
            throws IOException {

        InformePerfilProfesionalResponse informe = informeEjemplo();

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe, null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            XSSFSheet hoja = libro.getSheetAt(0);

            // El encabezado esta en indice 6.
            // Los dos titulos ocupan indices 7 y 8.
            assertEquals(
                    "Nombre del titulo",
                    hoja.getRow(6).getCell(4)
                            .getStringCellValue()
            );

            assertEquals(
                    "Licenciatura en Literatura",
                    hoja.getRow(7).getCell(4)
                            .getStringCellValue()
            );

            assertEquals(
                    "Maestria en Educacion",
                    hoja.getRow(8).getCell(4)
                            .getStringCellValue()
            );

            assertEquals(
                    "LICENCIATURA",
                    hoja.getRow(7).getCell(5)
                            .getStringCellValue()
            );

            assertEquals(
                    "Universidad de Prueba",
                    hoja.getRow(7).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    2020,
                    hoja.getRow(7).getCell(7)
                            .getNumericCellValue()
            );

            assertNull(hoja.getRow(9));

            // Los UUID de los titulos son distintos.
            Set<String> ids = IntStream.rangeClosed(7, 8)
                    .mapToObj(i -> hoja.getRow(i).getCell(3)
                            .getStringCellValue())
                    .collect(Collectors.toSet());

            assertEquals(2, ids.size());
        }
    }

    @Test
    void debeExportarIdoneidadesSinMultiplicarlas()
            throws IOException {

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(), null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            XSSFSheet hoja = libro.getSheetAt(1);

            assertEquals(
                    "Idoneidad ID",
                    hoja.getRow(6).getCell(3)
                            .getStringCellValue()
            );

            assertEquals(
                    "Humanidades",
                    hoja.getRow(7).getCell(4)
                            .getStringCellValue()
            );

            assertEquals(
                    "PRINCIPAL",
                    hoja.getRow(7).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "AUTORIZADA",
                    hoja.getRow(8).getCell(6)
                            .getStringCellValue()
            );

            assertEquals(
                    "EXCEPCIONAL",
                    hoja.getRow(9).getCell(6)
                            .getStringCellValue()
            );

            assertNull(hoja.getRow(10));

            Set<String> ids = IntStream.rangeClosed(7, 9)
                    .mapToObj(i -> hoja.getRow(i).getCell(3)
                            .getStringCellValue())
                    .collect(Collectors.toSet());

            assertEquals(3, ids.size());
        }
    }

    @Test
    void debeConservarAsignaturaTituloSoporteYVigencias()
            throws IOException {

        InformePerfilProfesionalResponse informe = informeEjemplo();

        var idoneidadOriginal = informe.docentes().get(0)
                .idoneidades().get(0);

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe, null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            XSSFSheet hoja = libro.getSheetAt(1);

            assertEquals(
                    "Literatura",
                    hoja.getRow(7).getCell(5)
                            .getStringCellValue()
            );

            assertEquals(
                    idoneidadOriginal.tituloSoporteId().toString(),
                    hoja.getRow(7).getCell(7)
                            .getStringCellValue()
            );

            assertEquals(
                    "2025-01-01",
                    hoja.getRow(7).getCell(8)
                            .getStringCellValue()
            );

            assertEquals(
                    "Si",
                    hoja.getRow(7).getCell(10)
                            .getStringCellValue()
            );

            assertEquals(
                    "No",
                    hoja.getRow(8).getCell(10)
                            .getStringCellValue()
            );

            assertEquals(
                    "No",
                    hoja.getRow(9).getCell(10)
                            .getStringCellValue()
            );

            // La segunda idoneidad no tiene asignatura
            // ni titulo de soporte.
            assertNull(hoja.getRow(8).getCell(5));
            assertNull(hoja.getRow(8).getCell(7));

            // Fecha de referencia en las dos hojas.
            assertEquals(
                    "Fecha de referencia: 2026-10-11",
                    libro.getSheetAt(0)
                            .getRow(1).getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Fecha de referencia: 2026-10-11",
                    libro.getSheetAt(1)
                            .getRow(1).getCell(0)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void debeValidarTituloPersonalizado()
            throws IOException {

        String tituloValido = "T".repeat(100);

        byte[] archivo = servicio.exportarPerfilProfesional(
                informeEjemplo(),
                tituloValido
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            assertEquals(
                    tituloValido,
                    libro.getSheetAt(0).getRow(0)
                            .getCell(0).getStringCellValue()
            );

            assertEquals(
                    tituloValido,
                    libro.getSheetAt(1).getRow(0)
                            .getCell(0).getStringCellValue()
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
    void debeGenerarDosHojasVaciasSinRegistrosInventados()
            throws IOException {

        InformePerfilProfesionalResponse vacio =
                new InformePerfilProfesionalResponse(
                        LocalDate.of(2026, 10, 11),
                        0,
                        0,
                        0,
                        List.of()
                );

        byte[] archivo = servicio.exportarPerfilProfesional(
                vacio, null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            assertEquals(2, libro.getNumberOfSheets());

            for (int i = 0; i < 2; i++) {

                XSSFSheet hoja = libro.getSheetAt(i);

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
                        0,
                        hoja.getRow(4).getCell(1)
                                .getNumericCellValue()
                );

                assertNull(hoja.getRow(7));
                assertNull(hoja.getCTWorksheet().getAutoFilter());
            }
        }
    }


    @Test
    void debeConservarDocenteSinTitulosNiIdoneidadesEnAmbasHojas()
            throws IOException {

        UUID docenteId = UUID.randomUUID();

        var docente = new InformePerfilProfesionalResponse.DetalleDocente(
                docenteId,
                "María Pérez",
                "Humanidades",
                List.of(),
                List.of()
        );

        var informe = new InformePerfilProfesionalResponse(
                LocalDate.of(2026, 10, 12),
                1, 0, 0,
                List.of(docente)
        );

        byte[] archivo = servicio.exportarPerfilProfesional(
                informe, null
        );

        try (XSSFWorkbook libro = abrir(archivo)) {

            assertEquals(2, libro.getNumberOfSheets());

            for (int i = 0; i < 2; i++) {

                XSSFSheet hoja = libro.getSheetAt(i);

                var fila = hoja.getRow(7);

                assertNotNull(fila);
                assertEquals(docenteId.toString(),
                        fila.getCell(0).getStringCellValue());
                assertEquals("María Pérez",
                        fila.getCell(1).getStringCellValue());
                assertEquals("Humanidades",
                        fila.getCell(2).getStringCellValue());

                assertNull(fila.getCell(3),
                        "No debe inventarse un título ni una idoneidad");
                assertNull(hoja.getRow(8),
                        "No debe añadirse una fila ficticia");
            }
        }
    }

    private XSSFWorkbook abrir(byte[] archivo) throws IOException {

        return new XSSFWorkbook(
                new ByteArrayInputStream(archivo)
        );
    }

    private InformePerfilProfesionalResponse informeEjemplo() {

        UUID docenteId = UUID.randomUUID();
        UUID areaHumanidades = UUID.randomUUID();
        UUID areaSociales = UUID.randomUUID();
        UUID asignaturaLiteratura = UUID.randomUUID();

        UUID primerTituloId = UUID.randomUUID();

        InformeTituloProfesionalDetalleResponse licenciatura =
                new InformeTituloProfesionalDetalleResponse(
                        primerTituloId,
                        "Licenciatura en Literatura",
                        NivelTituloProfesional.LICENCIATURA,
                        "Universidad de Prueba",
                        2020
                );

        InformeTituloProfesionalDetalleResponse maestria =
                new InformeTituloProfesionalDetalleResponse(
                        UUID.randomUUID(),
                        "Maestria en Educacion",
                        NivelTituloProfesional.MAESTRIA,
                        "Universidad de Prueba",
                        2024
                );

        InformeIdoneidadDetalleResponse vigente =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaHumanidades,
                        "Humanidades",
                        asignaturaLiteratura,
                        "Literatura",
                        TipoIdoneidad.PRINCIPAL,
                        primerTituloId,
                        LocalDate.of(2025, 1, 1),
                        null,
                        true
                );

        InformeIdoneidadDetalleResponse futura =
                new InformeIdoneidadDetalleResponse(
                        UUID.randomUUID(),
                        areaSociales,
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
                        areaSociales,
                        "Ciencias Sociales",
                        null,
                        null,
                        TipoIdoneidad.EXCEPCIONAL,
                        null,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2025, 12, 31),
                        false
                );

        InformePerfilProfesionalResponse.DetalleDocente docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        docenteId,
                        "Juan Perez",
                        "Humanidades",
                        List.of(licenciatura, maestria),
                        List.of(vigente, futura, vencida)
                );

        return new InformePerfilProfesionalResponse(
                LocalDate.of(2026, 10, 11),
                1,
                2,
                3,
                List.of(docente)
        );
    }
}
