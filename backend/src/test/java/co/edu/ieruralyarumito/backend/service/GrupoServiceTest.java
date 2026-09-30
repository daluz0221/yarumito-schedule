package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.GrupoResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de GrupoService.
@ExtendWith(MockitoExtension.class)
public class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @InjectMocks
    private GrupoService grupoService;

    // No permite repetir el mismo código de grupo
    // dentro del mismo año escolar.
    @Test
    void registrarGrupo_debeRechazarCodigoDuplicadoEnMismoAnio() {

        UUID anioEscolarId = UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("601");
        request.setAnioEscolarId(anioEscolarId);

        when(grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                "601"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> grupoService.registrarGrupo(request)
        );
    }

    // Verifica que un grupo válido pueda registrarse correctamente.
    @Test
    void registrarGrupo_debeRegistrarCorrectamente() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo(" 601 ");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(28);
        request.setActivo(true);

        when(grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                "601"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        assertEquals("601", response.getCodigo());
        assertEquals(6, response.getGradoNivel());
        assertEquals("SEXTO", response.getGradoNombre());
        assertEquals(2027, response.getAnioEscolar());
        assertEquals(
                "I.E.R. YARUMITO",
                response.getSedeNombre());
        assertEquals(
                28,
                response.getCantidadEstudiantes());
        assertTrue(response.isActivo());
    }

    // El director de grupo es opcional.
    @Test
    void registrarGrupo_debePermitirDirectorNoAsignado() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(7);
        grado.setNombre("SÉPTIMO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("701");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setDirectorGrupoId(null);
        request.setActivo(true);

        when(grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                "701"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        assertEquals("701", response.getCodigo());
        assertEquals(null, response.getDirectorGrupoId());
        assertEquals(null, response.getDirectorGrupoNombre());
    }

    // Si se informa un director, el docente debe existir.
    @Test
    void registrarGrupo_debeRechazarDirectorInexistente() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("801");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setDirectorGrupoId(docenteId);
        request.setActivo(true);

        when(grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                "801"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(new Grado()));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(new AnioEscolar()));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(new Sede()));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> grupoService.registrarGrupo(request)
        );
    }

    // No permite crear un grupo asociado a un grado inexistente.
    @Test
    void registrarGrupo_debeRechazarGradoInexistente() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("901");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setActivo(true);

        when(grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                "901"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> grupoService.registrarGrupo(request)
        );
    }

    // Verifica que consultar un grupo inexistente genere el error correspondiente.
    @Test
    void consultarGrupo_debeRechazarGrupoInexistente() {

        UUID grupoId = UUID.randomUUID();

        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> grupoService.consultarGrupo(grupoId)
        );
    }

    // No permite actualizar un grupo usando un código
    // ya utilizado por otro grupo del mismo año escolar.
    @Test
    void actualizarGrupo_debeRechazarCodigoDuplicadoEnMismoAnio() {

        UUID grupoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();

        Grupo existente = new Grupo();
        existente.setCodigo("601");
        existente.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("602");
        request.setAnioEscolarId(anioEscolarId);

        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(existente));

        when(grupoRepository.existsByAnioEscolar_IdAndCodigoAndIdNot(
                anioEscolarId,
                "602",
                grupoId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> grupoService.actualizarGrupo(
                        grupoId,
                        request)
        );
    }

    // Verifica que el estado activo pueda modificarse.
    @Test
    void actualizarGrupo_debePermitirCambiarEstado() {

        UUID grupoId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(11);
        grado.setNombre("UNDÉCIMO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        Grupo existente = new Grupo();
        existente.setCodigo("1101");
        existente.setGrado(grado);
        existente.setAnioEscolar(anioEscolar);
        existente.setSede(sede);
        existente.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("1101");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setActivo(false);

        when(grupoRepository.findById(grupoId))
                .thenReturn(Optional.of(existente));

        when(grupoRepository.existsByAnioEscolar_IdAndCodigoAndIdNot(
                anioEscolarId,
                "1101",
                grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GrupoResponse response =
                grupoService.actualizarGrupo(
                        grupoId,
                        request);

        assertFalse(response.isActivo());
        assertEquals("1101", response.getCodigo());
        assertEquals(11, response.getGradoNivel());
    }
}