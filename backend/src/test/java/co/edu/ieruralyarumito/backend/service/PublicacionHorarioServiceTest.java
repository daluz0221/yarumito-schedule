package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioVersionResponse;
import co.edu.ieruralyarumito.backend.dto.PublicarHorarioResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.ConflictoRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
    private ConflictoRepository conflictoRepository;

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
                        conflictoRepository,
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

    @Test
    void publicarArchivaLaVersionPublicadaAnterior() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede sede = sede(sedeId, "Yarumito");

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);
        horario.setVersion(2);
        horario.setAnioEscolarId(anio);
        horario.setSedeId(sede);

        UUID anteriorId = UUID.randomUUID();
        Horario anterior = horarioBorrador(anteriorId);
        anterior.setEstado(EstadoHorario.PUBLICADO);
        anterior.setVersion(1);
        anterior.setAnioEscolarId(anio);
        anterior.setSedeId(sede);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));
        when(claseProgramadaRepository.existsByHorarioId_Id(horarioId))
                .thenReturn(true);
        when(conflictoService.listarPendientes(horarioId))
                .thenReturn(List.of());
        when(horarioRepository
                .findByAnioEscolarId_IdAndSedeId_IdAndEstado(
                        anioId,
                        sedeId,
                        EstadoHorario.PUBLICADO))
                .thenReturn(List.of(anterior, horario));
        when(horarioRepository.save(any(Horario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = new Usuario();
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));

        PublicarHorarioResponse response =
                publicacionHorarioService.publicar(horarioId, CORREO);

        assertEquals(EstadoHorario.PUBLICADO, response.getEstado());
        assertEquals(EstadoHorario.ARCHIVADO, anterior.getEstado());
        assertEquals(2, response.getVersion());

        verify(registroAuditoriaService).registrar(
                eq(usuario),
                eq(RegistroAuditoriaService.ACCION_UPDATE),
                eq("Horario"),
                eq(anteriorId),
                any(),
                any(),
                isNull());
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
    void creaBorradorDeMejoraCopiandoLasClases() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede sede = sede(sedeId, "Yarumito");

        Horario origen = horarioBorrador(origenId);
        origen.setEstado(EstadoHorario.PUBLICADO);
        origen.setVersion(1);
        origen.setNombre("Horario 2026 - Yarumito");
        origen.setAnioEscolarId(anio);
        origen.setSedeId(sede);

        when(horarioRepository.findById(origenId))
                .thenReturn(Optional.of(origen));
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.BORRADOR))
                .thenReturn(Optional.empty());
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId,
                        sedeId))
                .thenReturn(Optional.of(origen));
        when(horarioRepository.save(any(Horario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(claseProgramadaRepository.findByHorarioId_Id(origenId))
                .thenReturn(List.of(new ClaseProgramada()));
        when(claseProgramadaRepository.save(any(ClaseProgramada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = new Usuario();
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));

        HorarioVersionResponse response =
                publicacionHorarioService.crearBorradorDeMejora(
                        origenId,
                        CORREO);

        assertTrue(response.isNuevo());
        assertEquals(2, response.getVersion());
        assertEquals(EstadoHorario.BORRADOR, response.getEstado());
        assertNull(response.getFechaPublicacion());
        assertEquals(sedeId, response.getSedeId());

        verify(claseProgramadaRepository, times(1))
                .save(any(ClaseProgramada.class));
        verify(registroAuditoriaService).registrar(
                eq(usuario),
                eq(RegistroAuditoriaService.ACCION_CREATE),
                eq("Horario"),
                nullable(UUID.class),
                isNull(),
                any(),
                isNull());
    }

    @Test
    void reutilizaElBorradorDeMejoraAbierto() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID borradorId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede sede = sede(sedeId, "Yarumito");

        Horario origen = horarioBorrador(origenId);
        origen.setEstado(EstadoHorario.PUBLICADO);
        origen.setAnioEscolarId(anio);
        origen.setSedeId(sede);

        Horario borrador = horarioBorrador(borradorId);
        borrador.setVersion(3);
        borrador.setNombre("Horario 2026 - Yarumito");
        borrador.setAnioEscolarId(anio);
        borrador.setSedeId(sede);

        when(horarioRepository.findById(origenId))
                .thenReturn(Optional.of(origen));
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.BORRADOR))
                .thenReturn(Optional.of(borrador));

        HorarioVersionResponse response =
                publicacionHorarioService.crearBorradorDeMejora(
                        origenId,
                        CORREO);

        assertFalse(response.isNuevo());
        assertEquals(borradorId, response.getId());
        assertEquals(3, response.getVersion());
        verify(horarioRepository, never()).save(any(Horario.class));
        verify(claseProgramadaRepository, never())
                .findByHorarioId_Id(any());
    }

    @Test
    void rechazaBorradorSiElHorarioNoEstaPublicado() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> publicacionHorarioService.crearBorradorDeMejora(
                        horarioId,
                        CORREO));

        verify(horarioRepository, never()).save(any(Horario.class));
    }

    @Test
    void descartaElBorradorDeMejoraYConservaElPublicado() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID borradorId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede sede = sede(sedeId, "Yarumito");

        Horario borrador = horarioBorrador(borradorId);
        borrador.setVersion(2);
        borrador.setAnioEscolarId(anio);
        borrador.setSedeId(sede);

        Horario publicado = horarioBorrador(UUID.randomUUID());
        publicado.setEstado(EstadoHorario.PUBLICADO);

        when(horarioRepository.findById(borradorId))
                .thenReturn(Optional.of(borrador));
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.PUBLICADO))
                .thenReturn(Optional.of(publicado));

        Usuario usuario = new Usuario();
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));

        publicacionHorarioService.descartarBorrador(borradorId, CORREO);

        verify(conflictoRepository).eliminarPorHorario(borradorId);
        verify(claseProgramadaRepository).eliminarPorHorario(borradorId);
        verify(horarioRepository).delete(borrador);
        verify(registroAuditoriaService).registrar(
                eq(usuario),
                eq(RegistroAuditoriaService.ACCION_DELETE),
                eq("Horario"),
                eq(borradorId),
                any(),
                isNull(),
                isNull());
    }

    @Test
    void rechazaDescartarSiNoHayHorarioPublicado() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID borradorId = UUID.randomUUID();

        Horario borrador = horarioBorrador(borradorId);
        borrador.setAnioEscolarId(anio(anioId));
        borrador.setSedeId(sede(sedeId, "Yarumito"));

        when(horarioRepository.findById(borradorId))
                .thenReturn(Optional.of(borrador));
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.PUBLICADO))
                .thenReturn(Optional.empty());

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> publicacionHorarioService.descartarBorrador(
                        borradorId,
                        CORREO));

        verify(horarioRepository, never()).delete(any(Horario.class));
    }

    @Test
    void publicarLimpiaLaNotaAutomaticaDelBorrador() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);
        horario.setNotas("Borrador de mejora de la versión 1");

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));
        when(claseProgramadaRepository.existsByHorarioId_Id(horarioId))
                .thenReturn(true);
        when(conflictoService.listarPendientes(horarioId))
                .thenReturn(List.of());
        when(horarioRepository.save(horario)).thenReturn(horario);
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(new Usuario()));

        publicacionHorarioService.publicar(horarioId, CORREO);

        assertEquals(EstadoHorario.PUBLICADO, horario.getEstado());
        assertNull(horario.getNotas());
    }

    @Test
    void publicarConservaUnaNotaDistintaALaDelBorrador() {

        UUID horarioId = UUID.randomUUID();
        Horario horario = horarioBorrador(horarioId);
        horario.setNotas("Ajuste pedido por coordinación");

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(horario));
        when(claseProgramadaRepository.existsByHorarioId_Id(horarioId))
                .thenReturn(true);
        when(conflictoService.listarPendientes(horarioId))
                .thenReturn(List.of());
        when(horarioRepository.save(horario)).thenReturn(horario);
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(new Usuario()));

        publicacionHorarioService.publicar(horarioId, CORREO);

        assertEquals(
                "Ajuste pedido por coordinación",
                horario.getNotas());
    }

    @Test
    void copiaDiaAulaYBloqueoAlCrearElBorrador() {

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede sede = sede(sedeId, "Yarumito");

        Horario origen = horarioBorrador(origenId);
        origen.setEstado(EstadoHorario.PUBLICADO);
        origen.setNombre("Horario 2026 - Yarumito");
        origen.setAnioEscolarId(anio);
        origen.setSedeId(sede);

        AsignacionAcademica asignacion = new AsignacionAcademica();
        FranjaHoraria franja = new FranjaHoraria();
        Aula aula = new Aula();

        ClaseProgramada clase = new ClaseProgramada();
        clase.setAsignacionAcademicaId(asignacion);
        clase.setDia(DiasSemana.MARTES);
        clase.setFranjaHorariaId(franja);
        clase.setAulaId(aula);
        clase.setEsHoraExtra(true);
        clase.setBloqueada(true);

        when(horarioRepository.findById(origenId))
                .thenReturn(Optional.of(origen));
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.BORRADOR))
                .thenReturn(Optional.empty());
        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId,
                        sedeId))
                .thenReturn(Optional.of(origen));
        when(horarioRepository.save(any(Horario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(claseProgramadaRepository.findByHorarioId_Id(origenId))
                .thenReturn(List.of(clase));
        when(claseProgramadaRepository.save(any(ClaseProgramada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(new Usuario()));

        publicacionHorarioService.crearBorradorDeMejora(origenId, CORREO);

        ArgumentCaptor<ClaseProgramada> copia =
                ArgumentCaptor.forClass(ClaseProgramada.class);
        verify(claseProgramadaRepository).save(copia.capture());

        assertEquals(DiasSemana.MARTES, copia.getValue().getDia());
        assertEquals(asignacion, copia.getValue().getAsignacionAcademicaId());
        assertEquals(franja, copia.getValue().getFranjaHorariaId());
        assertEquals(aula, copia.getValue().getAulaId());
        assertTrue(copia.getValue().isEsHoraExtra());
        assertTrue(copia.getValue().isBloqueada());
        assertEquals(
                EstadoHorario.BORRADOR,
                copia.getValue().getHorarioId().getEstado());
    }

    @Test
    void rechazaCrearBorradorSiElAnioEstaCerrado() {

        UUID origenId = UUID.randomUUID();
        AnioEscolar anio = anio(UUID.randomUUID());
        anio.setEstado(EstadoAnioEscolar.CERRADO);

        Horario origen = horarioBorrador(origenId);
        origen.setEstado(EstadoHorario.PUBLICADO);
        origen.setAnioEscolarId(anio);
        origen.setSedeId(sede(UUID.randomUUID(), "Yarumito"));

        when(horarioRepository.findById(origenId))
                .thenReturn(Optional.of(origen));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> publicacionHorarioService.crearBorradorDeMejora(
                        origenId,
                        CORREO));

        verify(horarioRepository, never()).save(any(Horario.class));
    }

    @Test
    void rechazaDescartarUnHorarioQueNoEstaEnBorrador() {

        UUID horarioId = UUID.randomUUID();
        Horario publicado = horarioBorrador(horarioId);
        publicado.setEstado(EstadoHorario.PUBLICADO);

        when(horarioRepository.findById(horarioId))
                .thenReturn(Optional.of(publicado));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> publicacionHorarioService.descartarBorrador(
                        horarioId,
                        CORREO));

        verify(horarioRepository, never()).delete(any(Horario.class));
    }

    @Test
    void listarVersionesOrdenaPorSedeYLuegoPorVersion() {

        UUID anioId = UUID.randomUUID();
        AnioEscolar anio = anio(anioId);
        Sede anexo = sede(UUID.randomUUID(), "Anexo");
        Sede principal = sede(UUID.randomUUID(), "Principal");

        Horario anexoV1 = horarioBorrador(UUID.randomUUID());
        anexoV1.setVersion(1);
        anexoV1.setEstado(EstadoHorario.ARCHIVADO);
        anexoV1.setNombre("Horario anexo");
        anexoV1.setAnioEscolarId(anio);
        anexoV1.setSedeId(anexo);

        Horario principalV1 = horarioBorrador(UUID.randomUUID());
        principalV1.setVersion(1);
        principalV1.setEstado(EstadoHorario.ARCHIVADO);
        principalV1.setNombre("Horario principal");
        principalV1.setAnioEscolarId(anio);
        principalV1.setSedeId(principal);

        Horario principalV2 = horarioBorrador(UUID.randomUUID());
        principalV2.setVersion(2);
        principalV2.setEstado(EstadoHorario.PUBLICADO);
        principalV2.setNombre("Horario principal");
        principalV2.setAnioEscolarId(anio);
        principalV2.setSedeId(principal);

        when(horarioRepository.findByAnioEscolarId_IdOrderByVersionDesc(anioId))
                .thenReturn(List.of(anexoV1, principalV1, principalV2));

        var versiones = publicacionHorarioService.listarVersiones(anioId);

        assertEquals(3, versiones.size());
        assertEquals("Anexo", versiones.get(0).getSedeNombre());
        assertEquals(1, versiones.get(0).getVersion());
        assertEquals("Principal", versiones.get(1).getSedeNombre());
        assertEquals(2, versiones.get(1).getVersion());
        assertEquals(EstadoHorario.PUBLICADO, versiones.get(1).getEstado());
        assertEquals(1, versiones.get(2).getVersion());
    }

    @Test
    void listarVersionesRechazaAnioNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> publicacionHorarioService.listarVersiones(null));
    }

    private AnioEscolar anio(UUID anioId) {
        AnioEscolar anio = spy(new AnioEscolar());
        lenient().doReturn(anioId).when(anio).getId();
        return anio;
    }

    private Sede sede(UUID sedeId, String nombre) {
        Sede sede = spy(new Sede());
        lenient().doReturn(sedeId).when(sede).getId();
        sede.setNombre(nombre);
        return sede;
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
