package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioServiceVersionTableroTest {

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
    private Horario publicado;
    private Horario borrador;

    @BeforeEach
    void prepararDatos() {

        anioId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        grupoId = UUID.randomUUID();

        AnioEscolar anio = spy(new AnioEscolar());
        lenient().doReturn(anioId).when(anio).getId();
        anio.setAnio(2027);

        Sede sede = spy(new Sede());
        lenient().doReturn(sedeId).when(sede).getId();
        sede.setNombre("Yarumito");

        Grado grado = spy(new Grado());
        lenient().doReturn(UUID.randomUUID()).when(grado).getId();
        grado.setNombre("SEXTO");

        Grupo grupo = spy(new Grupo());
        lenient().doReturn(grupoId).when(grupo).getId();
        grupo.setCodigo("601");
        grupo.setAnioEscolar(anio);
        grupo.setSede(sede);
        grupo.setGrado(grado);

        publicado = spy(new Horario());
        lenient().doReturn(UUID.randomUUID()).when(publicado).getId();
        publicado.setVersion(1);
        publicado.setEstado(EstadoHorario.PUBLICADO);

        borrador = spy(new Horario());
        lenient().doReturn(UUID.randomUUID()).when(borrador).getId();
        borrador.setVersion(2);
        borrador.setEstado(EstadoHorario.BORRADOR);

        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(grupo));
    }

    @Test
    void elTableroOficialMuestraElPublicadoAunqueExistaUnBorradorMasNuevo() {

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.PUBLICADO))
                .thenReturn(Optional.of(publicado));

        HorarioGrupoResponse tablero =
                horarioService.obtenerTablero(grupoId);

        assertEquals(publicado.getId(), tablero.getHorarioId());
        assertEquals(1, tablero.getVersionHorario());
        assertEquals(EstadoHorario.PUBLICADO, tablero.getEstadoHorario());
    }

    @Test
    void elTableroDeTrabajoMuestraElBorradorDeMejora() {

        when(horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        anioId,
                        sedeId,
                        EstadoHorario.BORRADOR))
                .thenReturn(Optional.of(borrador));

        HorarioGrupoResponse tablero =
                horarioService.obtenerTableroDeTrabajo(grupoId);

        assertEquals(borrador.getId(), tablero.getHorarioId());
        assertEquals(2, tablero.getVersionHorario());
        assertEquals(EstadoHorario.BORRADOR, tablero.getEstadoHorario());
    }
}
