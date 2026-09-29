package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.CrearPlanEstudiosRequest;
import co.edu.ieruralyarumito.backend.dto.PlanEstudiosResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de PlanEstudiosService.
@ExtendWith(MockitoExtension.class)
public class PlanEstudiosServiceTest {

    @Mock
    private PlanEstudiosRepository planEstudiosRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @InjectMocks
    private PlanEstudiosService planEstudiosService;

    // No permite registrar dos veces la misma asignatura
    // para el mismo grado y año escolar.
    @Test
    void registrarPlanEstudios_debeRechazarRegistroDuplicado() {

        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearPlanEstudiosRequest request =
                new CrearPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> planEstudiosService.registrarPlanEstudios(request)
        );
    }

    // Verifica que pueda registrarse correctamente
    // la intensidad semanal de una asignatura.
    @Test
    void registrarPlanEstudios_debeRegistrarCorrectamente() {

        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Grado grado = new Grado();
        grado.setNivel(10);
        grado.setNombre("DÉCIMO");

        Asignatura asignatura = new Asignatura();
        asignatura.setNombre("MATEMÁTICAS");
        asignatura.setCodigo("MAT");

        CrearPlanEstudiosRequest request =
                new CrearPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);

        // Corresponde a las horas que recibe el grado,
        // no a la carga laboral de 22 horas del docente.
        request.setHorasSemanales(4);

        request.setObservacion(
                "  Intensidad semanal para décimo  ");

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId))
                .thenReturn(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(planEstudiosRepository.save(any(PlanEstudios.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlanEstudiosResponse response =
                planEstudiosService.registrarPlanEstudios(request);

        assertEquals(2027, response.getAnioEscolar());
        assertEquals(10, response.getGradoNivel());
        assertEquals("DÉCIMO", response.getGradoNombre());

        assertEquals(
                "MATEMÁTICAS",
                response.getAsignaturaNombre());

        assertEquals(
                "MAT",
                response.getAsignaturaCodigo());

        assertEquals(
                4,
                response.getHorasSemanales());

        assertEquals(
                "Intensidad semanal para décimo",
                response.getObservacion());

        assertNull(response.getTurnoId());
    }

    // No permite registrar un plan asociado
    // a un año escolar inexistente.
    @Test
    void registrarPlanEstudios_debeRechazarAnioEscolarInexistente() {

        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearPlanEstudiosRequest request =
                new CrearPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId))
                .thenReturn(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> planEstudiosService.registrarPlanEstudios(request)
        );
    }

    // No permite registrar un plan asociado
    // a un grado inexistente.
    @Test
    void registrarPlanEstudios_debeRechazarGradoInexistente() {

        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearPlanEstudiosRequest request =
                new CrearPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId))
                .thenReturn(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(new AnioEscolar()));

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> planEstudiosService.registrarPlanEstudios(request)
        );
    }

    // No permite registrar un plan asociado
    // a una asignatura inexistente.
    @Test
    void registrarPlanEstudios_debeRechazarAsignaturaInexistente() {

        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        CrearPlanEstudiosRequest request =
                new CrearPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_Id(
                        anioEscolarId,
                        gradoId,
                        asignaturaId))
                .thenReturn(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(new AnioEscolar()));

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(new Grado()));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> planEstudiosService.registrarPlanEstudios(request)
        );
    }

    // Consultar un registro inexistente debe generar
    // el error correspondiente.
    @Test
    void consultarPlanEstudios_debeRechazarRegistroInexistente() {

        UUID planId = UUID.randomUUID();

        when(planEstudiosRepository.findById(planId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> planEstudiosService.consultarPlanEstudios(planId)
        );
    }

    // No permite actualizar un registro generando una combinación
    // año + grado + asignatura que ya pertenece a otro registro.
    @Test
    void actualizarPlanEstudios_debeRechazarRegistroDuplicado() {

        UUID planId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        ActualizarPlanEstudiosRequest request =
                new ActualizarPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);

        when(planEstudiosRepository.findById(planId))
                .thenReturn(Optional.of(new PlanEstudios()));

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_IdAndIdNot(
                        anioEscolarId,
                        gradoId,
                        asignaturaId,
                        planId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> planEstudiosService.actualizarPlanEstudios(
                        planId,
                        request)
        );
    }

    // Permite modificar la intensidad semanal de una asignatura.
    @Test
    void actualizarPlanEstudios_debeModificarHorasSemanales() {

        UUID planId = UUID.randomUUID();
        UUID anioEscolarId = UUID.randomUUID();
        UUID gradoId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();

        AnioEscolar anioEscolar = new AnioEscolar();
        anioEscolar.setAnio(2027);

        Grado grado = new Grado();
        grado.setNivel(11);
        grado.setNombre("UNDÉCIMO");

        Asignatura asignatura = new Asignatura();
        asignatura.setNombre("MATEMÁTICAS");
        asignatura.setCodigo("MAT");

        PlanEstudios existente = new PlanEstudios();
        existente.setAnioEscolar(anioEscolar);
        existente.setGrado(grado);
        existente.setAsignatura(asignatura);
        existente.setHorasSemanales(5);

        ActualizarPlanEstudiosRequest request =
                new ActualizarPlanEstudiosRequest();

        request.setAnioEscolarId(anioEscolarId);
        request.setGradoId(gradoId);
        request.setAsignaturaId(asignaturaId);
        request.setHorasSemanales(4);
        request.setObservacion(
                "Intensidad semanal actualizada");

        when(planEstudiosRepository.findById(planId))
                .thenReturn(Optional.of(existente));

        when(planEstudiosRepository
                .existsByAnioEscolar_IdAndGrado_IdAndAsignatura_IdAndIdNot(
                        anioEscolarId,
                        gradoId,
                        asignaturaId,
                        planId))
                .thenReturn(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(anioEscolar));

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(planEstudiosRepository.save(any(PlanEstudios.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlanEstudiosResponse response =
                planEstudiosService.actualizarPlanEstudios(
                        planId,
                        request);

        assertEquals(4, response.getHorasSemanales());

        assertEquals(
                "Intensidad semanal actualizada",
                response.getObservacion());

        assertEquals(11, response.getGradoNivel());

        assertEquals(
                "MATEMÁTICAS",
                response.getAsignaturaNombre());
    }
}
