package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de RestriccionDocenteService.
@ExtendWith(MockitoExtension.class)
class RestriccionDocenteServiceTest {

    @Mock
    private RestriccionDocenteRepository restriccionDocenteRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    private RestriccionDocenteService restriccionDocenteService;

    @BeforeEach
    void setUp() {

        restriccionDocenteService =
                new RestriccionDocenteService(
                        restriccionDocenteRepository,
                        docenteRepository,
                        anioEscolarRepository,
                        franjaHorariaRepository
                );
    }

    // Registra una restricción PREFERENTE válida.
    @Test
    void registrarRestriccion_debeRegistrarPreferenteCorrectamente() {

        UUID docenteId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID franjaHorariaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        docente.setEstado(EstadoDocente.ACTIVO);
        doReturn(docenteId).when(docente).getId();

        AnioEscolar anioEscolar =
                spy(new AnioEscolar());
        doReturn(anioEscolarId)
                .when(anioEscolar)
                .getId();

        FranjaHoraria franjaHoraria =
                spy(new FranjaHoraria());
        franjaHoraria.setEsDescanso(false);
        doReturn(franjaHorariaId)
                .when(franjaHoraria)
                .getId();

        CrearRestriccionDocenteRequest request =
                new CrearRestriccionDocenteRequest();

        request.setDocenteId(docenteId);
        request.setAnioEscolarId(anioEscolarId);
        request.setDia(DiaSemana.LUNES);
        request.setFranjaHorariaId(franjaHorariaId);
        request.setTipo(
                TipoRestriccionDocente.PREFERENTE);
        request.setMotivo(
                "Preferencia institucional registrada");

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(franjaHorariaRepository.findById(franjaHorariaId))
                .thenReturn(Optional.of(franjaHoraria));

        when(restriccionDocenteRepository
                .existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                        docenteId,
                        anioEscolarId,
                        DiaSemana.LUNES,
                        franjaHorariaId))
                .thenReturn(false);

