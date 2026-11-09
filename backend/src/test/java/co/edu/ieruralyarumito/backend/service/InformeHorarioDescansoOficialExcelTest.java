package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InformeHorarioDescansoOficialExcelTest {

    @Test
    void debeMostrarDescansoOficialSinContarloComoClase()
            throws IOException {

        UUID grupoId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();

        /*
         * Unicamente simulamos el servicio del Gestor.
         */
        HorarioService gestor =
                mock(HorarioService.class);

        /*
         * Clase academica de 55 minutos.
         */
        ClaseProgramadaResponse clase =
                new ClaseProgramadaResponse();

        clase.setDocenteId(UUID.randomUUID());
        clase.setDocenteNombre("Docente de prueba");

        clase.setAsignaturaId(UUID.randomUUID());
        clase.setAsignaturaNombre("Matematicas");

        clase.setDia(DiasSemana.LUNES);

        clase.setHoraInicio(LocalTime.of(7, 10));
        clase.setHoraFin(LocalTime.of(8, 5));

        clase.setAulaId(UUID.randomUUID());
        clase.setAulaNombre("Aula 1");

        clase.setEsHoraExtra(false);

        /*
         * Descanso oficial deliberadamente distinto
         * del horario fijo 09:15 - 09:45.
         *
         * Asi demostramos que Excel utiliza realmente
         * los datos recibidos desde el Gestor.
         */
        FranjaHorariaResponse descanso =
                new FranjaHorariaResponse();

        descanso.setId(UUID.randomUUID());
        descanso.setTurnoId(UUID.randomUUID());
        descanso.setTurnoNombre("Mañana");

        descanso.setNumero(2);
        descanso.setOrden(2);

        descanso.setHoraInicio(LocalTime.of(8, 5));
        descanso.setHoraFin(LocalTime.of(8, 20));

        descanso.setEsDescanso(true);

        /*
         * Tablero proporcionado por el Gestor.
         */
        HorarioGrupoResponse tablero =
                new HorarioGrupoResponse();

        tablero.setGrupoId(grupoId);
        tablero.setGrupoCodigo("1101");
        tablero.setGradoNombre("Undecimo");

        tablero.setHorarioId(UUID.randomUUID());
        tablero.setVersionHorario(1);

        tablero.setAnioEscolarId(anioId);
        tablero.setAnioEscolar(2026);

        tablero.setSedeNombre("Sede Principal");
        tablero.setTurnoNombre("Mañana");

        tablero.setEstadoHorario(
                EstadoAsignacionAcademica.BORRADOR
        );

        tablero.setClasesProgramadas(
                List.of(clase)
        );

        tablero.setFranjas(
                List.of(descanso)
        );

        when(gestor.obtenerTablero(grupoId))
                .thenReturn(tablero);

        /*
         * Filtros de la consulta institucional.
         */
        InformeHorariosFiltroRequest filtros =
                new InformeHorariosFiltroRequest(
                        anioId,
                        null,
                        LocalDate.of(2026, 11, 2),
                        LocalDate.of(2026, 11, 6),
                        null,
                        null,
                        grupoId,
                        null,
                        "TODAS",
                        null,
                        "BORRADOR"
                );

        /*
         * Servicios reales de HU-04.
         */
        InformeHorariosService informes =
                new InformeHorariosService();

        InformeHorarioGestorIntegracionService integracion =
                new InformeHorarioGestorIntegracionService(
                        informes
                );

        InformeHorarioJuanCamiloBridgeService puente =
                new InformeHorarioJuanCamiloBridgeService(
                        gestor,
                        integracion
                );

        InformeHorarioGestorExportacionService exportacion =
                new InformeHorarioGestorExportacionService(
                        puente,
                        new InformeHorariosPdfService(),
                        new InformeHorariosExcelService()
                );

        /*
         * Generacion real del archivo Excel.
         */
        byte[] contenido =
                exportacion.exportarExcelInstitucionalPorGrupo(
                        grupoId,
                        "I.E. Rural Yarumito",
                        filtros
                );

        assertNotNull(contenido);
        assertTrue(contenido.length > 2);

        /*
         * Abrimos el XLSX generado y examinamos
         * las filas reales de la parrilla.
         */
        try (XSSFWorkbook libro =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(contenido)
                     )) {

            Sheet hoja =
                    libro.getSheet("Horario semanal");

            assertNotNull(hoja);

            boolean descansoEncontrado = false;
            boolean claseEncontrada = false;

            int numeroClases = 0;
            int numeroDescansos = 0;

            for (Row fila : hoja) {

                Cell celdaHorario = fila.getCell(1);

                if (celdaHorario == null
                        || celdaHorario.getCellType()
                        != CellType.STRING) {
                    continue;
                }

                String horario =
                        celdaHorario.getStringCellValue();

                /*
                 * Verificacion de la clase academica.
                 */
                if ("07:10 - 08:05".equals(horario)) {

                    claseEncontrada = true;

                    assertEquals(
                            1,
                            (int) fila.getCell(0)
                                    .getNumericCellValue()
                    );

                    assertEquals(
                            "Matematicas",
                            fila.getCell(2)
                                    .getStringCellValue()
                    );

                    numeroClases++;
                }

                /*
                 * Verificacion del descanso oficial.
                 */
                if ("08:05 - 08:20".equals(horario)) {

                    descansoEncontrado = true;

                    assertEquals(
                            "—",
                            fila.getCell(0)
                                    .getStringCellValue()
                    );

                    for (int columna = 2;
                         columna < 7;
                         columna++) {

                        assertEquals(
                                "DESCANSO",
                                fila.getCell(columna)
                                        .getStringCellValue()
                        );
                    }

                    numeroDescansos++;
                }

                /*
                 * El descanso historico no debe
                 * aparecer porque el Gestor entrego
                 * una franja oficial diferente.
                 */
                assertNotEquals(
                        "09:15 - 09:45",
                        horario
                );
            }

            assertTrue(
                    claseEncontrada,
                    "No aparece la clase academica."
            );

            assertTrue(
                    descansoEncontrado,
                    "No aparece el descanso oficial."
            );

            assertEquals(1, numeroClases);
            assertEquals(1, numeroDescansos);
        }

        /*
         * Una consulta para construir el informe
         * y otra para obtener los descansos.
         */
        verify(gestor, times(2))
                .obtenerTablero(grupoId);
    }
}
