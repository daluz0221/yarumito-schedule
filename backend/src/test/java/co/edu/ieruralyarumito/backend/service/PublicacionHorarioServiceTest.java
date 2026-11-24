package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.PublicarHorarioResponse;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionHorarioServiceTest {

    private static final String CORREO = "rector@yarumito.edu.co";

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private ClaseProgramadaRepository claseProgramadaRepository;

    @Mock
    private ConflictoService conflictoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroAuditoriaService registroAuditoriaService;

    private PublicacionHorarioService publicacionHorarioService;

    @BeforeEach
    void setUp() {

        publicacionHorarioService =
                new PublicacionHorarioService(
                        horarioRepository,
                        claseProgramadaRepository,
                        conflictoService,
                        usuarioRepository,
                        registroAuditoriaService);
    }

    @Test
    void publicaHorarioBorradorSinConflictos() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));

        when(claseProgramadaRepository
                .existsByHorarioId_Id(horarioId))
                .thenReturn(true);

        // Una advertencia no bloquea la publicación.
        when(conflictoService.listarPendientes(horarioId))
                .thenReturn(List.of(
                        conflicto(SeveridadConflicto.ADVERTENCIA)));

        Usuario usuario = new Usuario();
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));

        when(horarioRepository.save(horario))
                .thenReturn(horario);

        PublicarHorarioResponse response =
                publicacionHorarioService.publicar(
                        horarioId,
                        CORREO);

        assertEquals(horarioId, response.getHorarioId());
        assertEquals(EstadoHorario.PUBLICADO, response.getEstado());
        assertNotNull(response.getFechaPublicacion());
        assertEquals(1, response.getVersion());

        assertEquals(EstadoHorario.PUBLICADO, horario.getEstado());
        assertNotNull(horario.getFechaPublicacion());

        verify(registroAuditoriaService).registrar(
                eq(usuario),
                eq(RegistroAuditoriaService.ACCION_PUBLICAR),
                eq("Horario"),
                eq(horarioId),
                any(),
                any(),
                isNull());
    }

    @Test
    void rechazaHorarioInexistente() {

        UUID horarioId = UUID.randomUUID();

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> publicacionHorarioService.publicar(
                        horarioId,
                        CORREO));

        verify(horarioRepository, never())
                .save(any(Horario.class));
    }

    @Test
    void rechazaHorarioQueYaEstaPublicado() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);
        horario.setEstado(EstadoHorario.PUBLICADO);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));

        TransicionEstadoNoPermitidaException exception =
                assertThrows(
                        TransicionEstadoNoPermitidaException.class,
                        () -> publicacionHorarioService.publicar(
                                horarioId,
                                CORREO));

        assertEquals(
                "Solo se puede publicar un horario en estado "
                        + "BORRADOR. Estado actual: PUBLICADO",
                exception.getMessage());

        verify(horarioRepository, never())
                .save(any(Horario.class));
    }

    @Test
    void rechazaHorarioSinClasesProgramadas() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));

        when(claseProgramadaRepository
                .existsByHorarioId_Id(horarioId))
                .thenReturn(false);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> publicacionHorarioService.publicar(
                        horarioId,
                        CORREO));

        verify(horarioRepository, never())
                .save(any(Horario.class));
    }

    @Test
    void rechazaHorarioConConflictosErrorPendientes() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));

        when(claseProgramadaRepository
                .existsByHorarioId_Id(horarioId))
                .thenReturn(true);

        when(conflictoService.listarPendientes(horarioId))
                .thenReturn(List.of(
                        conflicto(SeveridadConflicto.ERROR)));

        TransicionEstadoNoPermitidaException exception =
                assertThrows(
                        TransicionEstadoNoPermitidaException.class,
                        () -> publicacionHorarioService.publicar(
                                horarioId,
                                CORREO));

        assertEquals(
                EstadoHorario.BORRADOR,
                horario.getEstado());

        assertEquals(
                "No se puede publicar el horario: existen "
                        + "1 conflicto bloqueante pendiente: "
                        + "El docente ya tiene una clase programada "
                        + "en esta misma franja.",
                exception.getMessage());

        verify(horarioRepository, never())
                .save(any(Horario.class));

        verify(registroAuditoriaService, never())
                .registrar(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    private Horario horarioBorrador(
            UUID horarioId) {

        Horario horario = spy(new Horario());

        lenient()
                .doReturn(horarioId)
                .when(horario)
                .getId();

        horario.setEstado(EstadoHorario.BORRADOR);
        horario.setFechaPublicacion(null);
        horario.setVersion(1);

        return horario;
    }

    private ConflictoResponse conflicto(
            SeveridadConflicto severidad) {

        ConflictoResponse conflicto =
                new ConflictoResponse();

        conflicto.setId(UUID.randomUUID());
        conflicto.setTipo(
                severidad == SeveridadConflicto.ERROR
                        ? TipoConflicto.DOCENTE_OCUPADO
                        : TipoConflicto.CAPACIDAD_AULA);
        conflicto.setSeveridad(severidad);
        conflicto.setMensaje(
                severidad == SeveridadConflicto.ERROR
                        ? "El docente ya tiene una clase programada "
                        + "en esta misma franja."
                        : "El aula tiene capacidad insuficiente.");
        conflicto.setResuelto(false);
        conflicto.setFechaDeteccion(LocalDateTime.now());

        return conflicto;
    }
}