        when(restriccionDocenteRepository
                .save(any(RestriccionDocente.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RestriccionDocenteResponse response =
                restriccionDocenteService
                        .registrarRestriccion(request);

        assertEquals(
                TipoRestriccionDocente.PREFERENTE,
                response.getTipo());

        assertEquals(
                DiaSemana.LUNES,
                response.getDia());

        assertEquals(
                docenteId,
                response.getDocenteId());

        assertEquals(
                anioEscolarId,
                response.getAnioEscolarId());

        assertEquals(
                franjaHorariaId,
                response.getFranjaHorariaId());

        verify(restriccionDocenteRepository)
                .save(any(RestriccionDocente.class));
    }

    // No permite registrar dos condiciones para
    // el mismo docente, año, día y franja.
    @Test
    void registrarRestriccion_debeRechazarCondicionDuplicada() {

        UUID docenteId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID franjaHorariaId = UUID.randomUUID();

        Docente docente = new Docente();
        docente.setEstado(EstadoDocente.ACTIVO);

        AnioEscolar anioEscolar =
                new AnioEscolar();

        FranjaHoraria franjaHoraria =
                new FranjaHoraria();

        franjaHoraria.setEsDescanso(false);

        CrearRestriccionDocenteRequest request =
                new CrearRestriccionDocenteRequest();

        request.setDocenteId(docenteId);
        request.setAnioEscolarId(anioEscolarId);
        request.setDia(DiaSemana.MARTES);
        request.setFranjaHorariaId(franjaHorariaId);
        request.setTipo(
                TipoRestriccionDocente.NO_DISPONIBLE);
        request.setMotivo(
                "El docente no está disponible");

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(franjaHorariaRepository.findById(franjaHorariaId))
                .thenReturn(Optional.of(franjaHoraria));

        when(restriccionDocenteRepository
                .existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                        docenteId,
                        anioEscolarId,
                        DiaSemana.MARTES,
                        franjaHorariaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> restriccionDocenteService
                        .registrarRestriccion(request));

        verify(restriccionDocenteRepository, never())
                .save(any());
    }

    // Una franja de descanso ya es no asignable,
    // por lo que no admite restricciones docentes.
    @Test
    void registrarRestriccion_debeRechazarFranjaDeDescanso() {

        UUID docenteId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID franjaHorariaId = UUID.randomUUID();

        Docente docente = new Docente();
        docente.setEstado(EstadoDocente.ACTIVO);

        AnioEscolar anioEscolar =
                new AnioEscolar();

        FranjaHoraria descanso =
                new FranjaHoraria();

        descanso.setEsDescanso(true);

        CrearRestriccionDocenteRequest request =
                new CrearRestriccionDocenteRequest();

        request.setDocenteId(docenteId);
        request.setAnioEscolarId(anioEscolarId);
        request.setDia(DiaSemana.MIERCOLES);
        request.setFranjaHorariaId(franjaHorariaId);
        request.setTipo(
                TipoRestriccionDocente.NO_DISPONIBLE);
        request.setMotivo(
                "Restricción sobre descanso");

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(franjaHorariaRepository.findById(franjaHorariaId))
                .thenReturn(Optional.of(descanso));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> restriccionDocenteService
                        .registrarRestriccion(request));

        verify(restriccionDocenteRepository, never())
                .save(any());
    }

    // Un docente que no está ACTIVO no participa
    // en nuevas condiciones para programación académica.
    @Test
    void registrarRestriccion_debeRechazarDocenteNoActivo() {

        UUID docenteId = UUID.randomUUID();

        Docente docente = new Docente();
        docente.setEstado(EstadoDocente.LICENCIA);

        CrearRestriccionDocenteRequest request =
                new CrearRestriccionDocenteRequest();

        request.setDocenteId(docenteId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> restriccionDocenteService
                        .registrarRestriccion(request));

        verify(restriccionDocenteRepository, never())
                .save(any());
    }

    // Cambia PREFERENTE por NO_DISPONIBLE
    // sobre el mismo registro.
    @Test
    void actualizarRestriccion_debeCambiarTipoSinCrearOtraRestriccion() {

        UUID restriccionId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID franjaHorariaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        docente.setEstado(EstadoDocente.ACTIVO);
        doReturn(docenteId)
                .when(docente)
                .getId();

        AnioEscolar anioEscolar =
                spy(new AnioEscolar());

        doReturn(anioEscolarId)
                .when(anioEscolar)
                .getId();

        FranjaHoraria franjaHoraria =
                spy(new FranjaHoraria());

        franjaHoraria.setEsDescanso(false);

        doReturn(franjaHorariaId)
                .when(franjaHoraria)
                .getId();

        RestriccionDocente restriccion =
                new RestriccionDocente();

        restriccion.setDocente(docente);
        restriccion.setAnioEscolar(anioEscolar);
        restriccion.setDia(DiaSemana.JUEVES);
        restriccion.setFranjaHoraria(franjaHoraria);
        restriccion.setTipo(
                TipoRestriccionDocente.PREFERENTE);
        restriccion.setMotivo(
                "Preferencia inicial");

        ActualizarRestriccionDocenteRequest request =
                new ActualizarRestriccionDocenteRequest();

        request.setTipo(
                TipoRestriccionDocente.NO_DISPONIBLE);

        request.setMotivo(
                "Cambio administrativo que impide disponibilidad");

        when(restriccionDocenteRepository
                .findById(restriccionId))
                .thenReturn(Optional.of(restriccion));

        when(restriccionDocenteRepository
                .save(same(restriccion)))
                .thenReturn(restriccion);

        RestriccionDocenteResponse response =
                restriccionDocenteService
                        .actualizarRestriccion(
                                restriccionId,
                                request);

        assertEquals(
                TipoRestriccionDocente.NO_DISPONIBLE,
                response.getTipo());

        assertEquals(
                "Cambio administrativo que impide disponibilidad",
                response.getMotivo());

        verify(restriccionDocenteRepository)
                .save(same(restriccion));
    }

    // Un documento soporte vacío se normaliza a null
    // porque el soporte es opcional.
    @Test
    void registrarRestriccion_debeNormalizarDocumentoSoporteVacio() {

        UUID docenteId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID franjaHorariaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        docente.setEstado(EstadoDocente.ACTIVO);
        doReturn(docenteId)
                .when(docente)
                .getId();

        AnioEscolar anioEscolar =
                spy(new AnioEscolar());

        doReturn(anioEscolarId)
                .when(anioEscolar)
                .getId();

        FranjaHoraria franjaHoraria =
                spy(new FranjaHoraria());

        franjaHoraria.setEsDescanso(false);

        doReturn(franjaHorariaId)
                .when(franjaHoraria)
                .getId();

        CrearRestriccionDocenteRequest request =
                new CrearRestriccionDocenteRequest();

        request.setDocenteId(docenteId);
        request.setAnioEscolarId(anioEscolarId);
        request.setDia(DiaSemana.VIERNES);
        request.setFranjaHorariaId(franjaHorariaId);
        request.setTipo(
                TipoRestriccionDocente.PREFERENTE);
        request.setMotivo(
                "Preferencia registrada");
        request.setDocumentoSoporte(new byte[0]);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(franjaHorariaRepository.findById(franjaHorariaId))
                .thenReturn(Optional.of(franjaHoraria));

        when(restriccionDocenteRepository
                .existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                        docenteId,
                        anioEscolarId,
                        DiaSemana.VIERNES,
                        franjaHorariaId))
                .thenReturn(false);

        when(restriccionDocenteRepository
                .save(any(RestriccionDocente.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RestriccionDocenteResponse response =
                restriccionDocenteService
                        .registrarRestriccion(request);

        assertEquals(
                null,
                response.getDocumentoSoporte());
    }
}
