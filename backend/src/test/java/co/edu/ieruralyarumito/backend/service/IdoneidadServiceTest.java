package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.ieruralyarumito.backend.dto.FinalizarVigenciaIdoneidadRequest;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.dto.ActualizarIdoneidadRequest;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import static org.mockito.Mockito.lenient;


// Pruebas unitarias de la lógica de negocio de IdoneidadService.
@ExtendWith(MockitoExtension.class)
public class IdoneidadServiceTest {

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private TituloProfesionalRepository tituloProfesionalRepository;

    @InjectMocks
    private IdoneidadService idoneidadService;

    // Verifica que no se permita finalizar una idoneidad con una fecha anterior al inicio.
    @Test
    void finalizarVigencia_debeRechazarFechaAnteriorAlInicio() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad existente con fecha de inicio definida.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 18));

        // Prepara una fecha de finalización anterior al inicio.
        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 17));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Verifica que el servicio rechace la fecha inválida.
        assertThrows(
                FechaVigenciaInvalidaException.class,
                () -> idoneidadService.finalizarVigencia(idoneidadId, request)
        );
    }

    // Verifica que una fecha válida finalice correctamente la vigencia.
    @Test
    void finalizarVigencia_debeFinalizarCorrectamente() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad existente.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setDocente(new Docente());
        idoneidad.setArea(new Area());
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 18));

        // Prepara una fecha válida de finalización.
        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 20));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Devuelve la misma entidad que el servicio guarda.
        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta la finalización.
        IdoneidadResponse response =
                idoneidadService.finalizarVigencia(idoneidadId, request);

        // Verifica que la fecha haya quedado registrada.
        assertEquals(
                LocalDate.of(2026, 9, 20),
                response.getVigenteHasta()
        );
    }

    // Verifica que no se pueda finalizar nuevamente una idoneidad ya cerrada.
    @Test
    void finalizarVigencia_debeRechazarIdoneidadYaFinalizada() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad que ya tiene fecha de finalización.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 10));
        idoneidad.setVigenteHasta(LocalDate.of(2026, 9, 15));

        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 20));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Verifica que el servicio rechace una segunda finalización.
        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> idoneidadService.finalizarVigencia(idoneidadId, request)
        );
    }

    // Verifica que no se pueda registrar una idoneidad con una asignatura de otra área.
    @Test
    void registrarIdoneidad_debeRechazarAsignaturaDeOtraArea() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID otraAreaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        Docente docente = new Docente();

        // Simula dos áreas diferentes con identificadores distintos.
        Area area = spy(new Area());
        Area otraArea = spy(new Area());

        doReturn(areaId).when(area).getId();
        doReturn(otraAreaId).when(otraArea).getId();

        // La asignatura pertenece a otra área.
        Asignatura asignatura = new Asignatura();
        asignatura.setArea(otraArea);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setAsignaturaId(asignaturaId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

        // Verifica que no se pueda registrar una idoneidad con un título de otro docente.
    @Test
    void registrarIdoneidad_debeRechazarTituloDeOtroDocente() {

        UUID docenteId = UUID.randomUUID();
        UUID otroDocenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID();

        // Simula dos docentes diferentes.
        Docente docente = spy(new Docente());
        Docente otroDocente = spy(new Docente());

        doReturn(docenteId).when(docente).getId();
        doReturn(otroDocenteId).when(otroDocente).getId();

        // El título pertenece a otro docente.
        TituloProfesional tituloProfesional = new TituloProfesional();
        tituloProfesional.setDocente(otroDocente);

        Area area = new Area();

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTituloSoporteId(tituloId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(tituloProfesional));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que no se pueda actualizar una idoneidad
// con un título profesional perteneciente a otro docente.
    @Test
    void actualizarIdoneidad_debeRechazarTituloDeOtroDocente() {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID otroDocenteId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Docente otroDocente = spy(new Docente());

        doReturn(docenteId).when(docente).getId();
        doReturn(otroDocenteId).when(otroDocente).getId();

        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setDocente(docente);

        TituloProfesional tituloProfesional = new TituloProfesional();
        tituloProfesional.setDocente(otroDocente);

        ActualizarIdoneidadRequest request =
                new ActualizarIdoneidadRequest();

        request.setTituloSoporteId(tituloId);

        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(tituloProfesional));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.actualizarIdoneidad(
                        idoneidadId,
                        request
                )
        );
    }

    // Verifica que se listen las idoneidades asociadas a un docente existente.
    @Test
    void listarIdoneidadesPorDocente_debeRetornarIdoneidadesDelDocente() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Area area = spy(new Area());

        doReturn(docenteId).when(docente).getId();
        doReturn(areaId).when(area).getId();

        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setDocente(docente);
        idoneidad.setArea(area);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(idoneidadRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(idoneidad));

        List<IdoneidadResponse> response =
                idoneidadService.listarIdoneidadesPorDocente(docenteId);

        assertEquals(1, response.size());
        assertEquals(docenteId, response.get(0).getDocenteId());
        assertEquals(areaId, response.get(0).getAreaId());
    }

    // Verifica que no se pueda registrar una idoneidad con fecha final anterior al inicio.
    @Test
    void registrarIdoneidad_debeRechazarRangoDeVigenciaInvalido() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setVigenteDesde(LocalDate.of(2026, 9, 20));
        request.setVigenteHasta(LocalDate.of(2026, 9, 19));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(new Docente()));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(new Area()));

        assertThrows(
                FechaVigenciaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que consultar una idoneidad inexistente informe recurso no encontrado.
    @Test
    void consultarIdoneidad_debeRechazarIdInexistente() {

        UUID idoneidadId = UUID.randomUUID();

        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> idoneidadService.consultarIdoneidad(idoneidadId)
        );
    }

    // Verifica que no se pueda registrar una idoneidad con un área inexistente.
    @Test
    void registrarIdoneidad_debeRechazarAreaInexistente() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(new Docente()));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que no se pueda registrar una idoneidad con una asignatura inexistente.
    @Test
    void registrarIdoneidad_debeRechazarAsignaturaInexistente() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setAsignaturaId(asignaturaId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(new Docente()));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(new Area()));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que no se pueda registrar una idoneidad con un docente inexistente.
    @Test
    void registrarIdoneidad_debeRechazarDocenteInexistente() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que no se pueda actualizar una idoneidad
