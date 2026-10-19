package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.GrupoResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

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

    @Mock
    private AulaRepository aulaRepository;

    @InjectMocks
    private GrupoService grupoService;

    @Test
    void registrarGrupo_debeRechazarCodigoDuplicadoEnMismoAnio() {

        UUID anioEscolarId =
                UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("601");
        request.setAnioEscolarId(anioEscolarId);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "601"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        grupoService
                                .registrarGrupo(request));
    }

    @Test
    void registrarGrupo_debeRegistrarCorrectamente() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo(" 601 ");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(28);
        request.setAulaFijaId(null);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "601"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(
                any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(
                        request);

        assertEquals(
                "601",
                response.getCodigo());

        assertEquals(
                6,
                response.getGradoNivel());

        assertEquals(
                "SEXTO",
                response.getGradoNombre());

        assertEquals(
                2027,
                response.getAnioEscolar());

        assertEquals(
                "I.E.R. YARUMITO",
                response.getSedeNombre());

        assertEquals(
                28,
                response.getCantidadEstudiantes());

        assertNull(
                response.getAulaFijaId());

        assertTrue(
                response.isActivo());
    }

    @Test
    void registrarGrupo_debePermitirDirectorNoAsignado() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(7);
        grado.setNombre("SÉPTIMO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("701");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setDirectorGrupoId(null);
        request.setAulaFijaId(null);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "701"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(
                any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(
                        request);

        assertEquals(
                "701",
                response.getCodigo());

        assertNull(
                response.getDirectorGrupoId());

        assertNull(
                response.getDirectorGrupoNombre());
    }

    @Test
    void registrarGrupo_debeRechazarDirectorInexistente() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID docenteId =
                UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("801");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setDirectorGrupoId(docenteId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "801"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(new Grado()));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(new AnioEscolar()));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(new Sede()));

        when(docenteRepository.findById(docenteId))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        grupoService
                                .registrarGrupo(request));
    }

    @Test
    void registrarGrupo_debeRechazarGradoInexistente() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("901");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "901"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        grupoService
                                .registrarGrupo(request));
    }

    @Test
    void consultarGrupo_debeRechazarGrupoInexistente() {

        UUID grupoId =
                UUID.randomUUID();

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        grupoService
                                .consultarGrupo(grupoId));
    }

    @Test
    void actualizarGrupo_debeRechazarCodigoDuplicadoEnMismoAnio() {

        UUID grupoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        Grupo existente =
                new Grupo();

        existente.setCodigo("601");
        existente.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("602");
        request.setAnioEscolarId(anioEscolarId);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(existente));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "602",
                        grupoId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        grupoService
                                .actualizarGrupo(
                                        grupoId,
                                        request));
    }

    @Test
    void actualizarGrupo_debePermitirCambiarEstado() {

        UUID grupoId =
                UUID.randomUUID();

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(11);
        grado.setNombre("UNDÉCIMO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        Grupo existente =
                new Grupo();

        existente.setCodigo("1101");
        existente.setGrado(grado);
        existente.setAnioEscolar(anioEscolar);
        existente.setSede(sede);
        existente.setAulaFija(null);
        existente.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("1101");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(null);
        request.setActivo(false);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(existente));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "1101",
                        grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(
                any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.actualizarGrupo(
                        grupoId,
                        request);

        assertFalse(
                response.isActivo());

        assertEquals(
                "1101",
                response.getCodigo());

        assertEquals(
                11,
                response.getGradoNivel());
    }

    @Test
    void registrarGrupo_debePermitirGrupoSinAulaFija() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("602");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(null);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "602"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(
                any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(
                        request);

        assertNull(
                response.getAulaFijaId());
    }

    @Test
    void registrarGrupo_debeAsignarAulaFijaActiva() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        Aula aula =
                mock(Aula.class);

        when(aula.getId())
                .thenReturn(aulaId);

        when(aula.isActiva())
                .thenReturn(true);

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("603");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "603"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.of(aula));

        when(grupoRepository.save(
                any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(
                        request);

        assertEquals(
                aulaId,
                response.getAulaFijaId());
    }

    @Test
    void registrarGrupo_debeRechazarAulaFijaInexistente() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaId =
                UUID.randomUUID();

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("604");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "604"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(new Grado()));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(new AnioEscolar()));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(new Sede()));

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        grupoService
                                .registrarGrupo(request));
    }

    @Test
    void registrarGrupo_debeRechazarAulaFijaInactiva() {

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaId =
                UUID.randomUUID();

        Aula aula =
                new Aula();

        aula.setActiva(false);

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("605");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "605"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(new Grado()));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(
                        Optional.of(new AnioEscolar()));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(new Sede()));

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.of(aula));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () ->
                        grupoService
                                .registrarGrupo(request));
    }

    @Test
    void actualizarGrupo_debeConservarMismaAulaAunqueEsteInactiva() {

        UUID grupoId =
                UUID.randomUUID();

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        Aula aulaActual =
                mock(Aula.class);

        when(aulaActual.getId())
                .thenReturn(aulaId);

        Grupo grupo =
                new Grupo();

        grupo.setCodigo("606");
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setCantidadEstudiantes(30);
        grupo.setAulaFija(aulaActual);
        grupo.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("606");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(31);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(grupo));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "606",
                        grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.actualizarGrupo(
                        grupoId,
                        request);

        assertEquals(
                aulaId,
                response.getAulaFijaId());

        assertEquals(
                31,
                response.getCantidadEstudiantes());

        verify(aulaRepository, never())
                .findById(aulaId);
    }

    @Test
    void actualizarGrupo_debeRechazarCambioAOtraAulaInactiva() {

        UUID grupoId =
                UUID.randomUUID();

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaActualId =
                UUID.randomUUID();

        UUID aulaNuevaId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        AnioEscolar anioEscolar =
                new AnioEscolar();

        Sede sede =
                new Sede();

        Aula aulaActual =
                mock(Aula.class);

        when(aulaActual.getId())
                .thenReturn(aulaActualId);

        Aula aulaNueva =
                mock(Aula.class);

        when(aulaNueva.isActiva())
                .thenReturn(false);

        Grupo grupo =
                new Grupo();

        grupo.setCodigo("607");
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setAulaFija(aulaActual);
        grupo.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("607");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(aulaNuevaId);
        request.setActivo(true);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(grupo));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "607",
                        grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository.findById(aulaNuevaId))
                .thenReturn(
                        Optional.of(aulaNueva));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () ->
                        grupoService.actualizarGrupo(
                                grupoId,
                                request));
    }

    @Test
    void actualizarGrupo_debePermitirCambioAOtraAulaActiva() {

        UUID grupoId =
                UUID.randomUUID();

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaActualId =
                UUID.randomUUID();

        UUID aulaNuevaId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(7);
        grado.setNombre("SÉPTIMO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        Aula aulaActual =
                mock(Aula.class);

        when(aulaActual.getId())
                .thenReturn(aulaActualId);

        Aula aulaNueva =
                mock(Aula.class);

        when(aulaNueva.getId())
                .thenReturn(aulaNuevaId);

        when(aulaNueva.isActiva())
                .thenReturn(true);

        Grupo grupo =
                new Grupo();

        grupo.setCodigo("701");
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setAulaFija(aulaActual);
        grupo.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("701");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(aulaNuevaId);
        request.setActivo(true);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(grupo));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "701",
                        grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository.findById(aulaNuevaId))
                .thenReturn(
                        Optional.of(aulaNueva));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.actualizarGrupo(
                        grupoId,
                        request);

        assertEquals(
                aulaNuevaId,
                response.getAulaFijaId());

        verify(aulaRepository)
                .findById(aulaNuevaId);
    }

    @Test
    void actualizarGrupo_debePermitirRetirarAulaFija() {

        UUID grupoId =
                UUID.randomUUID();

        UUID gradoId =
                UUID.randomUUID();

        UUID anioEscolarId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        UUID aulaActualId =
                UUID.randomUUID();

        Grado grado =
                new Grado();

        grado.setNivel(8);
        grado.setNombre("OCTAVO");

        AnioEscolar anioEscolar =
                new AnioEscolar();

        anioEscolar.setAnio(2027);

        Sede sede =
                new Sede();

        sede.setNombre(
                "I.E.R. YARUMITO");

        Aula aulaActual =
                mock(Aula.class);

        Grupo grupo =
                new Grupo();

        grupo.setCodigo("801");
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setAulaFija(aulaActual);
        grupo.setActivo(true);

        ActualizarGrupoRequest request =
                new ActualizarGrupoRequest();

        request.setCodigo("801");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setAulaFijaId(null);
        request.setActivo(true);

        when(grupoRepository.findById(grupoId))
                .thenReturn(
                        Optional.of(grupo));

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigoAndIdNot(
                        anioEscolarId,
                        "801",
                        grupoId))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(
                        Optional.of(grado));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(
                        Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.actualizarGrupo(
                        grupoId,
                        request);

        assertNull(
                response.getAulaFijaId());

        verify(aulaRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    void registrarGrupo_debeGenerarAdvertenciaLogisticaCuandoSuperaCapacidadAula() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        Aula aula = mock(Aula.class);

        when(aula.getId())
                .thenReturn(aulaId);

        when(aula.isActiva())
                .thenReturn(true);

        when(aula.getCapacidad())
                .thenReturn(30);

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("610");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(34);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "610"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        assertEquals(
                "La cantidad de estudiantes del grupo supera "
                        + "la capacidad registrada del aula.",
                response.getAdvertenciaLogistica());

        assertEquals(
                aulaId,
                response.getAulaFijaId());
    }

    @Test
    void registrarGrupo_noDebeGenerarAdvertenciaCuandoCapacidadEsSuficiente() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        Aula aula = mock(Aula.class);

        when(aula.getId())
                .thenReturn(aulaId);

        when(aula.isActiva())
                .thenReturn(true);

        when(aula.getCapacidad())
                .thenReturn(35);

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("611");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(30);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "611"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        assertNull(
                response.getAdvertenciaLogistica());
    }

    @Test
    void registrarGrupo_noDebeGenerarAdvertenciaCuandoCapacidadAulaEsNull() {

        UUID gradoId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);
        grado.setNombre("SEXTO");

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Sede sede = new Sede();
        sede.setNombre("I.E.R. YARUMITO");

        Aula aula = mock(Aula.class);

        when(aula.getId())
                .thenReturn(aulaId);

        when(aula.isActiva())
                .thenReturn(true);

        when(aula.getCapacidad())
                .thenReturn(null);

        CrearGrupoRequest request =
                new CrearGrupoRequest();

        request.setCodigo("612");
        request.setGradoId(gradoId);
        request.setAnioEscolarId(anioEscolarId);
        request.setSedeId(sedeId);
        request.setCantidadEstudiantes(32);
        request.setAulaFijaId(aulaId);
        request.setActivo(true);

        when(grupoRepository
                .existsByAnioEscolar_IdAndCodigo(
                        anioEscolarId,
                        "612"))
                .thenReturn(false);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(anioEscolarRepository
                .findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(grupoRepository.save(any(Grupo.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GrupoResponse response =
                grupoService.registrarGrupo(request);

        assertNull(
                response.getAdvertenciaLogistica());
    }
}
