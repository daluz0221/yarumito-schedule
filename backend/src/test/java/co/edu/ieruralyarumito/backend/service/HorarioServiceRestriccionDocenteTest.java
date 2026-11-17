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
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;
import co.edu.ieruralyarumito.backend.exception.ConflictoHorarioException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceRestriccionDocenteTest {

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
    private UUID docenteId;
    private UUID grupoId;
    private UUID horarioId;
    private UUID franjaId;
    private UUID aulaId;
    private UUID asignacionId;
    private UUID claseId;
    private UUID sedeId;
    private UUID asignaturaId;

    private AnioEscolar anio;
    private Docente docente;
    private Grupo grupo;
    private Horario horario;
    private FranjaHoraria franja;
    private Aula aula;
    private AsignacionAcademica asignacion;
    private Asignatura asignatura;
    private ClaseProgramada clase;
    private Sede sede;

    @BeforeEach
    void prepararDatos() {

        anioId = UUID.randomUUID();
        docenteId = UUID.randomUUID();
        grupoId = UUID.randomUUID();
        horarioId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        aulaId = UUID.randomUUID();
        asignacionId = UUID.randomUUID();
        claseId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();

        anio = mock(AnioEscolar.class);
        docente = mock(Docente.class);
        grupo = mock(Grupo.class);
        horario = mock(Horario.class);
        franja = mock(FranjaHoraria.class);
        aula = mock(Aula.class);
        asignacion = mock(AsignacionAcademica.class);
        asignatura = mock(Asignatura.class);
        clase = mock(ClaseProgramada.class);
        sede = mock(Sede.class);
    }

    @Test
    void H06B_debeRechazarCrearClaseConDocenteNoDisponible() {

        prepararCreacion();

        configurarRestriccion(
                DiaSemana.LUNES,
                TipoRestriccionDocente.NO_DISPONIBLE);

        ConflictoHorarioException error = assertThrows(
                ConflictoHorarioException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(DiasSemana.LUNES),
                        "rector@yarumito.edu.co"));

        assertEquals(
                "DOCENTE_NO_DISPONIBLE",
                error.getCodigo());

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H06B_debePermitirCrearClaseConPreferencia() {

        prepararCreacion();

        configurarRestriccion(
                DiaSemana.LUNES,
                TipoRestriccionDocente.PREFERENTE);

        when(claseProgramadaRepository.save(any(ClaseProgramada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(any(ClaseProgramada.class)))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.crearClaseProgramada(
                        crearRequest(DiasSemana.LUNES),
                        "rector@yarumito.edu.co");

        assertSame(response, resultado);

        verify(claseProgramadaRepository)
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H06B_debeRechazarMoverClaseConDocenteNoDisponible() {

        prepararMovimiento();

        configurarRestriccion(
                DiaSemana.MARTES,
                TipoRestriccionDocente.NO_DISPONIBLE);

        ConflictoHorarioException error = assertThrows(
                ConflictoHorarioException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest(DiasSemana.MARTES)));

        assertEquals(
                "DOCENTE_NO_DISPONIBLE",
                error.getCodigo());

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H06B_debePermitirMoverClaseConPreferencia() {

        prepararMovimiento();

        configurarRestriccion(
                DiaSemana.MARTES,
                TipoRestriccionDocente.PREFERENTE);

        when(claseProgramadaRepository.save(clase))
                .thenReturn(clase);

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(clase))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest(DiasSemana.MARTES));

        assertSame(response, resultado);

        verify(claseProgramadaRepository).save(clase);
        verify(clase).setDia(DiasSemana.MARTES);
        verify(clase).setFranjaHorariaId(franja);
        verify(clase).setAulaId(aula);
    }

    private void prepararCreacion() {

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(asignacion.getGrupo()).thenReturn(grupo);
        when(asignacion.getDocente()).thenReturn(docente);

        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(grupo.getId()).thenReturn(grupoId);

        when(anio.getId()).thenReturn(anioId);

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId, sedeId))
                .thenReturn(Optional.of(horario));

        prepararValidacionesComunes();
    }

    private void prepararMovimiento() {

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(clase.getId()).thenReturn(claseId);
        when(clase.getHorarioId()).thenReturn(horario);
        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);

        when(asignacion.getGrupo()).thenReturn(grupo);
        when(asignacion.getDocente()).thenReturn(docente);

        when(grupo.getId()).thenReturn(grupoId);

        prepararValidacionesComunes();
    }

    private void prepararValidacionesComunes() {

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        // H-08: aula activa y perteneciente a la sede del grupo.
        when(aula.isActiva()).thenReturn(true);
        when(aula.getSede()).thenReturn(sede);
        when(grupo.getSede()).thenReturn(sede);
        when(sede.getId()).thenReturn(sedeId);

        when(franja.getId()).thenReturn(franjaId);

        when(horario.getId()).thenReturn(horarioId);
        when(horario.getAnioEscolarId()).thenReturn(anio);
        when(anio.getId()).thenReturn(anioId);

        when(docente.getId()).thenReturn(docenteId);

        // H-02: preparar la asignatura para validar el límite diario.
        // Se utiliza lenient porque algunas pruebas se detienen antes,
        // cuando detectan NO_DISPONIBLE.
        lenient().when(asignacion.getAsignatura())
                .thenReturn(asignatura);

        lenient().when(asignatura.getId())
                .thenReturn(asignaturaId);

        // H-02: sin otras clases programadas para este grupo.
        lenient().when(claseProgramadaRepository.findByHorarioAndGrupo(
                        horarioId,
                        grupoId))
                .thenReturn(List.of());
    }

    private void configurarRestriccion(
            DiaSemana dia,
            TipoRestriccionDocente tipo) {

        RestriccionDocente restriccion =
                mock(RestriccionDocente.class);

        when(restriccion.getTipo()).thenReturn(tipo);

        when(restriccionDocenteRepository
                .findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                        docenteId,
                        anioId,
                        dia,
                        franjaId))
                .thenReturn(Optional.of(restriccion));
    }

    private CrearClaseProgramadaRequest crearRequest(
            DiasSemana dia) {

        CrearClaseProgramadaRequest request =
                new CrearClaseProgramadaRequest();

        request.setAsignacionAcademicaId(asignacionId);
        request.setDia(dia);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }

    private ActualizarClaseProgramadaRequest actualizarRequest(
            DiasSemana dia) {

        ActualizarClaseProgramadaRequest request =
                new ActualizarClaseProgramadaRequest();

        request.setDia(dia);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }
}
