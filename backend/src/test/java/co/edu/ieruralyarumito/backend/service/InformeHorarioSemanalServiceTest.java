package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioCeldaSemanalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorarioSemanalServiceTest {

    private final InformeHorarioSemanalService service =
            new InformeHorarioSemanalService();

    @Test
    void agrupaClasesDeLaMismaFranja() {
        var primera = clase(
                "LUNES", "REGULAR", 7, 8
        );

        var segunda = clase(
                "LUNES", "REGULAR", 7, 8
        );

        List<InformeHorarioCeldaSemanalResponse> celdas =
                service.construirCuadricula(
                        List.of(primera, segunda)
                );

        assertEquals(1, celdas.size());
        assertEquals(2, celdas.get(0).clases().size());
        assertTrue(celdas.get(0).tieneVariasClases());
    }

    @Test
    void separaJornadaRegularDeContrajornada() {
        var regular = clase(
                "MARTES", "REGULAR", 7, 8
        );

        var contrajornada = clase(
                "MARTES", "CONTRAJORNADA", 14, 15
        );

        var celdas = service.construirCuadricula(
                List.of(contrajornada, regular)
        );

        assertEquals(2, celdas.size());
        assertEquals("REGULAR", celdas.get(0).jornada());
        assertEquals("CONTRAJORNADA", celdas.get(1).jornada());
    }

    @Test
    void ordenaDiasDeLunesAViernes() {
        var viernes = clase(
                "VIERNES", "REGULAR", 7, 8
        );

        var lunes = clase(
                "LUNES", "REGULAR", 7, 8
        );

        var miercoles = clase(
                "MIERCOLES", "REGULAR", 7, 8
        );

        var celdas = service.construirCuadricula(
                List.of(viernes, miercoles, lunes)
        );

        assertEquals(
                List.of("LUNES", "MIERCOLES", "VIERNES"),
                celdas.stream()
                        .map(
                                InformeHorarioCeldaSemanalResponse
                                        ::diaSemana
                        )
                        .toList()
        );
    }

    @Test
    void conservaHorasExactasDeCadaFranja() {
        var programada = clase(
                "JUEVES", "REGULAR", 9, 10
        );

        var celdas = service.construirCuadricula(
                List.of(programada)
        );

        assertEquals(
                LocalTime.of(9, 0),
                celdas.get(0).horaInicio()
        );

        assertEquals(
                LocalTime.of(10, 0),
                celdas.get(0).horaFin()
        );
    }

    @Test
    void noInventaEspaciosVacios() {
        var celdas = service.construirCuadricula(
                List.of()
        );

        assertTrue(celdas.isEmpty());
    }

    @Test
    void rechazaJornadasDesconocidas() {
        var programada = clase(
                "LUNES", "NO_DEFINIDA", 7, 8
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.construirCuadricula(
                        List.of(programada)
                )
        );
    }

    @Test
    void rechazaFranjasConHorasInvertidas() {
        var programada = clase(
                "LUNES", "REGULAR", 10, 9
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.construirCuadricula(
                        List.of(programada)
                )
        );
    }

    @Test
    void protegeLasClasesDeCadaCelda() {
        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        clases.add(
                clase("LUNES", "REGULAR", 7, 8)
        );

        var celda = new InformeHorarioCeldaSemanalResponse(
                "LUNES",
                LocalTime.of(7, 0),
                LocalTime.of(8, 0),
                "REGULAR",
                clases
        );

        clases.clear();

        assertEquals(1, celda.clases().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> celda.clases().clear()
        );
    }

    private InformeHorarioClaseDetalleResponse clase(
            String dia,
            String jornada,
            int horaInicio,
            int horaFin
    ) {
        return new InformeHorarioClaseDetalleResponse(
                UUID.randomUUID(),
                1,
                "PUBLICADO",
                UUID.randomUUID(),
                "Docente de prueba",
                UUID.randomUUID(),
                "10",
                "10A",
                UUID.randomUUID(),
                "Media Tecnica",
                dia,
                LocalTime.of(horaInicio, 0),
                LocalTime.of(horaFin, 0),
                jornada,
                UUID.randomUUID(),
                "Aula de prueba",
                false
        );
    }
}
