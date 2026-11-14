package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorarioPorGrupoServiceTest {

    private final InformeHorarioPorGrupoService service =
            new InformeHorarioPorGrupoService();

    @Test
    void separaClasesDeGruposDiferentes() {

        UUID horario = UUID.randomUUID();
        UUID grupo701 = UUID.randomUUID();
        UUID grupo1101 = UUID.randomUUID();

        var clases = List.of(
                clase(horario, 1, grupo701, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS"),
                clase(horario, 1, grupo1101, "11", "1101",
                        "LUNES", "REGULAR", 6, 30,
                        "QUÍMICA")
        );

        var resultado = service.organizar(clases);

        assertEquals(2, resultado.size());

        assertTrue(
                resultado.stream().allMatch(
                        grupo -> grupo.clases().size() == 1
                )
        );

        assertTrue(
                resultado.stream().noneMatch(
                        InformeHorarioPorGrupoService
                                .HorarioGrupo::tieneConflictos
                )
        );
    }

    @Test
    void separaVersionesDelMismoGrupo() {

        UUID grupo = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        var resultado = service.organizar(List.of(
                clase(horario, 1, grupo, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS"),
                clase(horario, 2, grupo, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "INGLÉS")
        ));

        assertEquals(2, resultado.size());

        assertEquals(1, resultado.get(0).versionHorario());
        assertEquals(2, resultado.get(1).versionHorario());

        assertFalse(resultado.get(0).tieneConflictos());
        assertFalse(resultado.get(1).tieneConflictos());
    }

    @Test
    void detectaDosClasesEnLaMismaFranjaDelGrupo() {

        UUID grupo = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        var resultado = service.organizar(List.of(
                clase(horario, 1, grupo, "7", "701",
                        "MARTES", "REGULAR", 7, 25,
                        "MATEMÁTICAS"),
                clase(horario, 1, grupo, "7", "701",
                        "MARTES", "REGULAR", 7, 25,
                        "INGLÉS")
        ));

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).tieneConflictos());

        assertEquals(
                2,
                resultado.get(0).conflictos()
                        .get(0).clases().size()
        );

        // Ninguna clase debe desaparecer.
        assertEquals(2, resultado.get(0).clases().size());
    }

    @Test
    void conservaFranjasRealesDeCincuentaYCincoMinutos() {

        UUID grupo = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        var resultado = service.organizar(List.of(
                clase(horario, 1, grupo, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS")
        ));

        var programada = resultado.get(0).clases().get(0);

        assertEquals(
                LocalTime.of(6, 30),
                programada.horaInicio()
        );

        assertEquals(
                LocalTime.of(7, 25),
                programada.horaFin()
        );
    }

    @Test
    void organizaLosDiasDeLunesAViernes() {

        UUID grupo = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        var resultado = service.organizar(List.of(
                clase(horario, 1, grupo, "7", "701",
                        "VIERNES", "REGULAR", 6, 30,
                        "ÉTICA"),
                clase(horario, 1, grupo, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS"),
                clase(horario, 1, grupo, "7", "701",
                        "MIÉRCOLES", "REGULAR", 6, 30,
                        "CIENCIAS NATURALES")
        ));

        var clases = resultado.get(0).clases();

        assertEquals("LUNES", clases.get(0).diaSemana());
        assertEquals("MIÉRCOLES", clases.get(1).diaSemana());
        assertEquals("VIERNES", clases.get(2).diaSemana());
    }

    @Test
    void noModificaLaListaOriginal() {

        UUID grupo = UUID.randomUUID();
        UUID horario = UUID.randomUUID();

        var original = new java.util.ArrayList<
                InformeHorarioClaseDetalleResponse>();

        original.add(
                clase(horario, 1, grupo, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS")
        );

        var resultado = service.organizar(original);

        original.clear();

        assertEquals(1, resultado.get(0).clases().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.get(0).clases().clear()
        );
    }

    @Test
    void permiteConsultaSinClases() {

        assertTrue(service.organizar(List.of()).isEmpty());
        assertTrue(service.organizar(null).isEmpty());
    }

    private InformeHorarioClaseDetalleResponse clase(
            UUID horarioId,
            int version,
            UUID grupoId,
            String grado,
            String grupo,
            String dia,
            String jornada,
            int hora,
            int minuto,
            String asignatura) {

        LocalTime inicio = LocalTime.of(hora, minuto);

        return new InformeHorarioClaseDetalleResponse(
                horarioId,
                version,
                "PUBLICADO",
                UUID.randomUUID(),
                "Docente de prueba",
                grupoId,
                grado,
                grupo,
                UUID.randomUUID(),
                asignatura,
                dia,
                inicio,
                inicio.plusMinutes(55),
                jornada,
                UUID.randomUUID(),
                "Aula 101",
                false
        );
    }


    @Test
    void seleccionaUnicamenteElGrupoSolicitado() {

        UUID horario = UUID.randomUUID();
        UUID grupo701 = UUID.randomUUID();
        UUID grupo1101 = UUID.randomUUID();

        var clases = List.of(
                clase(horario, 1, grupo701, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS"),
                clase(horario, 1, grupo1101, "11", "1101",
                        "LUNES", "REGULAR", 6, 30,
                        "QUÍMICA")
        );

        var resultado = service.seleccionarPublicadoPorGrupo(
                clases, grupo1101
        );

        assertTrue(resultado.isPresent());
        assertEquals(grupo1101, resultado.get().grupoId());
        assertEquals("1101", resultado.get().nombreGrupo());
        assertEquals(1, resultado.get().clases().size());
        assertEquals(
                "QUÍMICA",
                resultado.get().clases().get(0).nombreAsignatura()
        );
    }

    @Test
    void devuelveVacioSiElGrupoNoTieneHorario() {

        UUID grupo701 = UUID.randomUUID();
        UUID grupoSolicitado = UUID.randomUUID();

        var clases = List.of(
                clase(UUID.randomUUID(), 1, grupo701,
                        "7", "701", "LUNES", "REGULAR",
                        6, 30, "MATEMÁTICAS")
        );

        var resultado = service.seleccionarPublicadoPorGrupo(
                clases, grupoSolicitado
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void rechazaVariasVersionesPublicadas() {

        UUID horario = UUID.randomUUID();
        UUID grupo701 = UUID.randomUUID();

        var clases = List.of(
                clase(horario, 1, grupo701, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "MATEMÁTICAS"),
                clase(horario, 2, grupo701, "7", "701",
                        "LUNES", "REGULAR", 6, 30,
                        "INGLÉS")
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.seleccionarPublicadoPorGrupo(
                        clases, grupo701
                )
        );
    }


    @Test
    void noPermiteConsultarHorarioSinPublicar() {

        UUID grupo701 = UUID.randomUUID();

        var publicada = clase(
                UUID.randomUUID(),
                1,
                grupo701,
                "7",
                "701",
                "LUNES",
                "REGULAR",
                6,
                30,
                "MATEMÁTICAS"
        );

        var borrador = new InformeHorarioClaseDetalleResponse(
                publicada.horarioId(),
                publicada.versionHorario(),
                "BORRADOR",
                publicada.docenteId(),
                publicada.nombreDocente(),
                publicada.grupoId(),
                publicada.grado(),
                publicada.nombreGrupo(),
                publicada.asignaturaId(),
                publicada.nombreAsignatura(),
                publicada.diaSemana(),
                publicada.horaInicio(),
                publicada.horaFin(),
                publicada.jornada(),
                publicada.aulaId(),
                publicada.nombreAula(),
                publicada.esHoraExtra()
        );

        var resultado = service.seleccionarPublicadoPorGrupo(
                List.of(borrador),
                grupo701
        );

        assertTrue(
                resultado.isEmpty(),
                "Un horario sin publicar no debe ser visible."
        );
    }

}
