package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.CrearFranjaHorariaRequest;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import co.edu.ieruralyarumito.backend.dto.ActualizarFranjaHorariaRequest;

@ExtendWith(MockitoExtension.class)
class FranjaHorariaServiceTest {

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    @Mock
    private TurnoRepository turnoRepository;

    private FranjaHorariaService franjaHorariaService;

    @BeforeEach
    void setUp() {
        franjaHorariaService =
                new FranjaHorariaService(
                        franjaHorariaRepository,
                        turnoRepository
                );
    }

    @Test
    void debeRegistrarFranjaAcademicaValida() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(turnoId, 1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(turnoId, 1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of());

        when(franjaHorariaRepository.save(any(FranjaHoraria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FranjaHorariaResponse response =
                franjaHorariaService.registrarFranjaHoraria(request);

        assertEquals(turnoId, response.getTurnoId());
        assertEquals(1, response.getNumero());
        assertFalse(response.isEsDescanso());
        assertEquals(
                new BigDecimal("1.00"),
                response.getHorasAcademicasEquivalentes()
        );
    }

    @Test
    void debeRechazarTurnoInexistente() {

        UUID turnoId = UUID.randomUUID();

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );

        verify(franjaHorariaRepository, never())
                .save(any());
    }

    @Test
    void debeRechazarFranjaAcademicaConNumeroCero() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        request.setNumero(0);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarDescansoConNumeroDistintoDeCero() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestDescanso(turnoId);

        request.setNumero(2);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarDescansoConEquivalenciaMayorQueCero() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestDescanso(turnoId);

        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00")
        );

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarNumeroAcademicoDuplicado() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(turnoId, 1))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarOrdenDuplicadoDentroDelTurno() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(turnoId, 1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(turnoId, 1))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarFranjaSolapada() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        FranjaHoraria existente =
                new FranjaHoraria();

