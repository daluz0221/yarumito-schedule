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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceGrupoOcupadoTest {

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

    @Test
    void H01_debeRechazarCrearClaseSiGrupoEstaOcupado() {

        UUID asignacionId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        AnioEscolar anio = mock(AnioEscolar.class);
        Sede sede = mock(Sede.class);
        Grupo grupo = mock(Grupo.class);
        Horario horario = mock(Horario.class);
        FranjaHoraria franja = mock(FranjaHoraria.class);
        Aula aula = mock(Aula.class);
        Asignatura asignatura = mock(Asignatura.class);
        AsignacionAcademica asignacion =
                mock(AsignacionAcademica.class);

        when(asignacion.getGrupo()).thenReturn(grupo);

        // H-02: asignatura requerida para validar el máximo diario.
        lenient().when(asignacion.getAsignatura())
                .thenReturn(asignatura);
        lenient().when(asignatura.getId())
                .thenReturn(asignaturaId);

        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(grupo.getSede()).thenReturn(sede);

        when(anio.getId()).thenReturn(anioId);
        when(sede.getId()).thenReturn(sedeId);
        when(grupo.getId()).thenReturn(grupoId);
        when(horario.getId()).thenReturn(horarioId);
        when(franja.getId()).thenReturn(franjaId);

        // H-08: aula activa y perteneciente a la sede del grupo.
        when(aula.isActiva()).thenReturn(true);
        when(aula.getSede()).thenReturn(sede);

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId, sedeId))
                .thenReturn(Optional.of(horario));

        // H-02: ningún bloque previo para esta asignatura.
        lenient().when(claseProgramadaRepository
                        .findByHorarioAndGrupo(horarioId, grupoId))
                .thenReturn(List.of());

        when(claseProgramadaRepository.existeGrupoOcupado(
                horarioId,
                grupoId,
                DiasSemana.LUNES,
                franjaId))
                .thenReturn(true);

        CrearClaseProgramadaRequest request =
                new CrearClaseProgramadaRequest();

        request.setAsignacionAcademicaId(asignacionId);
        request.setDia(DiasSemana.LUNES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        ConflictoHorarioException error =
                assertThrows(
                        ConflictoHorarioException.class,
                        () -> horarioService.crearClaseProgramada(
                                request,
                                "rector@yarumito.edu.co"));

        assertEquals("GRUPO_OCUPADO", error.getCodigo());

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H01_debeRechazarMoverClaseSiGrupoEstaOcupado() {

        UUID claseId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        ClaseProgramada clase = mock(ClaseProgramada.class);
        AsignacionAcademica asignacion =
                mock(AsignacionAcademica.class);
        Grupo grupo = mock(Grupo.class);
        Horario horario = mock(Horario.class);
        FranjaHoraria franja = mock(FranjaHoraria.class);
        Aula aula = mock(Aula.class);
        Sede sede = mock(Sede.class);

        when(clase.getId()).thenReturn(claseId);
        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);
        when(clase.getHorarioId()).thenReturn(horario);
        when(asignacion.getGrupo()).thenReturn(grupo);

        when(grupo.getId()).thenReturn(grupoId);
        when(horario.getId()).thenReturn(horarioId);
        when(franja.getId()).thenReturn(franjaId);

        // H-08: datos válidos del aula.
        when(aula.isActiva()).thenReturn(true);
        when(aula.getSede()).thenReturn(sede);
        when(grupo.getSede()).thenReturn(sede);
        when(sede.getId()).thenReturn(sedeId);

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(claseProgramadaRepository.existeGrupoOcupadoExcluyendo(
                horarioId,
                grupoId,
                DiasSemana.MARTES,
                franjaId,
                claseId))
                .thenReturn(true);

        ActualizarClaseProgramadaRequest request =
                new ActualizarClaseProgramadaRequest();

        request.setDia(DiasSemana.MARTES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        ConflictoHorarioException error =
                assertThrows(
                        ConflictoHorarioException.class,
                        () -> horarioService.actualizarClaseProgramada(
                                claseId,
                                request));

        assertEquals("GRUPO_OCUPADO", error.getCodigo());

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H01_debePermitirMoverClaseSinConflicto() {

        UUID claseId = UUID.randomUUID();
        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID horarioId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        ClaseProgramada clase = mock(ClaseProgramada.class);
        AsignacionAcademica asignacion =
                mock(AsignacionAcademica.class);
        Asignatura asignatura = mock(Asignatura.class);
        Grupo grupo = mock(Grupo.class);
        Horario horario = mock(Horario.class);
        AnioEscolar anio = mock(AnioEscolar.class);
        Docente docente = mock(Docente.class);
        FranjaHoraria franja = mock(FranjaHoraria.class);
        Aula aula = mock(Aula.class);
        Sede sede = mock(Sede.class);

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(clase.getId()).thenReturn(claseId);
        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);
        when(clase.getHorarioId()).thenReturn(horario);

        when(asignacion.getGrupo()).thenReturn(grupo);
        when(asignacion.getDocente()).thenReturn(docente);

        // H-02.
        when(asignacion.getAsignatura()).thenReturn(asignatura);
        when(asignatura.getId()).thenReturn(asignaturaId);

        when(grupo.getId()).thenReturn(grupoId);
        when(horario.getId()).thenReturn(horarioId);
        when(horario.getAnioEscolarId()).thenReturn(anio);
        when(anio.getId()).thenReturn(anioId);
        when(docente.getId()).thenReturn(docenteId);
        when(franja.getId()).thenReturn(franjaId);

        // H-08: aula activa y sede coincidente.
        when(aula.isActiva()).thenReturn(true);
        when(aula.getSede()).thenReturn(sede);
        when(grupo.getSede()).thenReturn(sede);
        when(sede.getId()).thenReturn(sedeId);

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(claseProgramadaRepository.findByHorarioAndGrupo(
                horarioId,
                grupoId))
                .thenReturn(List.of());

        when(claseProgramadaRepository.save(clase))
                .thenReturn(clase);

        when(horarioMapper.toClaseResponse(clase))
                .thenReturn(response);

        ActualizarClaseProgramadaRequest request =
                new ActualizarClaseProgramadaRequest();

        request.setDia(DiasSemana.MIERCOLES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        ClaseProgramadaResponse resultado =
                horarioService.actualizarClaseProgramada(
                        claseId,
                        request);

        assertEquals(response, resultado);

        verify(claseProgramadaRepository)
                .existeGrupoOcupadoExcluyendo(
                        horarioId,
                        grupoId,
                        DiasSemana.MIERCOLES,
                        franjaId,
                        claseId);

        verify(claseProgramadaRepository)
                .findByHorarioAndGrupo(horarioId, grupoId);

        verify(claseProgramadaRepository).save(clase);
    }
}
