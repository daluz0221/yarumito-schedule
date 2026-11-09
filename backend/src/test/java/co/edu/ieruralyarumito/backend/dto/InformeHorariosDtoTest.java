package co.edu.ieruralyarumito.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformeHorariosDtoTest {

    @Test
    void permiteRangoFechasValido() {
        InformeHorariosFiltroRequest filtros = crearFiltros(
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 4, 3)
        );

        assertTrue(filtros.tieneRangoFechasValido());
        assertTrue(filtros.tieneRangoPersonalizado());
    }

    @Test
    void rechazaRangoConFechasInvertidas() {
        InformeHorariosFiltroRequest filtros = crearFiltros(
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 2, 2)
        );

        assertFalse(filtros.tieneRangoFechasValido());
    }

    @Test
    void permiteConsultaSinRangoPersonalizado() {
        InformeHorariosFiltroRequest filtros = crearFiltros(
                null, null
        );

        assertTrue(filtros.tieneRangoFechasValido());
        assertFalse(filtros.tieneRangoPersonalizado());
    }

    @Test
    void rechazaRangoIncompleto() {
        assertFalse(crearFiltros(
                LocalDate.of(2026, 2, 2), null
        ).tieneRangoFechasValido());

        assertFalse(crearFiltros(
                null, LocalDate.of(2026, 4, 3)
        ).tieneRangoFechasValido());
    }

    @Test
    void aceptaFechaInicialIgualALaFinal() {
        LocalDate fecha = LocalDate.of(2026, 2, 2);

        InformeHorariosFiltroRequest filtros =
                crearFiltros(fecha, fecha);

        assertTrue(filtros.tieneRangoFechasValido());
    }

    @Test
    void protegeLasListasDelInforme() {
        List<InformeHorarioClaseDetalleResponse> clases =
                new ArrayList<>();

        List<InformeHorarioCargaResponse> cargas =
                new ArrayList<>();

        InformeHorariosResponse informe =
                new InformeHorariosResponse(
                        UUID.randomUUID(),
                        "2026",
                        null,
                        null,
                        "I.E. Rural Yarumito",
                        "Principal",
                        crearFiltros(null, null),
                        clases,
                        cargas,
                        0,
                        0,
                        0,
                        0,
                        0L,
                        0L,
                        0L
                );

        assertNotNull(informe.clasesProgramadas());
        assertNotNull(informe.resumenCargaHoraria());

        assertThrows(
                UnsupportedOperationException.class,
                () -> informe.clasesProgramadas().add(null)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> informe.resumenCargaHoraria().add(null)
        );
    }

    @Test
    void reemplazaListasNulasPorListasVacias() {
        InformeHorariosResponse informe =
                new InformeHorariosResponse(
                        UUID.randomUUID(),
                        "2026",
                        null,
                        null,
                        "I.E. Rural Yarumito",
                        "Principal",
                        crearFiltros(null, null),
                        null,
                        null,
                        0,
                        0,
                        0,
                        0,
                        0L,
                        0L,
                        0L
                );

        assertTrue(informe.clasesProgramadas().isEmpty());
        assertTrue(informe.resumenCargaHoraria().isEmpty());
    }

    private InformeHorariosFiltroRequest crearFiltros(
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
}