// con un título profesional inexistente.
    @Test
    void actualizarIdoneidad_debeRechazarTituloInexistente() {

        UUID idoneidadId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID();

        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setDocente(new Docente());

        ActualizarIdoneidadRequest request =
                new ActualizarIdoneidadRequest();

        request.setTituloSoporteId(tituloId);

        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> idoneidadService.actualizarIdoneidad(
                        idoneidadId,
                        request
                )
        );
    }

    // Verifica que una idoneidad PRINCIPAL corresponda al área de nombramiento del docente.
    @Test
    void registrarIdoneidadPrincipal_debeRechazarAreaDistintaAlNombramiento() {

        UUID docenteId = UUID.randomUUID();
        UUID areaNombramientoId = UUID.randomUUID();
        UUID otraAreaId = UUID.randomUUID();

        Area areaNombramiento = spy(new Area());
        Area otraArea = spy(new Area());

        doReturn(areaNombramientoId).when(areaNombramiento).getId();
        doReturn(otraAreaId).when(otraArea).getId();

        Docente docente = new Docente();
        docente.setAreaNombramiento(areaNombramiento);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(otraAreaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.PRINCIPAL
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(otraAreaId))
                .thenReturn(Optional.of(otraArea));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que una asignatura de Media Técnica que exige docente exclusivo
// no pueda asociarse a un docente que no sea exclusivo de Media Técnica.
    @Test
    void registrarIdoneidadMediaTecnica_debeRechazarDocenteNoExclusivo() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        Docente docente = new Docente();
        docente.setEsExclusivoMediaTecnica(false);

        Asignatura asignatura = new Asignatura();
        asignatura.setArea(area);
        asignatura.setEsMediaTecnica(true);
        asignatura.setRequiereDocenteExclusivo(true);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setAsignaturaId(asignaturaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        lenient()
                .when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que una asignatura de Media Técnica pueda asociarse
// cuando el docente sí es exclusivo de Media Técnica.
    @Test
    void registrarIdoneidadMediaTecnica_debePermitirDocenteExclusivo() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();
        docente.setEsExclusivoMediaTecnica(true);

        Asignatura asignatura = spy(new Asignatura());
        doReturn(asignaturaId).when(asignatura).getId();
        asignatura.setArea(area);
        asignatura.setEsMediaTecnica(true);
        asignatura.setRequiereDocenteExclusivo(true);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setAsignaturaId(asignaturaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        assertEquals(docenteId, response.getDocenteId());
        assertEquals(areaId, response.getAreaId());
        assertEquals(asignaturaId, response.getAsignaturaId());
    }


    // Verifica que una idoneidad PRINCIPAL permita una asignatura
// perteneciente al área de nombramiento del docente.
    @Test
    void registrarIdoneidadPrincipal_debePermitirAsignaturaDelAreaNombramiento() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();
        docente.setAreaNombramiento(area);

        Asignatura asignatura = spy(new Asignatura());
        doReturn(asignaturaId).when(asignatura).getId();
        asignatura.setArea(area);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setAsignaturaId(asignaturaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.PRINCIPAL
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        assertEquals(docenteId, response.getDocenteId());
        assertEquals(areaId, response.getAreaId());
        assertEquals(asignaturaId, response.getAsignaturaId());
        assertEquals(request.getTipo(), response.getTipo());
    }


    // Verifica que una idoneidad AUTORIZADA pueda corresponder
// a un área diferente al área de nombramiento del docente.
    @Test
    void registrarIdoneidadAutorizada_debePermitirAreaDistintaAlNombramiento() {

        UUID docenteId = UUID.randomUUID();
        UUID areaAutorizadaId = UUID.randomUUID();

        Area areaNombramiento = spy(new Area());
        Area areaAutorizada = spy(new Area());

        doReturn(areaAutorizadaId).when(areaAutorizada).getId();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();
        docente.setAreaNombramiento(areaNombramiento);

        CrearIdoneidadRequest request = new CrearIdoneidadRequest();
        request.setDocenteId(docenteId);
        request.setAreaId(areaAutorizadaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaAutorizadaId))
                .thenReturn(Optional.of(areaAutorizada));

        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        assertEquals(docenteId, response.getDocenteId());
        assertEquals(areaAutorizadaId, response.getAreaId());
        assertEquals(request.getTipo(), response.getTipo());
    }

    // Verifica que una idoneidad EXCEPCIONAL
// no pueda registrarse sin justificación.
    @Test
    void registrarIdoneidadExcepcional_debeRechazarSinJustificacion() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = new Docente();
        Area area = new Area();

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL
        );
        request.setVigenteDesde(LocalDate.of(2026, 9, 19));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que una idoneidad EXCEPCIONAL
