package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Verifica que la exportacion PDF utilice
 * los descansos oficiales del Gestor de Horarios.
 *
 * No modifica PostgreSQL.
 * No modifica el Gestor de Juan Camilo.
 * No requiere servidor ni conexiones externas.
 */
class InformeHorarioDescansoOficialPdfTest {

    @Test
    void debeMostrarDescansoOficialSinContarloComoClase()
            throws IOException {

        UUID grupoId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();

        /*
         * Simulamos exclusivamente el servicio del Gestor.
         */
        HorarioService gestor =
                mock(HorarioService.class);

        /*
         * Clase academica real de 55 minutos.
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
         * Descanso oficial diferente del descanso
         * historico de 09:15 a 09:45.
         *
         * Debe obtenerse desde el Gestor.
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
         * Consulta institucional.
         *
         * BORRADOR solamente se utiliza para
         * comprobar el informe institucional.
         *
         * No sustituye el estado PUBLICADO.
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
         * Generacion real del PDF mediante
         * el servicio de exportacion integrado.
         */
        byte[] contenido =
                exportacion.exportarPdfInstitucionalPorGrupo(
                        grupoId,
                        "I.E. Rural Yarumito",
                        filtros
                );

        assertNotNull(contenido);
        assertTrue(
                contenido.length > 4,
                "El PDF generado esta vacio."
        );

        /*
         * Validacion de la firma PDF.
         */
        String firma = new String(
                contenido,
                0,
                4,
                java.nio.charset.StandardCharsets.US_ASCII
        );

        assertEquals("%PDF", firma);

        /*
         * Abrimos el documento PDF generado.
         *
         * Extraemos su contenido para comprobar
         * las franjas horarias efectivamente escritas.
         */
        try (PDDocument documento =
                     Loader.loadPDF(contenido)) {

            assertTrue(
                    documento.getNumberOfPages() > 0,
                    "El PDF no contiene paginas."
            );

            PDFTextStripper extractor =
                    new PDFTextStripper();

            extractor.setSortByPosition(true);

            String texto =
                    extractor.getText(documento);

            assertNotNull(texto);
            assertFalse(texto.isBlank());

            /*
             * La clase academica debe estar presente.
             */
            assertTrue(
                    texto.contains("07:10 - 08:05"),
                    "No aparece el horario de la clase."
            );

            assertTrue(
                    texto.contains("Matematicas"),
                    "No aparece la asignatura."
            );

            /*
             * El descanso oficial debe aparecer
             * exactamente con las horas del Gestor.
             */
            assertTrue(
                    texto.contains("08:05 - 08:20"),
                    "No aparece el descanso oficial."
            );

            /*
             * La cuadricula semanal debe identificar
             * el descanso en sus cinco dias.
             *
             * Se comprueba que DESCANSO no se
             * presenta como una asignatura academica.
             */
            assertEquals(
                    5,
                    contarOcurrencias(texto, "DESCANSO"),
                    "El descanso debe aparecer "
                            + "de lunes a viernes."
            );

            /*
             * Solo existe una asignatura programada.
             */
            assertEquals(
                    1,
                    contarOcurrencias(texto, "Matematicas"),
                    "La clase academica fue duplicada."
            );

            /*
             * El descanso historico no debe utilizarse
             * cuando el Gestor entrega uno oficial.
             */
            assertFalse(
                    texto.contains("09:15 - 09:45"),
                    "El PDF utilizo el descanso historico."
            );
        }

        /*
         * Primera consulta: construir el informe.
         * Segunda consulta: obtener descansos oficiales.
         */
        verify(gestor, times(2))
                .obtenerTablero(grupoId);

        verifyNoMoreInteractions(gestor);
    }

    /**
     * Cuenta las apariciones exactas de un texto
     * dentro del contenido extraido del PDF.
     */
    private static int contarOcurrencias(
            String contenido,
            String buscado) {

        int cantidad = 0;
        int posicion = 0;

        while ((posicion = contenido.indexOf(
                buscado,
                posicion
        )) >= 0) {

            cantidad++;
            posicion += buscado.length();
        }

        return cantidad;
    }
}
