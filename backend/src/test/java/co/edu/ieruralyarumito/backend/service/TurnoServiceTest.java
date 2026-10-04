package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.TurnoResponse;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    private TurnoService turnoService;

    @BeforeEach
    void setUp() {

        turnoService =
                new TurnoService(
                        turnoRepository,
                        franjaHorariaRepository);
    }

    @Test
    void debeRegistrarTurnoValido() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setNombre(
                "  Mañana  ");

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(false);

        when(turnoRepository
                .save(any(Turno.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        TurnoResponse response =
                turnoService
                        .registrarTurno(request);

        assertEquals(
                "Mañana",
                response.getNombre());

        assertEquals(
                LocalTime.of(6, 30),
                response.getHoraInicio());

        assertEquals(
                LocalTime.of(12, 30),
                response.getHoraFin());

        assertEquals(
                55,
                response.getDuracionClaseMinutos());

        assertEquals(
                6,
                response.getClasesPorDia());

        assertEquals(
                3,
                response.getClasesAntesDeDescanso());

        assertEquals(
                30,
                response.getDuracionDescansoMinutos());

        verify(turnoRepository)
                .existsByNombreIgnoreCase(
                        "Mañana");

        verify(turnoRepository)
                .save(any(Turno.class));
    }

    @Test
    void debeNormalizarEspaciosInternosDelNombre() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setNombre(
                "  Jornada   Única  ");

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Jornada Única"))
                .thenReturn(false);

        when(turnoRepository
                .save(any(Turno.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        TurnoResponse response =
                turnoService
                        .registrarTurno(request);

        assertEquals(
                "Jornada Única",
                response.getNombre());
    }

    @Test
    void debeRechazarNombreDuplicado() {

        CrearTurnoRequest request =
                crearRequestValido();

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        turnoService
                                .registrarTurno(
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void debeRechazarHoraInicioIgualAHoraFin() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setHoraInicio(
                LocalTime.of(6, 30));

        request.setHoraFin(
                LocalTime.of(6, 30));

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .registrarTurno(
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void debeRechazarHoraInicioPosteriorAHoraFin() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setHoraInicio(
                LocalTime.of(13, 0));

        request.setHoraFin(
                LocalTime.of(12, 30));

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .registrarTurno(
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void debeRechazarMasClasesAntesDelDescansoQueClasesPorDia() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setClasesPorDia(
                6);

        request.setClasesAntesDeDescanso(
                7);

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .registrarTurno(
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void debeConsultarTurnoExistente() {

        UUID id =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        when(turnoRepository
                .findById(id))
                .thenReturn(
                        Optional.of(turno));

        TurnoResponse response =
                turnoService
                        .consultarTurno(id);

        assertEquals(
                "Mañana",
                response.getNombre());

        assertEquals(
                55,
                response.getDuracionClaseMinutos());
    }

    @Test
    void debeRechazarConsultaDeTurnoInexistente() {

        UUID id =
                UUID.randomUUID();

        when(turnoRepository
                .findById(id))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        turnoService
                                .consultarTurno(id));
    }

    @Test
    void debeRechazarNombreDuplicadoDespuesDeNormalizar() {

        CrearTurnoRequest request =
                crearRequestValido();

        request.setNombre(
                "   Mañana   ");

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Mañana"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        turnoService
                                .registrarTurno(
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void actualizarTurno_debePermitirCambiosCompatiblesConFranjasExistentes() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        FranjaHoraria franjaAcademica =
                crearFranjaAcademica(
                        turno);

        FranjaHoraria descanso =
                crearFranjaDescanso(
                        turno);

        ActualizarTurnoRequest request =
                crearRequestActualizacionValido();

        when(turnoRepository
                .findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(
                        List.of(
                                franjaAcademica,
                                descanso));

        when(turnoRepository
                .save(turno))
                .thenReturn(turno);

        TurnoResponse response =
                assertDoesNotThrow(
                        () ->
                                turnoService
                                        .actualizarTurno(
                                                turnoId,
                                                request));

        assertEquals(
                55,
                response.getDuracionClaseMinutos());

        assertEquals(
                30,
                response.getDuracionDescansoMinutos());

        verify(turnoRepository)
                .save(turno);
    }

    @Test
    void actualizarTurno_debeRechazarDuracionAcademicaIncompatibleConFranjasExistentes() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        FranjaHoraria franjaAcademica =
                crearFranjaAcademica(
                        turno);

        ActualizarTurnoRequest request =
                crearRequestActualizacionValido();

        request.setDuracionClaseMinutos(
                50);

        when(turnoRepository
                .findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(
                        List.of(
                                franjaAcademica));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .actualizarTurno(
                                        turnoId,
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void actualizarTurno_debeRechazarDuracionDescansoIncompatibleConFranjasExistentes() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        FranjaHoraria descanso =
                crearFranjaDescanso(
                        turno);

        ActualizarTurnoRequest request =
                crearRequestActualizacionValido();

        request.setDuracionDescansoMinutos(
                20);

        when(turnoRepository
                .findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(
                        List.of(
                                descanso));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .actualizarTurno(
                                        turnoId,
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));
    }

    @Test
    void actualizarTurno_debeRechazarNombreDuplicado() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        ActualizarTurnoRequest request =
                crearRequestActualizacionValido();

        request.setNombre(
                "Tarde");

        when(turnoRepository
                .findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        when(turnoRepository
                .existsByNombreIgnoreCase(
                        "Tarde"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        turnoService
                                .actualizarTurno(
                                        turnoId,
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));

        verify(franjaHorariaRepository, never())
                .findByTurno_IdOrderByOrdenAsc(
                        turnoId);
    }

    @Test
    void actualizarTurno_debeRechazarEstructuraInvalida() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurnoValido();

        ActualizarTurnoRequest request =
                crearRequestActualizacionValido();

        request.setClasesPorDia(
                6);

        request.setClasesAntesDeDescanso(
                7);

        when(turnoRepository
                .findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turnoService
                                .actualizarTurno(
                                        turnoId,
                                        request));

        verify(turnoRepository, never())
                .save(any(Turno.class));

        verify(franjaHorariaRepository, never())
                .findByTurno_IdOrderByOrdenAsc(
                        turnoId);
    }

    private CrearTurnoRequest crearRequestValido() {

        CrearTurnoRequest request =
                new CrearTurnoRequest();

        request.setNombre(
                "Mañana");

        request.setHoraInicio(
                LocalTime.of(6, 30));

        request.setHoraFin(
                LocalTime.of(12, 30));

        request.setDuracionClaseMinutos(
                55);

        request.setClasesPorDia(
                6);

        request.setClasesAntesDeDescanso(
                3);

        request.setDuracionDescansoMinutos(
                30);

        return request;
    }

    private ActualizarTurnoRequest crearRequestActualizacionValido() {

        ActualizarTurnoRequest request =
                new ActualizarTurnoRequest();

        request.setNombre(
                "Mañana");

        request.setHoraInicio(
                LocalTime.of(6, 30));

        request.setHoraFin(
                LocalTime.of(12, 30));

        request.setDuracionClaseMinutos(
                55);

        request.setClasesPorDia(
                6);

        request.setClasesAntesDeDescanso(
                3);

        request.setDuracionDescansoMinutos(
                30);

        return request;
    }

    private Turno crearTurnoValido() {

        Turno turno =
                new Turno();

        turno.setNombre(
                "Mañana");

        turno.setHoraInicio(
                LocalTime.of(6, 30));

        turno.setHoraFin(
                LocalTime.of(12, 30));

        turno.setDuracionClaseMinutos(
                55);

        turno.setClasesPorDia(
                6);

        turno.setClasesAntesDeDescanso(
                3);

        turno.setDuracionDescansoMinutos(
                30);

        return turno;
    }

    private FranjaHoraria crearFranjaAcademica(
            Turno turno) {

        FranjaHoraria franja =
                new FranjaHoraria();

        franja.setTurno(
                turno);

        franja.setNumero(
                1);

        franja.setHoraInicio(
                LocalTime.of(6, 30));

        franja.setHoraFin(
                LocalTime.of(7, 25));

        franja.setEsDescanso(
                false);

        franja.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));

        franja.setOrden(
                1);

        return franja;
    }

    private FranjaHoraria crearFranjaDescanso(
            Turno turno) {

        FranjaHoraria franja =
                new FranjaHoraria();

        franja.setTurno(
                turno);

        franja.setNumero(
                0);

        franja.setHoraInicio(
                LocalTime.of(9, 15));

        franja.setHoraFin(
                LocalTime.of(9, 45));

        franja.setEsDescanso(
                true);

        franja.setHorasAcademicasEquivalentes(
                new BigDecimal("0.00"));

        franja.setOrden(
                4);

        return franja;
    }
}