// pueda registrarse cuando tiene justificación.
    @Test
    void registrarIdoneidadExcepcional_debePermitirConJustificacion() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Area area = spy(new Area());

        doReturn(docenteId).when(docente).getId();
        doReturn(areaId).when(area).getId();

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL
        );
        request.setJustificacion(
                "Necesidad institucional por ausencia del docente titular"
        );
        request.setVigenteDesde(LocalDate.of(2026, 9, 19));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        assertEquals(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL,
                response.getTipo()
        );

        assertEquals(
                "Necesidad institucional por ausencia del docente titular",
                response.getJustificacion()
        );
    }

    // Verifica que una idoneidad EXCEPCIONAL existente
// no pueda perder su justificación mediante una actualización.
    @Test
    void actualizarIdoneidadExcepcional_debeRechazarJustificacionVacia() {

        UUID idoneidadId = UUID.randomUUID();

        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL
        );
        idoneidad.setJustificacion(
                "Necesidad institucional"
        );

        ActualizarIdoneidadRequest request =
                new ActualizarIdoneidadRequest();

        request.setJustificacion("   ");

        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.actualizarIdoneidad(
                        idoneidadId,
                        request
                )
        );
    }

    // Verifica que no se pueda registrar otra idoneidad activa
// para el mismo docente, área y asignatura.
    @Test
    void registrarIdoneidad_debeRechazarDuplicadoActivo() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Area area = spy(new Area());

        doReturn(docenteId).when(docente).getId();
        doReturn(areaId).when(area).getId();

        Idoneidad existente = new Idoneidad();
        existente.setDocente(docente);
        existente.setArea(area);
        existente.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );
        existente.setVigenteDesde(LocalDate.of(2026, 1, 1));
        existente.setVigenteHasta(null);

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );
        request.setVigenteDesde(LocalDate.of(2026, 9, 19));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(idoneidadRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(existente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }


    // Verifica que no se permitan periodos de vigencia superpuestos
// para el mismo docente, área y asignatura.
    @Test
    void registrarIdoneidad_debeRechazarVigenciasSuperpuestas() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Area area = spy(new Area());

        doReturn(docenteId).when(docente).getId();
        doReturn(areaId).when(area).getId();

        Idoneidad existente = new Idoneidad();
        existente.setDocente(docente);
        existente.setArea(area);
        existente.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL
        );
        existente.setJustificacion("Necesidad institucional");
        existente.setVigenteDesde(LocalDate.of(2026, 1, 1));
        existente.setVigenteHasta(LocalDate.of(2026, 12, 31));

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );
        request.setVigenteDesde(LocalDate.of(2026, 6, 1));
        request.setVigenteHasta(LocalDate.of(2026, 9, 30));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(idoneidadRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(existente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );
    }

    // Verifica que una nueva idoneidad pueda iniciar
// después de finalizar la vigencia anterior.
    @Test
    void registrarIdoneidad_debePermitirVigenciaPosteriorSinSolapamiento() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        Area area = spy(new Area());

        doReturn(docenteId).when(docente).getId();
        doReturn(areaId).when(area).getId();

        Idoneidad existente = new Idoneidad();
        existente.setDocente(docente);
        existente.setArea(area);
        existente.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.EXCEPCIONAL
        );
        existente.setJustificacion("Necesidad institucional");
        existente.setVigenteDesde(LocalDate.of(2026, 1, 1));
        existente.setVigenteHasta(LocalDate.of(2026, 6, 30));

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA
        );
        request.setVigenteDesde(LocalDate.of(2026, 7, 1));

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(idoneidadRepository.findByDocente_Id(docenteId))
                .thenReturn(List.of(existente));

        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IdoneidadResponse response =
                idoneidadService.registrarIdoneidad(request);

        assertEquals(
                LocalDate.of(2026, 7, 1),
                response.getVigenteDesde()
        );

        assertEquals(
                co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad.AUTORIZADA,
                response.getTipo()
        );
    }
}
