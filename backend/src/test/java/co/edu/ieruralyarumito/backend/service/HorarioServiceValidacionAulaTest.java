package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.mapper.HorarioMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceValidacionAulaTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private AsignacionAcademicaRepository asignacionAcademicaRepository;

    @Mock
    private ClaseProgramadaRepository claseProgramadaRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlanEstudiosRepository planEstudiosRepository;

    @Mock
    private HorarioMapper horarioMapper;

    @Mock
    private ConflictoHorarioRegistroService conflictoHorarioRegistroService;

    @Mock
    private RestriccionDocenteRepository restriccionDocenteRepository;

    @InjectMocks
    private HorarioService horarioService;

    private UUID claseId;
    private UUID asignacionId;
    private UUID aulaId;
    private UUID franjaId;
    private UUID grupoId;
    private UUID horarioId;
    private UUID asignaturaId;

    private ClaseProgramada clase;
    private AsignacionAcademica asignacion;
    private Asignatura asignatura;
    private Grupo grupo;
    private Horario horario;
    private Aula aula;
    private FranjaHoraria franja;

    @BeforeEach
    void prepararDatos() {

        claseId = UUID.randomUUID();
        asignacionId = UUID.randomUUID();
        aulaId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        grupoId = UUID.randomUUID();
        horarioId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();

        clase = mock(ClaseProgramada.class);
        asignacion = mock(AsignacionAcademica.class);
        asignatura = mock(Asignatura.class);
        grupo = mock(Grupo.class);
        horario = mock(Horario.class);
        aula = mock(Aula.class);
        franja = mock(FranjaHoraria.class);
    }

    @Test
    void H08_debeRechazarCrearClaseEnAulaInactiva() {

        prepararCreacion();

        // El valor predeterminado del mock es false.
        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H08_debeRechazarCrearClaseEnOtraSede() {

        prepararCreacion();
        configurarAulaDeOtraSede();

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H08_debeRechazarMoverClaseAAulaInactiva() {

        prepararMovimiento();

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest()));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H08_debeRechazarMoverClaseAOtraSede() {

        prepararMovimiento();
        configurarAulaDeOtraSede();

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest()));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    private void prepararCreacion() {

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(asignacion.getGrupo()).thenReturn(grupo);

        prepararFranjaYAula();
    }

    private void prepararMovimiento() {

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);

        when(clase.getHorarioId())
                .thenReturn(horario);

        when(asignacion.getGrupo())
                .thenReturn(grupo);

        prepararFranjaYAula();

        // H-02: datos simulados necesarios para comprobar
        // el máximo diario de una asignatura.
        // lenient evita fallos por stubs no utilizados cuando
        // H-08 rechaza antes el aula.

        lenient().when(asignacion.getAsignatura())
                .thenReturn(asignatura);

        lenient().when(asignatura.getId())
                .thenReturn(asignaturaId);

        lenient().when(grupo.getId())
                .thenReturn(grupoId);

        lenient().when(horario.getId())
                .thenReturn(horarioId);

        lenient().when(claseProgramadaRepository
                        .findByHorarioAndGrupo(horarioId, grupoId))
                .thenReturn(List.of());
    }

    private void prepararFranjaYAula() {

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));
    }

    private void configurarAulaDeOtraSede() {

        Sede sedeGrupo = mock(Sede.class);
        Sede sedeAula = mock(Sede.class);

        UUID sedeGrupoId = UUID.randomUUID();
        UUID sedeAulaId = UUID.randomUUID();

        when(aula.isActiva()).thenReturn(true);

        when(grupo.getSede()).thenReturn(sedeGrupo);
        when(aula.getSede()).thenReturn(sedeAula);

        when(sedeGrupo.getId()).thenReturn(sedeGrupoId);
        when(sedeAula.getId()).thenReturn(sedeAulaId);
    }

    private CrearClaseProgramadaRequest crearRequest() {

        CrearClaseProgramadaRequest request =
                new CrearClaseProgramadaRequest();

        request.setAsignacionAcademicaId(asignacionId);
        request.setDia(DiasSemana.LUNES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }

    private ActualizarClaseProgramadaRequest actualizarRequest() {

        ActualizarClaseProgramadaRequest request =
                new ActualizarClaseProgramadaRequest();

        request.setDia(DiasSemana.MARTES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }
}
