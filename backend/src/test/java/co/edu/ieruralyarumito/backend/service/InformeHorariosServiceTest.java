package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCargaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosServiceTest {

    private InformeHorariosService service;

    @BeforeEach
    void configurar() {
        service = new InformeHorariosService();
    }

    @Test
    void generaInformeSinClases() {
        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of()
        );

        assertEquals(0, resultado.totalClasesProgramadas());
        assertEquals(0L, resultado.horasAcademicasProgramadas());
        assertTrue(resultado.clasesProgramadas().isEmpty());
        assertTrue(resultado.resumenCargaHoraria().isEmpty());
    }

    @Test
    void contabilizaClasesPorJornada() {
        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MARTES", "CONTRAJORNADA", 14, false)
                )
        );

        assertEquals(2, resultado.totalClasesProgramadas());
        assertEquals(2L, resultado.horasAcademicasProgramadas());
        assertEquals(1L, resultado.horasAcademicasJornadaRegular());
        assertEquals(1L, resultado.horasAcademicasContrajornada());

        assertEquals(1, resultado.totalDocentes());
        assertEquals(1, resultado.totalGrupos());
        assertEquals(1, resultado.totalAsignaturas());

        InformeHorarioCargaResponse carga =
                resultado.resumenCargaHoraria().get(0);

        assertEquals(2L, carga.horasOrdinarias());
        assertEquals(0L, carga.horasExtras());
        assertEquals(2L, carga.totalHorasAcademicas());
    }

    @Test
    void distingueHorasOrdinariasYExtras() {
        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MARTES", "REGULAR", 8, true),
                        clase(docente, grupo, asignatura,
                                "MIERCOLES", "CONTRAJORNADA", 14, false)
                )
        );

        InformeHorarioCargaResponse carga =
                resultado.resumenCargaHoraria().get(0);

        assertEquals(2L, carga.horasOrdinarias());
        assertEquals(1L, carga.horasExtras());
        assertEquals(2L, carga.horasJornadaRegular());
        assertEquals(1L, carga.horasContrajornada());
        assertEquals(3L, carga.totalHorasAcademicas());
    }

    @Test
    void contrajornadaNoImplicaHoraExtra() {
        UUID docente = UUID.randomUUID();

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(
                        clase(
                                docente,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "JUEVES",
                                "CONTRAJORNADA",
                                14,
                                false
                        )
                )
        );

        InformeHorarioCargaResponse carga =
                resultado.resumenCargaHoraria().get(0);

        assertEquals(1L, carga.horasOrdinarias());
        assertEquals(0L, carga.horasExtras());
        assertEquals(1L, carga.horasContrajornada());
    }

    @Test
    void ordenaDiasDeLunesAViernes() {
        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(
                        clase(docente, grupo, asignatura,
                                "VIERNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MIERCOLES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false)
                )
        );

        assertEquals(
                List.of("LUNES", "MIERCOLES", "VIERNES"),
                resultado.clasesProgramadas().stream()
                        .map(InformeHorarioClaseDetalleResponse::diaSemana)
                        .toList()
        );
    }

    @Test
    void noDuplicaDocentesEnLosTotales() {
        UUID docente = UUID.randomUUID();

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(
                        clase(docente, UUID.randomUUID(),
                                UUID.randomUUID(),
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, UUID.randomUUID(),
                                UUID.randomUUID(),
                                "MARTES", "REGULAR", 8, false)
                )
        );

        assertEquals(1, resultado.totalDocentes());
        assertEquals(2, resultado.totalGrupos());
        assertEquals(2, resultado.totalAsignaturas());
    }

    @Test
    void rechazaRangoDeFechasInvertido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generar(
                        filtros(
                                LocalDate.of(2026, 4, 3),
                                LocalDate.of(2026, 2, 2)
                        ),
                        List.of()
                )
        );
    }

    @Test
    void rechazaFiltrosNulos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generar(null, List.of())
        );
    }

    @Test
    void aceptaListaNulaSinInventarClases() {
        InformeHorariosResponse resultado =
                generar(filtros(null, null), null);

        assertTrue(resultado.clasesProgramadas().isEmpty());
        assertEquals(0, resultado.totalClasesProgramadas());
    }

    private InformeHorariosResponse generar(
            InformeHorariosFiltroRequest filtros,
            List<InformeHorarioClaseDetalleResponse> clases
    ) {
        return service.construirInforme(
                UUID.randomUUID(),
                "2026",
                "I.E. Rural Yarumito",
                "Principal",
                filtros,
                clases
        );
    }

    private InformeHorariosFiltroRequest filtros(
            LocalDate inicio,
            LocalDate fin
    ) {
        return new InformeHorariosFiltroRequest(
                UUID.randomUUID(),
                null,
                inicio,
                fin,
                null,
                null,
                null,
                null,
                "TODAS",
                null,
                "PUBLICADO"
        );
    }

    private InformeHorarioClaseDetalleResponse clase(
            UUID docenteId,
            UUID grupoId,
            UUID asignaturaId,
            String dia,
            String jornada,
            int hora,
            boolean esHoraExtra
    ) {
        LocalTime inicio = LocalTime.of(hora, 0);

        return new InformeHorarioClaseDetalleResponse(
                UUID.randomUUID(),
                1,
                "PUBLICADO",
                docenteId,
                "Docente de prueba",
                grupoId,
                "6",
                "6A",
                asignaturaId,
                "Matematicas",
                dia,
                inicio,
                inicio.plusHours(1),
                jornada,
                UUID.randomUUID(),
                "Aula de prueba",
                esHoraExtra
        );
    }

    @Test
    void filtraClasesPorGrupoSeleccionado() {

        UUID grupo701 = UUID.randomUUID();
        UUID grupo1101 = UUID.randomUUID();
        UUID docente = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosFiltroRequest filtrosGrupo =
                new InformeHorariosFiltroRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        grupo701,
                        null,
                        null,
                        null,
                        null
                );

        var resultado = generar(
                filtrosGrupo,
                List.of(
                        clase(
                                docente, grupo701, asignatura,
                                "LUNES", "REGULAR", 7, false
                        ),
                        clase(
                                docente, grupo1101, asignatura,
                                "MARTES", "REGULAR", 8, false
                        )
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(1, resultado.totalGrupos());
        assertEquals(1L, resultado.horasAcademicasProgramadas());

        assertEquals(
                grupo701,
                resultado.clasesProgramadas()
                        .get(0).grupoId()
        );
    }

    @Test
    void filtraClasesPorDiaSeleccionado() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosFiltroRequest filtroDia =
                new InformeHorariosFiltroRequest(
                        null, null, null, null,
                        null, null, null, null,
                        null, "LUNES", null
                );

        var resultado = generar(
                filtroDia,
                List.of(
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MARTES", "REGULAR", 8, false)
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());

        assertEquals(
                "LUNES",
                resultado.clasesProgramadas()
                        .get(0).diaSemana()
        );

        assertEquals(1L, resultado.horasAcademicasProgramadas());
    }

    @Test
    void filtraClasesPorJornadaSeleccionada() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        InformeHorariosFiltroRequest filtroJornada =
                new InformeHorariosFiltroRequest(
                        null, null, null, null,
                        null, null, null, null,
                        "REGULAR", null, null
                );

        var resultado = generar(
                filtroJornada,
                List.of(
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MARTES", "CONTRAJORNADA", 14, false)
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(1L, resultado.horasAcademicasJornadaRegular());
        assertEquals(0L, resultado.horasAcademicasContrajornada());

        assertEquals(
                "REGULAR",
                resultado.clasesProgramadas().get(0).jornada()
        );
    }

    @Test
    void conservaTodasLasJornadasCuandoFiltroEsTodas() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        var resultado = generar(
                filtros(null, null),
                List.of(
                        clase(docente, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, asignatura,
                                "MARTES", "CONTRAJORNADA", 14, false)
                )
        );

        assertEquals(2, resultado.totalClasesProgramadas());

        assertEquals(
                1L,
                resultado.horasAcademicasJornadaRegular()
        );

        assertEquals(
                1L,
                resultado.horasAcademicasContrajornada()
        );
    }

    @Test
    void filtraClasesPorAsignaturaSeleccionada() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID matematicas = UUID.randomUUID();
        UUID ingles = UUID.randomUUID();

        InformeHorariosFiltroRequest filtroAsignatura =
                new InformeHorariosFiltroRequest(
                        null, null, null, null,
                        null, null, null, matematicas,
                        null, null, null
                );

        var resultado = generar(
                filtroAsignatura,
                List.of(
                        clase(docente, grupo, matematicas,
                                "LUNES", "REGULAR", 7, false),
                        clase(docente, grupo, ingles,
                                "MARTES", "REGULAR", 8, false)
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(1, resultado.totalAsignaturas());
        assertEquals(1L, resultado.horasAcademicasProgramadas());

        assertEquals(
                matematicas,
                resultado.clasesProgramadas()
                        .get(0).asignaturaId()
        );
    }

    @Test
    void filtraUnicamenteHorariosPublicados() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        var publicada = clase(
                docente, grupo, asignatura,
                "LUNES", "REGULAR", 7, false
        );

        var borrador = new InformeHorarioClaseDetalleResponse(
                UUID.randomUUID(),
                1,
                "BORRADOR",
                docente,
                "Docente de prueba",
                grupo,
                "6",
                "6A",
                asignatura,
                "Matematicas",
                "MARTES",
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                "REGULAR",
                UUID.randomUUID(),
                "Aula de prueba",
                false
        );

        InformeHorariosResponse resultado = generar(
                filtros(null, null),
                List.of(publicada, borrador)
        );

        assertEquals(1, resultado.totalClasesProgramadas());

        assertEquals(
                "PUBLICADO",
                resultado.clasesProgramadas()
                        .get(0).estadoHorario()
        );

        assertEquals(
                "LUNES",
                resultado.clasesProgramadas()
                        .get(0).diaSemana()
        );
    }

    @Test
    void filtraUnicamenteHorariosEnBorrador() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        var publicada = clase(
                docente, grupo, asignatura,
                "LUNES", "REGULAR", 7, false
        );

        var borrador = conEstado(
                clase(docente, grupo, asignatura,
                        "MARTES", "REGULAR", 8, false),
                "BORRADOR"
        );

        var filtrosBorrador = new InformeHorariosFiltroRequest(
                null, null, null, null,
                null, null, null, null,
                null, null, "BORRADOR"
        );

        var resultado = generar(
                filtrosBorrador,
                List.of(publicada, borrador)
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(
                "BORRADOR",
                resultado.clasesProgramadas().get(0).estadoHorario()
        );
    }

    @Test
    void conservaTodosLosEstadosSiNoSeAplicaFiltro() {

        UUID docente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        var publicada = clase(
                docente, grupo, asignatura,
                "LUNES", "REGULAR", 7, false
        );

        var borrador = conEstado(
                clase(docente, grupo, asignatura,
                        "MARTES", "REGULAR", 8, false),
                "BORRADOR"
        );

        var sinFiltroEstado = new InformeHorariosFiltroRequest(
                null, null, null, null,
                null, null, null, null,
                null, null, null
        );

        var resultado = generar(
                sinFiltroEstado,
                List.of(publicada, borrador)
        );

        assertEquals(2, resultado.totalClasesProgramadas());
        assertEquals(
                2L,
                resultado.horasAcademicasProgramadas()
        );
    }

    private InformeHorarioClaseDetalleResponse conEstado(
            InformeHorarioClaseDetalleResponse original,
            String estado) {

        return new InformeHorarioClaseDetalleResponse(
                original.horarioId(),
                original.versionHorario(),
                estado,
                original.docenteId(),
                original.nombreDocente(),
                original.grupoId(),
                original.grado(),
                original.nombreGrupo(),
                original.asignaturaId(),
                original.nombreAsignatura(),
                original.diaSemana(),
                original.horaInicio(),
                original.horaFin(),
                original.jornada(),
                original.aulaId(),
                original.nombreAula(),
                original.esHoraExtra()
        );
    }

    @Test
    void filtraClasesPorDocenteSeleccionado() {

        UUID docenteSeleccionado = UUID.randomUUID();
        UUID otroDocente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID asignatura = UUID.randomUUID();

        var filtroDocente = new InformeHorariosFiltroRequest(
                null, null, null, null,
                docenteSeleccionado, null, null, null,
                null, null, null
        );

        var resultado = generar(
                filtroDocente,
                List.of(
                        clase(docenteSeleccionado, grupo, asignatura,
                                "LUNES", "REGULAR", 7, false),
                        clase(otroDocente, grupo, asignatura,
                                "MARTES", "REGULAR", 8, false)
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(1, resultado.totalDocentes());
        assertEquals(1L, resultado.horasAcademicasProgramadas());

        assertEquals(
                docenteSeleccionado,
                resultado.clasesProgramadas()
                        .get(0).docenteId()
        );
    }

    @Test
    void aplicaTodosLosFiltrosCombinados() {

        UUID docente = UUID.randomUUID();
        UUID otroDocente = UUID.randomUUID();
        UUID grupo = UUID.randomUUID();
        UUID otroGrupo = UUID.randomUUID();
        UUID matematicas = UUID.randomUUID();
        UUID ingles = UUID.randomUUID();

        var filtrosCombinados = new InformeHorariosFiltroRequest(
                null, null, null, null,
                docente, null, grupo, matematicas,
                "REGULAR", "LUNES", "PUBLICADO"
        );

        var resultado = generar(
                filtrosCombinados,
                List.of(
                        // Única clase que cumple todos los filtros.
                        clase(docente, grupo, matematicas,
                                "LUNES", "REGULAR", 7, false),

                        // Grupo diferente.
                        clase(docente, otroGrupo, matematicas,
                                "LUNES", "REGULAR", 7, false),

                        // Docente diferente.
                        clase(otroDocente, grupo, matematicas,
                                "LUNES", "REGULAR", 7, false),

                        // Asignatura diferente.
                        clase(docente, grupo, ingles,
                                "LUNES", "REGULAR", 7, false),

                        // Día diferente.
                        clase(docente, grupo, matematicas,
                                "MARTES", "REGULAR", 7, false),

                        // Jornada diferente.
                        clase(docente, grupo, matematicas,
                                "LUNES", "CONTRAJORNADA", 14, false),

                        // Horario sin publicar.
                        conEstado(
                                clase(docente, grupo, matematicas,
                                        "LUNES", "REGULAR", 8, false),
                                "BORRADOR"
                        )
                )
        );

        assertEquals(1, resultado.totalClasesProgramadas());
        assertEquals(1, resultado.totalDocentes());
        assertEquals(1, resultado.totalGrupos());
        assertEquals(1, resultado.totalAsignaturas());
        assertEquals(1L, resultado.horasAcademicasProgramadas());

        var claseSeleccionada =
                resultado.clasesProgramadas().get(0);

        assertEquals(docente, claseSeleccionada.docenteId());
        assertEquals(grupo, claseSeleccionada.grupoId());
        assertEquals(matematicas, claseSeleccionada.asignaturaId());
        assertEquals("LUNES", claseSeleccionada.diaSemana());
        assertEquals("REGULAR", claseSeleccionada.jornada());
        assertEquals("PUBLICADO", claseSeleccionada.estadoHorario());
    }

}
