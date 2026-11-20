package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;

import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;

import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// H-04: pruebas de baja lógica, reactivación
// y protección de registros históricos.
@ExtendWith(MockitoExtension.class)
class H04BajaLogicaTest {

    @Mock
    private PlanEstudiosRepository planEstudiosRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private AsignacionAcademicaRepository asignacionAcademicaRepository;

    @Mock
    private TituloProfesionalRepository tituloProfesionalRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private RestriccionDocenteRepository restriccionDocenteRepository;

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private AreaRepository areaRepository;

    private PlanEstudiosService planEstudiosService;
    private TituloProfesionalService tituloProfesionalService;
    private RestriccionDocenteService restriccionDocenteService;
    private IdoneidadService idoneidadService;

    @BeforeEach
    void configurarServicios() {

        planEstudiosService = new PlanEstudiosService(
                planEstudiosRepository,
                anioEscolarRepository,
                gradoRepository,
                asignaturaRepository,
                turnoRepository,
                asignacionAcademicaRepository
        );

        // H-04: el servicio ahora recibe IdoneidadRepository
        // para comprobar dependencias antes de retirar títulos.
        tituloProfesionalService = new TituloProfesionalService(
                tituloProfesionalRepository,
                docenteRepository,
                idoneidadRepository
        );

        restriccionDocenteService = new RestriccionDocenteService(
                restriccionDocenteRepository,
                docenteRepository,
                anioEscolarRepository,
                franjaHorariaRepository
        );

        idoneidadService = new IdoneidadService(
                idoneidadRepository,
                docenteRepository,
                areaRepository,
                asignaturaRepository,
                tituloProfesionalRepository
        );
    }

    // Construye un registro de plan de estudios
    // para las pruebas de retiro y reactivación.
    private PlanEstudios crearPlan() {

        AnioEscolar anioEscolar = mock(AnioEscolar.class);
        Grado grado = mock(Grado.class);
        Asignatura asignatura = mock(Asignatura.class);

        when(anioEscolar.getId())
                .thenReturn(UUID.randomUUID());

        when(grado.getId())
                .thenReturn(UUID.randomUUID());

        when(asignatura.getId())
                .thenReturn(UUID.randomUUID());

        PlanEstudios plan = new PlanEstudios();

        plan.setAnioEscolar(anioEscolar);
        plan.setGrado(grado);
        plan.setAsignatura(asignatura);

        return plan;
    }

    // Construye un título profesional asociado
    // a un docente para las pruebas.
    private TituloProfesional crearTitulo() {

        Docente docente = mock(Docente.class);

        TituloProfesional titulo = new TituloProfesional();

        titulo.setDocente(docente);

        return titulo;
    }

    // Construye una restricción docente de prueba.
    private RestriccionDocente crearRestriccion() {

        Docente docente = mock(Docente.class);
        AnioEscolar anioEscolar = mock(AnioEscolar.class);
        FranjaHoraria franjaHoraria = mock(FranjaHoraria.class);

        when(docente.getId())
                .thenReturn(UUID.randomUUID());

        when(anioEscolar.getId())
                .thenReturn(UUID.randomUUID());

        when(franjaHoraria.getId())
                .thenReturn(UUID.randomUUID());

        RestriccionDocente restriccion =
                new RestriccionDocente();

        restriccion.setDocente(docente);
        restriccion.setAnioEscolar(anioEscolar);
        restriccion.setFranjaHoraria(franjaHoraria);
        restriccion.setDia(DiaSemana.LUNES);

        return restriccion;
    }

    // H-04: permite retirar un plan sin asignaciones.
    @Test
    void retirarPlanDebeConservarRegistro() {

        UUID id = UUID.randomUUID();

        PlanEstudios plan = crearPlan();

        when(planEstudiosRepository.findById(id))
                .thenReturn(Optional.of(plan));

        when(asignacionAcademicaRepository
                .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                        plan.getAnioEscolar().getId(),
                        plan.getGrado().getId(),
                        plan.getAsignatura().getId()))
                .thenReturn(List.of());

        when(planEstudiosRepository.save(plan))
                .thenReturn(plan);

        var response =
                planEstudiosService.retirarPlanEstudios(id);

        assertFalse(response.isActivo());

        verify(planEstudiosRepository).save(plan);

