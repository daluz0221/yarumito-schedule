package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MejoraM01RegresarBorradorTest {

    private static final String CORREO =
            "rector@yarumito.edu.co";

    @Mock
    private AsignacionAcademicaRepository asignacionRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionAcademicaMapper mapper;

    @Mock
    private RegistroAuditoriaService auditoria;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> conteoQuery;

    @InjectMocks
    private CorreccionAsignacionAcademicaService servicio;

    private UUID asignacionId;
    private AsignacionAcademica asignacion;

    @BeforeEach
    void preparar() {

        asignacionId = UUID.randomUUID();

        AnioEscolar anio = new AnioEscolar();
        ReflectionTestUtils.setField(
                anio, "id", UUID.randomUUID());

        Sede sede = new Sede();
        ReflectionTestUtils.setField(
                sede, "id", UUID.randomUUID());

        Grupo grupo = new Grupo();
        ReflectionTestUtils.setField(
                grupo, "id", UUID.randomUUID());
        grupo.setSede(sede);
        grupo.setAnioEscolar(anio);

        Asignatura asignatura = new Asignatura();
        ReflectionTestUtils.setField(
                asignatura, "id", UUID.randomUUID());

        Docente docente = new Docente();
        ReflectionTestUtils.setField(
                docente, "id", UUID.randomUUID());

        asignacion = new AsignacionAcademica();
        ReflectionTestUtils.setField(
                asignacion, "id", asignacionId);

        asignacion.setAnioEscolar(anio);
        asignacion.setGrupo(grupo);
        asignacion.setAsignatura(asignatura);
        asignacion.setDocente(docente);
        asignacion.setHorasSemanales(4);
        asignacion.setEstado(
                EstadoAsignacionAcademica.CONFIRMADA);
    }

    private void autenticar(RolUsuario rol) {

        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));
    }

    private void encontrarAsignacion() {

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));
    }

    private void prepararConteos(
            Long... resultados) {

        when(entityManager.createQuery(
                anyString(), eq(Long.class)))
                .thenReturn(conteoQuery);

        when(conteoQuery.setParameter(
                anyString(), any()))
                .thenReturn(conteoQuery);

        when(conteoQuery.getSingleResult())
                .thenReturn(resultados[0],
                        java.util.Arrays.copyOfRange(
                                resultados,
                                1,
                                resultados.length));
    }

    @Test
    void M01_regresaConfirmadaABorrador() {

        autenticar(RolUsuario.RECTOR);
        encontrarAsignacion();
        prepararConteos(0L, 0L);

        when(asignacionRepository.save(asignacion))
                .thenReturn(asignacion);

        AsignacionAcademicaResponse respuesta =
                new AsignacionAcademicaResponse();

        when(mapper.toResponse(asignacion))
                .thenReturn(respuesta);

        AsignacionAcademicaResponse resultado =
                servicio.regresarABorrador(
                        asignacionId, CORREO);

        assertSame(respuesta, resultado);

        assertEquals(
                EstadoAsignacionAcademica.BORRADOR,
                asignacion.getEstado());

        verify(asignacionRepository)
                .save(asignacion);

        verify(conteoQuery, times(2))
                .getSingleResult();

        ArgumentCaptor<Object> antes =
                ArgumentCaptor.forClass(Object.class);

        ArgumentCaptor<Object> despues =
                ArgumentCaptor.forClass(Object.class);

        verify(auditoria).registrar(
                any(Usuario.class),
                eq(RegistroAuditoriaService.ACCION_UPDATE),
                eq("AsignacionAcademica"),
                eq(asignacionId),
                antes.capture(),
                despues.capture(),
                isNull());

        Map<?, ?> datosAntes =
                (Map<?, ?>) antes.getValue();

        Map<?, ?> datosDespues =
                (Map<?, ?>) despues.getValue();

        assertEquals(
                "CONFIRMADA",
                datosAntes.get("estado"));

        assertEquals(
                "BORRADOR",
                datosDespues.get("estado"));
    }

    @Test
    void M01_noRegresaSiTieneClasesProgramadas() {

        autenticar(RolUsuario.RECTOR);
        encontrarAsignacion();
        prepararConteos(1L);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> servicio.regresarABorrador(
                        asignacionId, CORREO));

        assertEquals(
                EstadoAsignacionAcademica.CONFIRMADA,
                asignacion.getEstado());

        verify(asignacionRepository, never())
                .save(any());

        verifyNoInteractions(auditoria);
    }

    @Test
    void M01_noRegresaConHorarioPublicado() {

        autenticar(RolUsuario.RECTOR);
        encontrarAsignacion();
        prepararConteos(0L, 1L);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> servicio.regresarABorrador(
                        asignacionId, CORREO));

        assertEquals(
                EstadoAsignacionAcademica.CONFIRMADA,
                asignacion.getEstado());

        verify(asignacionRepository, never())
                .save(any());

        verifyNoInteractions(auditoria);
    }

    @Test
    void M01_noRegresaUnaAsignacionYaEnBorrador() {

        autenticar(RolUsuario.RECTOR);
        encontrarAsignacion();

        asignacion.setEstado(
                EstadoAsignacionAcademica.BORRADOR);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> servicio.regresarABorrador(
                        asignacionId, CORREO));

        verify(asignacionRepository, never())
                .save(any());

        verifyNoInteractions(entityManager, auditoria);
    }

    @Test
    void M01_rechazaUsuarioQueNoEsRector() {

        autenticar(RolUsuario.DOCENTE);

        assertThrows(
                ResponseStatusException.class,
                () -> servicio.regresarABorrador(
                        asignacionId, CORREO));

        verifyNoInteractions(
                asignacionRepository,
                entityManager,
                auditoria);
    }
}
