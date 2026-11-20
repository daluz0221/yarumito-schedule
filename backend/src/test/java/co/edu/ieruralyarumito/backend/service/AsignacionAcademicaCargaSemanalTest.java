package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionAcademicaCargaSemanalTest {

    @Mock
    private AsignacionAcademicaRepository asignacionRepository;

    @Mock
    private AnioEscolarRepository anioRepository;

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

    private final UUID anioId = UUID.randomUUID();
    private final UUID docenteId = UUID.randomUUID();
    private final UUID grupoId = UUID.randomUUID();
    private final UUID asignaturaId = UUID.randomUUID();

    private AnioEscolar anio;
    private Docente docente;

    // Prepara las entidades y dependencias necesarias
    // para registrar una asignación académica.
    private CrearAsignacionAcademicaRequest preparar(
            int ordinarias,
            int extras,
            int horasSolicitadas) {

        anio = mock(AnioEscolar.class);
        docente = mock(Docente.class);

        Grupo grupo = mock(Grupo.class);
        Asignatura asignatura = mock(Asignatura.class);
        Idoneidad idoneidad = mock(Idoneidad.class);
        Usuario usuario = mock(Usuario.class);

        when(anio.getId()).thenReturn(anioId);

        when(docente.getId()).thenReturn(docenteId);
        when(docente.getEstado())
                .thenReturn(EstadoDocente.ACTIVO);
        when(docente.getHorasSemanalesContratadas())
                .thenReturn(ordinarias);
        when(docente.getMaxHorasExtra())
                .thenReturn(extras);

        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(grupo.getId()).thenReturn(grupoId);

        when(asignatura.getId()).thenReturn(asignaturaId);

        when(idoneidad.getAsignatura())
                .thenReturn(asignatura);
        when(idoneidad.getVigenteDesde())
                .thenReturn(LocalDate.now().minusDays(1));

        when(anioRepository.findById(anioId))
                .thenReturn(Optional.of(anio));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(grupo));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(usuarioRepository.findByCorreo(
                "rector@yarumito.edu.co"))
                .thenReturn(Optional.of(usuario));

        when(idoneidadRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(idoneidad));

        CrearAsignacionAcademicaRequest request =
                new CrearAsignacionAcademicaRequest();

        request.setAnioEscolarId(anioId);
        request.setDocenteId(docenteId);
        request.setGrupoId(grupoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(horasSolicitadas);

        return request;
    }

    // CORRECCIÓN:
    // Utilizamos una entidad real para representar
    // una asignación existente.
    // Evitamos crear mocks dentro de thenReturn().
    private AsignacionAcademica existente(
            AnioEscolar anioExistente,
            int horas) {

        AsignacionAcademica asignacion =
                new AsignacionAcademica();

        asignacion.setAnioEscolar(anioExistente);
        asignacion.setHorasSemanales(horas);

        return asignacion;
    }

    // Simula únicamente el guardado exitoso.
    private void prepararGuardadoExitoso() {

        when(mapper.toEntity(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(new AsignacionAcademica());

        when(asignacionRepository.save(
                any(AsignacionAcademica.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(mapper.toResponse(
                any(AsignacionAcademica.class)))
                .thenReturn(
                        new AsignacionAcademicaResponse());
    }

    // H-05: una asignación de 60 horas debe rechazarse.
    @Test
    void H05_debeRechazarAsignacionDe60Horas() {

        CrearAsignacionAcademicaRequest request =
                preparar(22, 10, 60);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.crearAsignacionAcademica(
                        request,
                        "rector@yarumito.edu.co"));

        verify(asignacionRepository, never())
                .save(any(AsignacionAcademica.class));
    }

    // H-05: el límite de 32 horas sí debe permitirse.
    @Test
    void H05_debePermitirExactamente32Horas() {

        CrearAsignacionAcademicaRequest request =
                preparar(22, 10, 2);

        AsignacionAcademica asignacionExistente =
                existente(anio, 30);

        when(asignacionRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(asignacionExistente));

        prepararGuardadoExitoso();

        assertDoesNotThrow(
                () -> service.crearAsignacionAcademica(
                        request,
                        "rector@yarumito.edu.co"));

        verify(asignacionRepository)
                .save(any(AsignacionAcademica.class));
    }

    // H-05: una carga acumulada de 33 horas debe rechazarse.
    @Test
    void H05_debeRechazarCargaAcumuladaDe33Horas() {

        CrearAsignacionAcademicaRequest request =
                preparar(22, 10, 3);

        AsignacionAcademica asignacionExistente =
                existente(anio, 30);

        when(asignacionRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(asignacionExistente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.crearAsignacionAcademica(
                        request,
                        "rector@yarumito.edu.co"));

        verify(asignacionRepository, never())
                .save(any(AsignacionAcademica.class));
    }

    // H-05: las asignaciones de otro año escolar
    // no deben sumarse a la carga del año actual.
    @Test
    void H05_noDebeSumarHorasDeOtroAnio() {

        CrearAsignacionAcademicaRequest request =
                preparar(22, 10, 4);

        AnioEscolar otroAnio =
                mock(AnioEscolar.class);

        when(otroAnio.getId())
                .thenReturn(UUID.randomUUID());

        AsignacionAcademica asignacionAnterior =
                existente(otroAnio, 30);

        when(asignacionRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(asignacionAnterior));

        prepararGuardadoExitoso();

        assertDoesNotThrow(
                () -> service.crearAsignacionAcademica(
                        request,
                        "rector@yarumito.edu.co"));

        verify(asignacionRepository)
                .save(any(AsignacionAcademica.class));
    }

    // H-05: respeta un límite individual menor
    // al máximo institucional de 32 horas.
    @Test
    void H05_debeRespetarLimiteIndividualInferior() {

        CrearAsignacionAcademicaRequest request =
                preparar(22, 4, 2);

        AsignacionAcademica asignacionExistente =
                existente(anio, 25);

        when(asignacionRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(asignacionExistente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.crearAsignacionAcademica(
                        request,
                        "rector@yarumito.edu.co"));

        verify(asignacionRepository, never())
                .save(any(AsignacionAcademica.class));
    }
}
