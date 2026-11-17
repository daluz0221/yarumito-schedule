package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.Docente;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceMaximoDiarioAsignaturaTest {

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

    private UUID anioId;
    private UUID sedeId;
    private UUID grupoId;
    private UUID horarioId;
    private UUID asignacionId;
    private UUID asignaturaId;
    private UUID franjaId;
    private UUID aulaId;
    private UUID docenteId;

    private AnioEscolar anio;
    private Sede sede;
    private Grupo grupo;
    private Horario horario;
    private AsignacionAcademica asignacion;
    private Asignatura asignatura;
    private FranjaHoraria franja;
    private Aula aula;
    private Docente docente;

    @BeforeEach
    void prepararDatos() {

        anioId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        grupoId = UUID.randomUUID();
        horarioId = UUID.randomUUID();
        asignacionId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        aulaId = UUID.randomUUID();
        docenteId = UUID.randomUUID();

        anio = mock(AnioEscolar.class);
        sede = mock(Sede.class);
        grupo = mock(Grupo.class);
        horario = mock(Horario.class);
        asignacion = mock(AsignacionAcademica.class);
        asignatura = mock(Asignatura.class);
        franja = mock(FranjaHoraria.class);
        aula = mock(Aula.class);
        docente = mock(Docente.class);
    }

    @Test
    void H02_debePermitirDosBloquesDiariosMismaAsignatura() {

        prepararCreacion();

        ClaseProgramada primeraClase =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        false);

        when(claseProgramadaRepository.findByHorarioAndGrupo(
                horarioId, grupoId))
                .thenReturn(List.of(primeraClase));

        when(claseProgramadaRepository.save(
                any(ClaseProgramada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(
                any(ClaseProgramada.class)))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co");

        assertSame(response, resultado);

        verify(claseProgramadaRepository)
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H02_debeRechazarTercerBloqueNoConsecutivo() {

        prepararCreacion();

        Asignatura otraAsignatura = mock(Asignatura.class);

        ClaseProgramada primera =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        false);

        ClaseProgramada intermedia =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        otraAsignatura,
                        false);

        ClaseProgramada segunda =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        false);

        when(claseProgramadaRepository.findByHorarioAndGrupo(
                horarioId, grupoId))
                .thenReturn(List.of(
                        primera,
                        intermedia,
                        segunda));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H02_debeRechazarTercerBloqueDespuesDelDescanso() {

        prepararCreacion();

        ClaseProgramada primera =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        false);

        ClaseProgramada segunda =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        false);

        ClaseProgramada descanso =
                crearClaseExistente(
                        DiasSemana.LUNES,
                        asignatura,
                        true);

        when(claseProgramadaRepository.findByHorarioAndGrupo(
                horarioId, grupoId))
                .thenReturn(List.of(
                        primera,
                        segunda,
                        descanso));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H02_debePermitirMoverClaseSinContarlaDosVeces() {

        UUID claseId = UUID.randomUUID();

        ClaseProgramada clase = mock(ClaseProgramada.class);

        prepararMovimiento(clase, claseId);

        ClaseProgramada otraClase =
                crearClaseExistente(
                        DiasSemana.MARTES,
                        asignatura,
                        false);

        when(claseProgramadaRepository.findByHorarioAndGrupo(
                horarioId, grupoId))
                .thenReturn(List.of(clase, otraClase));

        when(claseProgramadaRepository.save(clase))
                .thenReturn(clase);

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(clase))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest());

        assertSame(response, resultado);

        verify(claseProgramadaRepository).save(clase);
    }

    private void prepararCreacion() {

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(grupo.getAnioEscolar()).thenReturn(anio);

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId, sedeId))
                .thenReturn(Optional.of(horario));

        prepararValidacionesComunes();
    }

    private void prepararMovimiento(
            ClaseProgramada clase,
            UUID claseId) {

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(clase.getId()).thenReturn(claseId);

        when(clase.getHorarioId())
                .thenReturn(horario);

        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);

        prepararValidacionesComunes();
    }

    private void prepararValidacionesComunes() {

        // Algunos datos no se consultan cuando H-02 rechaza
        // anticipadamente el tercer bloque.
        // lenient evita errores por simulaciones no utilizadas.

        lenient().when(asignacion.getGrupo())
                .thenReturn(grupo);

        lenient().when(asignacion.getDocente())
                .thenReturn(docente);

        lenient().when(asignacion.getAsignatura())
                .thenReturn(asignatura);

        lenient().when(asignatura.getId())
                .thenReturn(asignaturaId);

        lenient().when(grupo.getId())
                .thenReturn(grupoId);

        lenient().when(grupo.getSede())
                .thenReturn(sede);

        lenient().when(anio.getId())
                .thenReturn(anioId);

        lenient().when(sede.getId())
                .thenReturn(sedeId);

        lenient().when(horario.getId())
                .thenReturn(horarioId);

        lenient().when(horario.getAnioEscolarId())
                .thenReturn(anio);

        lenient().when(docente.getId())
                .thenReturn(docenteId);

        lenient().when(
                        franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        lenient().when(franja.getId())
                .thenReturn(franjaId);

        lenient().when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        lenient().when(aula.isActiva())
                .thenReturn(true);

        lenient().when(aula.getSede())
                .thenReturn(sede);
    }

    private ClaseProgramada crearClaseExistente(
            DiasSemana dia,
            Asignatura materia,
            boolean esDescanso) {

        ClaseProgramada existente =
                mock(ClaseProgramada.class);

        AsignacionAcademica asignacionExistente =
                mock(AsignacionAcademica.class);

        FranjaHoraria franjaExistente =
                mock(FranjaHoraria.class);

        lenient().when(existente.getId())
                .thenReturn(UUID.randomUUID());

        lenient().when(existente.getDia())
                .thenReturn(dia);

        lenient().when(existente.getAsignacionAcademicaId())
                .thenReturn(asignacionExistente);

        lenient().when(asignacionExistente.getAsignatura())
                .thenReturn(materia);

        lenient().when(existente.getFranjaHorariaId())
                .thenReturn(franjaExistente);

        lenient().when(franjaExistente.isEsDescanso())
                .thenReturn(esDescanso);

        if (esDescanso) {
            lenient().when(franjaExistente.getNumero())
                    .thenReturn(0);
        }

        return existente;
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
