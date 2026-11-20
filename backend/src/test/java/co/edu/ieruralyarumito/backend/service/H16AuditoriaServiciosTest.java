package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAulaRequest;
import co.edu.ieruralyarumito.backend.dto.CambiarEstadoDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearAulaRequest;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.Usuario;

import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;

import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class H16AuditoriaServiciosTest {

    private static final String CORREO = "rector@yarumito.edu.co";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RegistroAuditoriaService auditoria;

    @Mock private RestriccionDocenteRepository restriccionRepository;
    @Mock private DocenteRepository docenteRepository;
    @Mock private AnioEscolarRepository anioRepository;
    @Mock private FranjaHorariaRepository franjaRepository;

    @Mock private TituloProfesionalRepository tituloRepository;
    @Mock private IdoneidadRepository idoneidadRepository;

    @Mock private PlanEstudiosRepository planRepository;
    @Mock private GradoRepository gradoRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private AsignacionAcademicaRepository asignacionRepository;

    @Mock private AreaRepository areaRepository;
    @Mock private AulaRepository aulaRepository;
    @Mock private SedeRepository sedeRepository;

    @BeforeEach
    void configurarAutenticacion() {

        var autenticacion =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        "no-utilizada",
                        List.of(new SimpleGrantedAuthority("ROLE_RECTOR")));

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);
    }

    @AfterEach
    void limpiarAutenticacion() {
        SecurityContextHolder.clearContext();
    }

    private Usuario prepararUsuario() {

        Usuario usuario = new Usuario();

        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));

        return usuario;
    }

    // Comprueba el estado antes y después de una operación.
    private void verificarCambioAuditado(
            Usuario usuario,
            String entidad,
            UUID id,
            String campo,
            Object valorAnterior,
            Object valorPosterior) {

        ArgumentCaptor<Object> antes =
                ArgumentCaptor.forClass(Object.class);

        ArgumentCaptor<Object> despues =
                ArgumentCaptor.forClass(Object.class);

        verify(auditoria).registrar(
                same(usuario),
                eq(RegistroAuditoriaService.ACCION_UPDATE),
                eq(entidad),
                eq(id),
                antes.capture(),
                despues.capture(),
                isNull());

        assertInstanceOf(Map.class, antes.getValue());
        assertInstanceOf(Map.class, despues.getValue());

        Map<?, ?> estadoAnterior =
                (Map<?, ?>) antes.getValue();

        Map<?, ?> estadoPosterior =
                (Map<?, ?>) despues.getValue();

        assertEquals(valorAnterior, estadoAnterior.get(campo));
        assertEquals(valorPosterior, estadoPosterior.get(campo));
    }

    @Test
    void restriccionDocente_debeAuditarRetiroLogico() {

        Usuario usuario = prepararUsuario();
        UUID id = UUID.randomUUID();

        Docente docente = mock(Docente.class);
        AnioEscolar anio = mock(AnioEscolar.class);
        FranjaHoraria franja = mock(FranjaHoraria.class);

        when(docente.getId()).thenReturn(UUID.randomUUID());
        when(anio.getId()).thenReturn(UUID.randomUUID());
        when(franja.getId()).thenReturn(UUID.randomUUID());

        RestriccionDocente restriccion =
                new RestriccionDocente();

        ReflectionTestUtils.setField(restriccion, "id", id);

        restriccion.setDocente(docente);
        restriccion.setAnioEscolar(anio);
        restriccion.setFranjaHoraria(franja);
        restriccion.setDia(DiaSemana.LUNES);
        restriccion.setTipo(TipoRestriccionDocente.PREFERENTE);
        restriccion.setMotivo("Preferencia institucional");
        restriccion.setActivo(true);

        when(restriccionRepository.findById(id))
                .thenReturn(Optional.of(restriccion));

        when(restriccionRepository.save(restriccion))
                .thenReturn(restriccion);

        RestriccionDocenteService servicio =
                new RestriccionDocenteService(
                        restriccionRepository,
                        docenteRepository,
                        anioRepository,
                        franjaRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        servicio.retirarRestriccion(id);

        verificarCambioAuditado(
                usuario,
                "RestriccionDocente",
                id,
                "activo",
                true,
                false);
    }

    @Test
    void tituloProfesional_debeAuditarRetiroLogico() {

        Usuario usuario = prepararUsuario();
        UUID id = UUID.randomUUID();

        Docente docente = mock(Docente.class);

        when(docente.getId()).thenReturn(UUID.randomUUID());

        TituloProfesional titulo = new TituloProfesional();

        ReflectionTestUtils.setField(titulo, "id", id);

        titulo.setDocente(docente);
        titulo.setNombreTitulo("Licenciatura");
        titulo.setInstitucion("Universidad");
        titulo.setActivo(true);

        when(tituloRepository.findById(id))
                .thenReturn(Optional.of(titulo));

        when(tituloRepository.save(titulo))
                .thenReturn(titulo);

        TituloProfesionalService servicio =
                new TituloProfesionalService(
                        tituloRepository,
                        docenteRepository,
                        idoneidadRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        servicio.retirarTituloProfesional(id);

        verificarCambioAuditado(
                usuario,
                "TituloProfesional",
                id,
                "activo",
                true,
                false);
    }

    @Test
    void planEstudios_debeAuditarReactivacion() {

        Usuario usuario = prepararUsuario();
        UUID id = UUID.randomUUID();

        AnioEscolar anio = mock(AnioEscolar.class);
        Grado grado = mock(Grado.class);
        Asignatura asignatura = mock(Asignatura.class);

        when(anio.getId()).thenReturn(UUID.randomUUID());
        when(grado.getId()).thenReturn(UUID.randomUUID());
        when(asignatura.getId()).thenReturn(UUID.randomUUID());

        PlanEstudios plan = new PlanEstudios();

        ReflectionTestUtils.setField(plan, "id", id);

        plan.setAnioEscolar(anio);
        plan.setGrado(grado);
        plan.setAsignatura(asignatura);
        plan.setHorasSemanales(4);
        plan.setActivo(false);

        when(planRepository.findById(id))
                .thenReturn(Optional.of(plan));

        when(planRepository.save(plan))
                .thenReturn(plan);

        PlanEstudiosService servicio =
                new PlanEstudiosService(
                        planRepository,
                        anioRepository,
                        gradoRepository,
                        asignaturaRepository,
                        turnoRepository,
                        asignacionRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        servicio.reactivarPlanEstudios(id);

        verificarCambioAuditado(
                usuario,
                "PlanEstudios",
                id,
                "activo",
                false,
                true);
    }

    @Test
    void docente_debeAuditarCambioDeEstado() {

        Usuario usuario = prepararUsuario();
        UUID id = UUID.randomUUID();

        Area area = mock(Area.class);

        when(area.getId()).thenReturn(UUID.randomUUID());

        Docente docente = new Docente();

        ReflectionTestUtils.setField(docente, "id", id);

        docente.setAreaNombramiento(area);
        docente.setNombres("Docente");
        docente.setApellidos("Prueba");
        docente.setEstado(EstadoDocente.ACTIVO);

        when(docenteRepository.findById(id))
                .thenReturn(Optional.of(docente));

        when(docenteRepository.save(docente))
                .thenReturn(docente);

        CambiarEstadoDocenteRequest request =
                new CambiarEstadoDocenteRequest();

        request.setNuevoEstado(EstadoDocente.LICENCIA);

        DocenteService servicio =
                new DocenteService(
                        docenteRepository,
                        areaRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        servicio.cambiarEstado(id, request);

        verificarCambioAuditado(
                usuario,
                "Docente",
                id,
                "estado",
                EstadoDocente.ACTIVO,
                EstadoDocente.LICENCIA);
    }

    @Test
    void aula_debeAuditarActualizacionDeCapacidad() {

        Usuario usuario = prepararUsuario();
        UUID id = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        Sede sede = mock(Sede.class);

        when(sede.getId()).thenReturn(sedeId);

        Aula aula = new Aula();

        ReflectionTestUtils.setField(aula, "id", id);

        aula.setSede(sede);
        aula.setNombre("Aula 101");
        aula.setTipo(TipoAulaRequerida.AULA);
        aula.setCapacidad(25);
        aula.setActiva(true);

        when(aulaRepository.findById(id))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        ActualizarAulaRequest request =
                new ActualizarAulaRequest();

        request.setSedeId(sedeId);
        request.setNombre("Aula 101");
        request.setTipo(TipoAulaRequerida.AULA);
        request.setCapacidad(40);
        request.setActiva(false);

        AulaService servicio =
                new AulaService(aulaRepository, sedeRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        servicio.actualizarAula(id, request);

        verificarCambioAuditado(
                usuario,
                "Aula",
                id,
                "capacidad",
                25,
                40);
    }

    @Test
    void aula_debeRechazarCapacidadCeroSinGuardarNiAuditar() {

        UUID sedeId = UUID.randomUUID();

        Sede sede = mock(Sede.class);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        CrearAulaRequest request = new CrearAulaRequest();

        request.setSedeId(sedeId);
        request.setNombre("Aula inválida");
        request.setTipo(TipoAulaRequerida.AULA);
        request.setCapacidad(0);

        AulaService servicio =
                new AulaService(aulaRepository, sedeRepository);

        servicio.configurarAuditoria(usuarioRepository, auditoria);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> servicio.registrarAula(request));

        verify(aulaRepository, never()).save(any());
        verifyNoInteractions(auditoria);
    }
}
