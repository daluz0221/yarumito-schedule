package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.DetalleConflicto;
import co.edu.ieruralyarumito.backend.entity.Conflicto;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.ConflictoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConflictoServiceTest {

    @Mock
    private ConflictoRepository conflictoRepository;

    private ConflictoService conflictoService;

    @BeforeEach
    void setUp() {
        conflictoService =
                new ConflictoService(
                        conflictoRepository
                );
    }

    @Test
    void registrarConflicto_docenteOcupado_debeCrearError() {

        UUID horarioId = UUID.randomUUID();

        Horario horario = spy(new Horario());
        doReturn(horarioId)
                .when(horario)
                .getId();

        when(conflictoRepository
                .save(any(Conflicto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ConflictoResponse response =
                conflictoService.registrarConflicto(
                        horario,
                        TipoConflicto.DOCENTE_OCUPADO,
                        "El docente ya tiene una clase programada"
                );

        assertEquals(
                TipoConflicto.DOCENTE_OCUPADO,
                response.getTipo());

        assertEquals(
                SeveridadConflicto.ERROR,
                response.getSeveridad());

        assertFalse(
                response.isResuelto());

        assertNotNull(
                response.getFechaDeteccion());

        assertEquals(
                horarioId,
                response.getHorarioId());
    }

    @Test
    void registrarConflicto_aulaOcupada_debeCrearError() {

        UUID horarioId = UUID.randomUUID();

        Horario horario = spy(new Horario());
        doReturn(horarioId)
                .when(horario)
                .getId();

        when(conflictoRepository
                .save(any(Conflicto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ConflictoResponse response =
                conflictoService.registrarConflicto(
                        horario,
                        TipoConflicto.AULA_OCUPADA,
                        "El aula está ocupada"
                );

        assertEquals(
                SeveridadConflicto.ERROR,
                response.getSeveridad());
    }

    @Test
    void registrarConflicto_capacidadAula_debeCrearAdvertencia() {

        UUID horarioId = UUID.randomUUID();

        Horario horario = spy(new Horario());
        doReturn(horarioId)
                .when(horario)
                .getId();

        when(conflictoRepository
                .save(any(Conflicto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ConflictoResponse response =
                conflictoService.registrarConflicto(
                        horario,
                        TipoConflicto.CAPACIDAD_AULA,
                        "La cantidad de estudiantes supera la capacidad"
                );

        assertEquals(
                SeveridadConflicto.ADVERTENCIA,
                response.getSeveridad());
    }

    @Test
    void registrarConflicto_debeRechazarHorarioNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> conflictoService.registrarConflicto(
                        null,
                        TipoConflicto.DOCENTE_OCUPADO,
                        "Conflicto"
                )
        );

        verify(conflictoRepository, never())
                .save(any());
    }

    @Test
    void registrarConflicto_debeRechazarMensajeVacio() {

        Horario horario = new Horario();

        assertThrows(
                IllegalArgumentException.class,
                () -> conflictoService.registrarConflicto(
                        horario,
                        TipoConflicto.DOCENTE_OCUPADO,
                        "   "
                )
        );

        verify(conflictoRepository, never())
                .save(any());
    }

    @Test
    void marcarResuelto_debeActualizarConflicto() {

        UUID conflictoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();

        Horario horario = spy(new Horario());
        doReturn(horarioId)
                .when(horario)
                .getId();

        Conflicto conflicto =
                new Conflicto();

        conflicto.setHorario(horario);
        conflicto.setTipo(
                TipoConflicto.DOCENTE_OCUPADO);
        conflicto.setSeveridad(
                SeveridadConflicto.ERROR);
        conflicto.setMensaje(
                "Docente ocupado");
        conflicto.setResuelto(false);

        when(conflictoRepository
                .findById(conflictoId))
                .thenReturn(Optional.of(conflicto));

        when(conflictoRepository
                .save(conflicto))
                .thenReturn(conflicto);

        ConflictoResponse response =
                conflictoService.marcarResuelto(
                        conflictoId);

        assertTrue(
                response.isResuelto());
    }

    @Test
    void marcarResuelto_debeRechazarConflictoInexistente() {

        UUID conflictoId = UUID.randomUUID();

        when(conflictoRepository
                .findById(conflictoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> conflictoService
                        .marcarResuelto(conflictoId)
        );
    }

    @Test
    void listarPendientes_debeRetornarSoloPendientes() {

        UUID horarioId = UUID.randomUUID();

        when(conflictoRepository
                .findByHorario_IdAndResueltoOrderByFechaDeteccionDesc(
                        horarioId,
                        false))
                .thenReturn(List.of());

        List<ConflictoResponse> response =
                conflictoService.listarPendientes(
                        horarioId);

        assertTrue(
                response.isEmpty());

        verify(conflictoRepository)
                .findByHorario_IdAndResueltoOrderByFechaDeteccionDesc(
                        horarioId,
                        false);
    }

    @Test
    void registrarConflicto_guardaElDetalleParaElModal() {
        UUID horarioId = UUID.randomUUID();
        Horario horario = spy(new Horario());
        doReturn(horarioId).when(horario).getId();

        when(conflictoRepository.save(any(Conflicto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConflictoResponse response = conflictoService.registrarConflicto(
                horario,
                TipoConflicto.DOCENTE_OCUPADO,
                "Ana Gómez ya tiene clase el lunes, 06:30–07:25.",
                new DetalleConflicto(
                        "lunes",
                        "06:30–07:25",
                        "Ana Gómez",
                        "Matemáticas",
                        "Ana Gómez",
                        "Salón 6",
                        "601"));

        assertEquals("lunes", response.getDia());
        assertEquals("06:30–07:25", response.getFranja());
        assertEquals("Ana Gómez", response.getDocente());
        assertEquals("Matemáticas", response.getAsignatura());
        assertEquals("Salón 6", response.getAula());
        assertEquals("601", response.getGrupo());
    }
}