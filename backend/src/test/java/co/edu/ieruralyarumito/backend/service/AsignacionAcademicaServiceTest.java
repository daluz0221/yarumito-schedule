package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.ResumenAsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionAcademicaServiceTest {

    @Mock
    private AsignacionAcademicaRepository asignacionRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private PlanEstudiosRepository planEstudiosRepository;

    @Mock
    private AsignacionAcademicaMapper mapper;

    @InjectMocks
    private AsignacionAcademicaService service;

    @Test
    void H05_debeRechazarGrupoDeOtroAnio() {

        UUID anioId = UUID.randomUUID();
        UUID otroAnioId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearAsignacionAcademicaRequest request =
                new CrearAsignacionAcademicaRequest();

        request.setAnioEscolarId(anioId);
        request.setDocenteId(docenteId);
        request.setGrupoId(grupoId);
        request.setAsignaturaId(asignaturaId);

        AnioEscolar anio = mock(AnioEscolar.class);
        AnioEscolar otroAnio = mock(AnioEscolar.class);
        Docente docente = mock(Docente.class);
        Grupo grupo = mock(Grupo.class);
        Asignatura asignatura = mock(Asignatura.class);

        when(anio.getId()).thenReturn(anioId);
        when(otroAnio.getId()).thenReturn(otroAnioId);
        when(grupo.getAnioEscolar()).thenReturn(otroAnio);

        when(anioEscolarRepository.findById(anioId))
                .thenReturn(Optional.of(anio));
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));
        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(grupo));
        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findByCorreo("rector@yarumito.edu.co"))
                .thenReturn(Optional.of(mock(Usuario.class)));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.crearAsignacionAcademica(
                        request, "rector@yarumito.edu.co"));

        verify(asignacionRepository, never())
                .save(any(AsignacionAcademica.class));
    }

    @Test
    void H06A_debeRechazarDocenteRetirado() {

        UUID anioId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearAsignacionAcademicaRequest request =
                new CrearAsignacionAcademicaRequest();

        request.setAnioEscolarId(anioId);
        request.setDocenteId(docenteId);
        request.setGrupoId(grupoId);
        request.setAsignaturaId(asignaturaId);

        AnioEscolar anio = mock(AnioEscolar.class);
        Docente docente = mock(Docente.class);
        Grupo grupo = mock(Grupo.class);
        Asignatura asignatura = mock(Asignatura.class);

        when(anio.getId()).thenReturn(anioId);
        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(docente.getEstado()).thenReturn(EstadoDocente.RETIRADO);

        when(anioEscolarRepository.findById(anioId))
                .thenReturn(Optional.of(anio));
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));
        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(grupo));
        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findByCorreo("rector@yarumito.edu.co"))
                .thenReturn(Optional.of(mock(Usuario.class)));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.crearAsignacionAcademica(
                        request, "rector@yarumito.edu.co"));

        verify(asignacionRepository, never())
                .save(any(AsignacionAcademica.class));
    }

    @Test
    void Q01_debeConsultarPlanPorIds() {

        UUID anioId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();

        AnioEscolar anio = mock(AnioEscolar.class);
        Grupo grupo = mock(Grupo.class);
        Grado grado = mock(Grado.class);
        PlanEstudios plan = mock(PlanEstudios.class);

        when(anio.getId()).thenReturn(anioId);
        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(grupo.getGrado()).thenReturn(grado);
        when(grupo.getCodigo()).thenReturn("701");
        when(grado.getId()).thenReturn(gradoId);
        when(grado.getNombre()).thenReturn("SEPTIMO");
        when(grado.getHorasSemanalesEsperadas()).thenReturn(30);
        when(plan.getHorasSemanales()).thenReturn(30);

        when(anioEscolarRepository.findById(anioId))
                .thenReturn(Optional.of(anio));
        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(grupo));

        when(planEstudiosRepository
                .findByAnioEscolar_IdAndGrado_Id(anioId, gradoId))
                .thenReturn(List.of(plan));

        ResumenAsignacionAcademicaResponse resultado =
                service.generateResumenAsignacionAcademica(
                        anioId, grupoId);

        assertEquals("701", resultado.getGrupo());
        assertEquals(30, resultado.getHorasRequeridas());

        verify(planEstudiosRepository)
                .findByAnioEscolar_IdAndGrado_Id(anioId, gradoId);
    }
}