        existente.setTurno(turno);
        existente.setNumero(2);
        existente.setHoraInicio(LocalTime.of(7, 0));
        existente.setHoraFin(LocalTime.of(7, 55));
        existente.setEsDescanso(false);
        existente.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00")
        );
        existente.setOrden(2);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(turnoId, 1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(turnoId, 1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of(existente));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debePermitirFranjasConsecutivasSinSolapamiento() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        request.setNumero(2);
        request.setOrden(2);
        request.setHoraInicio(LocalTime.of(7, 25));
        request.setHoraFin(LocalTime.of(8, 20));

        FranjaHoraria existente =
                new FranjaHoraria();

        existente.setTurno(turno);
        existente.setNumero(1);
        existente.setHoraInicio(LocalTime.of(6, 30));
        existente.setHoraFin(LocalTime.of(7, 25));
        existente.setEsDescanso(false);
        existente.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00")
        );
        existente.setOrden(1);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(turnoId, 2))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(turnoId, 2))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of(existente));

        when(franjaHorariaRepository.save(any(FranjaHoraria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRechazarDuracionAcademicaIncompatibleConTurno() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(turnoId);

        request.setHoraFin(LocalTime.of(7, 20));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .registrarFranjaHoraria(request)
        );
    }

    @Test
    void debeRegistrarDescansoValido() {

        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestDescanso(turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(turnoId, 4))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of());

        when(franjaHorariaRepository.save(any(FranjaHoraria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FranjaHorariaResponse response =
                franjaHorariaService.registrarFranjaHoraria(request);

        assertTrue(response.isEsDescanso());
        assertEquals(0, response.getNumero());
        assertEquals(
                new BigDecimal("0.00"),
                response.getHorasAcademicasEquivalentes()
        );
    }

    @Test
    void debePermitirMismoOrdenEnTurnosDiferentes() {

        UUID turnoId1 =
                UUID.randomUUID();

        UUID turnoId2 =
                UUID.randomUUID();

        Turno turno1 =
                crearTurno(turnoId1);

        Turno turno2 =
                crearTurno(turnoId2);

        CrearFranjaHorariaRequest request1 =
                crearRequestAcademico(
                        turnoId1);

        CrearFranjaHorariaRequest request2 =
                crearRequestAcademico(
                        turnoId2);

        when(turnoRepository.findById(turnoId1))
                .thenReturn(
                        Optional.of(turno1));

        when(turnoRepository.findById(turnoId2))
                .thenReturn(
                        Optional.of(turno2));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(
                        turnoId1,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(
                        turnoId2,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(
                        turnoId1,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(
                        turnoId2,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId1))
                .thenReturn(
                        List.of());

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId2))
                .thenReturn(
                        List.of());

        when(franjaHorariaRepository
                .save(any(FranjaHoraria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        assertDoesNotThrow(
                () ->
                        franjaHorariaService
                                .registrarFranjaHoraria(
                                        request1));

        assertDoesNotThrow(
                () ->
                        franjaHorariaService
                                .registrarFranjaHoraria(
                                        request2));

        verify(franjaHorariaRepository)
                .existsByTurno_IdAndOrden(
                        turnoId1,
                        1);

        verify(franjaHorariaRepository)
                .existsByTurno_IdAndOrden(
                        turnoId2,
                        1);
    }

    @Test
    void debeRegistrarUnaSolaFranjaReutilizableDuranteLaSemana() {

        UUID turnoId =
                UUID.randomUUID();

        Turno turno =
                crearTurno(turnoId);

        CrearFranjaHorariaRequest request =
                crearRequestAcademico(
                        turnoId);

        when(turnoRepository.findById(turnoId))
                .thenReturn(
                        Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumero(
                        turnoId,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrden(
                        turnoId,
                        1))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(
                        List.of());

        when(franjaHorariaRepository
                .save(any(FranjaHoraria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        FranjaHorariaResponse response =
                franjaHorariaService
                        .registrarFranjaHoraria(
                                request);

        assertEquals(
                turnoId,
                response.getTurnoId());

        verify(franjaHorariaRepository, times(1))
                .save(any(FranjaHoraria.class));
    }

    @Test
    void actualizarFranja_debeExcluirseASiMismaAlValidarSolapamiento() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        FranjaHoraria franja =
                mock(FranjaHoraria.class);

        when(franja.getId())
                .thenReturn(franjaId);

        when(franja.getTurno())
                .thenReturn(turno);

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(1);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        request.setOrden(1);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrdenAndIdNot(
                        turnoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of(franja));

        when(franjaHorariaRepository.save(franja))
                .thenReturn(franja);

        assertDoesNotThrow(
                () -> franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request));
    }

    @Test
    void actualizarFranja_debeRechazarNumeroDuplicado() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        FranjaHoraria franja =
                new FranjaHoraria();

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(2);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        request.setOrden(1);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoId,
                        2,
                        franjaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request));

        verify(franjaHorariaRepository, never())
                .save(any());
    }

    @Test
    void actualizarFranja_debeRechazarOrdenDuplicado() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        FranjaHoraria franja =
                new FranjaHoraria();

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(1);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        request.setOrden(2);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrdenAndIdNot(
                        turnoId,
                        2,
                        franjaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request));

        verify(franjaHorariaRepository, never())
                .save(any());
    }

    @Test
    void actualizarFranja_debeRechazarSolapamientoConOtraFranja() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        Turno turno = crearTurno(turnoId);

        FranjaHoraria franja =
                new FranjaHoraria();

        FranjaHoraria otra =
                new FranjaHoraria();

        otra.setTurno(turno);
        otra.setNumero(2);
        otra.setHoraInicio(
                LocalTime.of(7, 0));
        otra.setHoraFin(
                LocalTime.of(7, 55));
        otra.setEsDescanso(false);
        otra.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        otra.setOrden(2);

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(1);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        request.setOrden(1);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrdenAndIdNot(
                        turnoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId))
                .thenReturn(List.of(otra));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request));

        verify(franjaHorariaRepository, never())
                .save(any());
    }

    @Test
    void actualizarFranja_debePermitirCambiarDeTurno() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoAnteriorId = UUID.randomUUID();
        UUID turnoNuevoId = UUID.randomUUID();

        Turno turnoAnterior =
                crearTurno(turnoAnteriorId);

        Turno turnoNuevo =
                crearTurno(turnoNuevoId);

        turnoNuevo.setNombre(
                "Contrajornada");

        FranjaHoraria franja =
                new FranjaHoraria();

        franja.setTurno(turnoAnterior);
        franja.setNumero(1);
        franja.setHoraInicio(
                LocalTime.of(6, 30));
        franja.setHoraFin(
                LocalTime.of(7, 25));
        franja.setEsDescanso(false);
        franja.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        franja.setOrden(1);

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoNuevoId);
        request.setNumero(1);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        request.setOrden(1);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoNuevoId))
                .thenReturn(Optional.of(turnoNuevo));

        when(franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoNuevoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .existsByTurno_IdAndOrdenAndIdNot(
                        turnoNuevoId,
                        1,
                        franjaId))
                .thenReturn(false);

        when(franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(
                        turnoNuevoId))
                .thenReturn(List.of());

        when(franjaHorariaRepository
                .save(any(FranjaHoraria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        FranjaHorariaResponse response =
                franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request);

        assertEquals(
                turnoNuevoId,
                response.getTurnoId());

        assertEquals(
                "Contrajornada",
                response.getTurnoNombre());

        verify(turnoRepository)
                .findById(turnoNuevoId);
    }

    @Test
    void actualizarFranja_debeRechazarDescansoConDuracionInvalida() {

        UUID franjaId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        Turno turno =
                crearTurno(turnoId);

        FranjaHoraria franja =
                new FranjaHoraria();

        ActualizarFranjaHorariaRequest request =
                new ActualizarFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(0);
        request.setHoraInicio(
                LocalTime.of(9, 15));

        // 25 minutos, pero el Turno exige 30.
        request.setHoraFin(
                LocalTime.of(9, 40));

        request.setEsDescanso(true);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("0.00"));
        request.setOrden(4);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(turnoRepository.findById(turnoId))
                .thenReturn(Optional.of(turno));

        assertThrows(
                IllegalArgumentException.class,
                () -> franjaHorariaService
                        .actualizarFranjaHoraria(
                                franjaId,
                                request));

        verify(franjaHorariaRepository, never())
                .save(any());
    }

    private Turno crearTurno(UUID id) {

        Turno turno = new Turno();

        // El ID se obtiene normalmente por persistencia.
        // Para estas pruebas únicamente necesitamos los datos funcionales.
        turno.setNombre("Mañana");
        turno.setHoraInicio(LocalTime.of(6, 30));
        turno.setHoraFin(LocalTime.of(12, 30));
        turno.setDuracionClaseMinutos(55);
        turno.setClasesPorDia(6);
        turno.setClasesAntesDeDescanso(3);
        turno.setDuracionDescansoMinutos(30);

        return turno;
    }

    private CrearFranjaHorariaRequest crearRequestAcademico(
            UUID turnoId) {

        CrearFranjaHorariaRequest request =
                new CrearFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(1);
        request.setHoraInicio(LocalTime.of(6, 30));
        request.setHoraFin(LocalTime.of(7, 25));
        request.setEsDescanso(false);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00")
        );
        request.setOrden(1);

        return request;
    }

    private CrearFranjaHorariaRequest crearRequestDescanso(
            UUID turnoId) {

        CrearFranjaHorariaRequest request =
                new CrearFranjaHorariaRequest();

        request.setTurnoId(turnoId);
        request.setNumero(0);
        request.setHoraInicio(LocalTime.of(9, 15));
        request.setHoraFin(LocalTime.of(9, 45));
        request.setEsDescanso(true);
        request.setHorasAcademicasEquivalentes(
                new BigDecimal("0.00")
        );
        request.setOrden(4);

        return request;
    }
}
