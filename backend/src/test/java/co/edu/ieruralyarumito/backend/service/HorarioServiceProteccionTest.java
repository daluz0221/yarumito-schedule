package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceProteccionTest {

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
    private UUID franjaId;
    private UUID aulaId;

    private Horario horario;
    private ClaseProgramada clase;

    @BeforeEach
    void prepararDatos() {

        claseId = UUID.randomUUID();
        asignacionId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        aulaId = UUID.randomUUID();

        horario = new Horario();
        clase = new ClaseProgramada();
        clase.setHorarioId(horario);
    }

    @Test
    void H10_rechazaCrearEnHorarioPublicado() {

        prepararCreacion(EstadoHorario.PUBLICADO);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaCrearEnHorarioArchivado() {

        prepararCreacion(EstadoHorario.ARCHIVADO);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaMoverEnHorarioPublicado() {

        prepararClase(EstadoHorario.PUBLICADO, false);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest()));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaMoverEnHorarioArchivado() {

        prepararClase(EstadoHorario.ARCHIVADO, false);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest()));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaEliminarEnHorarioPublicado() {

        prepararClase(EstadoHorario.PUBLICADO, false);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.eliminarClaseProgramada(claseId));

        verify(claseProgramadaRepository, never())
                .delete(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaEliminarEnHorarioArchivado() {

        prepararClase(EstadoHorario.ARCHIVADO, false);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.eliminarClaseProgramada(claseId));

        verify(claseProgramadaRepository, never())
                .delete(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaMoverClaseBloqueadaEnBorrador() {

        prepararClase(EstadoHorario.BORRADOR, true);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest()));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H10_rechazaEliminarClaseBloqueadaEnBorrador() {

        prepararClase(EstadoHorario.BORRADOR, true);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.eliminarClaseProgramada(claseId));

        verify(claseProgramadaRepository, never())
                .delete(any(ClaseProgramada.class));
    }

    @Test
    void H10_permiteEliminarClaseNoBloqueadaEnBorrador() {

        prepararClase(EstadoHorario.BORRADOR, false);

        // H-07: datos necesarios para recalcular la carga
        // semanal después de eliminar la clase.
        UUID anioId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        AnioEscolar anio = mock(AnioEscolar.class);
        AsignacionAcademica asignacion =
                mock(AsignacionAcademica.class);
        Docente docente = mock(Docente.class);

        horario.setAnioEscolarId(anio);
        clase.setAsignacionAcademicaId(asignacion);

        when(anio.getId()).thenReturn(anioId);
        when(asignacion.getDocente()).thenReturn(docente);
        when(docente.getId()).thenReturn(docenteId);

        // H-07: no quedan clases del docente después
        // de la eliminación.
        when(claseProgramadaRepository.findByDocenteAndAnioEscolar(
                anioId, docenteId))
                .thenReturn(List.of());

        assertDoesNotThrow(
                () -> horarioService.eliminarClaseProgramada(claseId));

        verify(claseProgramadaRepository).delete(clase);

        verify(claseProgramadaRepository)
                .findByDocenteAndAnioEscolar(anioId, docenteId);
    }

    private void prepararClase(
            EstadoHorario estado,
            boolean bloqueada) {

        horario.setEstado(estado);
        clase.setBloqueada(bloqueada);

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));
    }

    private void prepararCreacion(EstadoHorario estado) {

        horario.setEstado(estado);

        AsignacionAcademica asignacion =
                mock(AsignacionAcademica.class);

        Grupo grupo = mock(Grupo.class);
        AnioEscolar anio = mock(AnioEscolar.class);
        Sede sede = mock(Sede.class);
        Aula aula = mock(Aula.class);
        FranjaHoraria franja = mock(FranjaHoraria.class);

        UUID anioId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(asignacion.getGrupo()).thenReturn(grupo);

        when(grupo.getAnioEscolar()).thenReturn(anio);
        when(grupo.getSede()).thenReturn(sede);

        when(anio.getId()).thenReturn(anioId);
        when(sede.getId()).thenReturn(sedeId);

        when(franjaHorariaRepository.findById(franjaId))
                .thenReturn(Optional.of(franja));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(aula.isActiva()).thenReturn(true);
        when(aula.getSede()).thenReturn(sede);

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        anioId,
                        sedeId))
                .thenReturn(Optional.of(horario));
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