        verify(planEstudiosRepository, never())
                .delete(any());
    }

    // H-04: rechaza el retiro de un plan
    // que tiene asignaciones académicas relacionadas.
    @Test
    void retirarPlanConAsignacionesDebeRechazarse() {

        UUID id = UUID.randomUUID();

        PlanEstudios plan = crearPlan();

        when(planEstudiosRepository.findById(id))
                .thenReturn(Optional.of(plan));

        when(asignacionAcademicaRepository
                .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                        plan.getAnioEscolar().getId(),
                        plan.getGrado().getId(),
                        plan.getAsignatura().getId()))
                .thenReturn(
                        List.of(mock(AsignacionAcademica.class)));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> planEstudiosService.retirarPlanEstudios(id)
        );

        assertTrue(plan.isActivo());

        verify(planEstudiosRepository, never())
                .save(any());
    }

    // H-04: permite reactivar un plan retirado.
    @Test
    void reactivarPlanDebeCambiarEstado() {

        UUID id = UUID.randomUUID();

        PlanEstudios plan = crearPlan();

        plan.setActivo(false);

        when(planEstudiosRepository.findById(id))
                .thenReturn(Optional.of(plan));

        when(planEstudiosRepository.save(plan))
                .thenReturn(plan);

        var response =
                planEstudiosService.reactivarPlanEstudios(id);

        assertTrue(response.isActivo());

        verify(planEstudiosRepository).save(plan);
    }

    // H-04: permite retirar un título cuando
    // no existen idoneidades con vigencia pendiente.
    @Test
    void retirarTituloDebeConservarRegistro() {

        UUID id = UUID.randomUUID();

        TituloProfesional titulo = crearTitulo();

        when(tituloProfesionalRepository.findById(id))
                .thenReturn(Optional.of(titulo));

        // Simula que las idoneidades relacionadas
        // ya finalizaron o que no existen.
        when(idoneidadRepository
                .existeIdoneidadVigentePorTitulo(
                        eq(id),
                        any(LocalDate.class)))
                .thenReturn(false);

        when(tituloProfesionalRepository.save(titulo))
                .thenReturn(titulo);

        var response =
                tituloProfesionalService.retirarTituloProfesional(id);

        assertFalse(response.isActivo());

        verify(idoneidadRepository)
                .existeIdoneidadVigentePorTitulo(
                        eq(id),
                        any(LocalDate.class));

        verify(tituloProfesionalRepository).save(titulo);

        verify(tituloProfesionalRepository, never())
                .delete(any());
    }

    // H-04: rechaza el retiro de un título
    // mientras respalde idoneidades vigentes.
    @Test
    void retirarTituloConIdoneidadesVigentesDebeRechazarse() {

        UUID id = UUID.randomUUID();

        TituloProfesional titulo = crearTitulo();

        when(tituloProfesionalRepository.findById(id))
                .thenReturn(Optional.of(titulo));

        // Simula que existe al menos una idoneidad
        // con vigencia actual o pendiente.
        when(idoneidadRepository
                .existeIdoneidadVigentePorTitulo(
                        eq(id),
                        any(LocalDate.class)))
                .thenReturn(true);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> tituloProfesionalService
                        .retirarTituloProfesional(id)
        );

        // El título debe seguir activo.
        assertTrue(titulo.isActivo());

        // No debe persistirse ningún retiro.
        verify(tituloProfesionalRepository, never())
                .save(any());
    }

    // H-04: reactiva un título anteriormente retirado.
    @Test
    void reactivarTituloDebeCambiarEstado() {

        UUID id = UUID.randomUUID();

        TituloProfesional titulo = crearTitulo();

        titulo.setActivo(false);

        when(tituloProfesionalRepository.findById(id))
                .thenReturn(Optional.of(titulo));

        when(tituloProfesionalRepository.save(titulo))
                .thenReturn(titulo);

        var response =
                tituloProfesionalService.reactivarTituloProfesional(id);

        assertTrue(response.isActivo());

        verify(tituloProfesionalRepository).save(titulo);
    }

    // H-04: retira una restricción sin
    // eliminar su registro histórico.
    @Test
    void retirarRestriccionDebeConservarRegistro() {

        UUID id = UUID.randomUUID();

        RestriccionDocente restriccion =
                crearRestriccion();

        when(restriccionDocenteRepository.findById(id))
                .thenReturn(Optional.of(restriccion));

        when(restriccionDocenteRepository.save(restriccion))
                .thenReturn(restriccion);

        var response =
                restriccionDocenteService.retirarRestriccion(id);

        assertFalse(response.isActivo());

        verify(restriccionDocenteRepository)
                .save(restriccion);

        verify(restriccionDocenteRepository, never())
                .delete(any());
    }

    // H-04: reactiva una restricción válida.
    @Test
    void reactivarRestriccionDebeCambiarEstado() {

        UUID id = UUID.randomUUID();

        RestriccionDocente restriccion =
                crearRestriccion();

        restriccion.setActivo(false);

        when(restriccion.getDocente().getEstado())
                .thenReturn(EstadoDocente.ACTIVO);

        when(restriccionDocenteRepository.findById(id))
                .thenReturn(Optional.of(restriccion));

        when(restriccionDocenteRepository
                .findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                        restriccion.getDocente().getId(),
                        restriccion.getAnioEscolar().getId(),
                        restriccion.getDia(),
                        restriccion.getFranjaHoraria().getId()))
                .thenReturn(Optional.empty());

        when(restriccionDocenteRepository.save(restriccion))
                .thenReturn(restriccion);

        var response =
                restriccionDocenteService.reactivarRestriccion(id);

        assertTrue(response.isActivo());

        verify(restriccionDocenteRepository)
                .save(restriccion);
    }

    // H-04: rechaza utilizar un título retirado
    // como soporte de una nueva idoneidad.
    @Test
    void idoneidadDebeRechazarTituloRetirado() {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID();

        Docente docente = mock(Docente.class);
        Area area = mock(Area.class);

        when(docente.getId())
                .thenReturn(docenteId);

        TituloProfesional titulo =
                new TituloProfesional();

        titulo.setDocente(docente);
        titulo.setActivo(false);

        CrearIdoneidadRequest request =
                new CrearIdoneidadRequest();

        request.setDocenteId(docenteId);
        request.setAreaId(areaId);
        request.setTipo(TipoIdoneidad.EXCEPCIONAL);
        request.setTituloSoporteId(tituloId);
        request.setVigenteDesde(LocalDate.now());

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(titulo));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> idoneidadService.registrarIdoneidad(request)
        );

        verify(idoneidadRepository, never())
                .save(any());
    }
}
