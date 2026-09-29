package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGradoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGradoRequest;
import co.edu.ieruralyarumito.backend.dto.GradoResponse;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
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

// Pruebas unitarias de la lógica de negocio de GradoService.
@ExtendWith(MockitoExtension.class)
public class GradoServiceTest {

    @Mock
    private GradoRepository gradoRepository;

    @InjectMocks
    private GradoService gradoService;

    // No permite registrar dos veces el mismo grado escolar.
    @Test
    void registrarGrado_debeRechazarNivelDuplicado() {

        CrearGradoRequest request =
                new CrearGradoRequest();

        request.setNivel(10);

        when(gradoRepository.existsByNivel(10))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> gradoService.registrarGrado(request)
        );
    }

    // Verifica la configuración automática de un grado de básica secundaria.
    @Test
    void registrarGrado_debeConfigurarSextoCorrectamente() {

        CrearGradoRequest request =
                new CrearGradoRequest();

        request.setNivel(6);

        when(gradoRepository.existsByNivel(6))
                .thenReturn(false);

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GradoResponse response =
                gradoService.registrarGrado(request);

        assertEquals(6, response.getNivel());
        assertEquals("SEXTO", response.getNombre());
        assertFalse(response.isEsMedia());
        assertEquals(5, response.getPrioridadAsignacion());
        assertEquals(30, response.getHorasSemanalesEsperadas());
    }

    // RN-12.G02: los grados de educación media tienen máxima
    // prioridad para iniciar posteriormente la asignación académica.
    @Test
    void registrarGrado_debeConfigurarDecimoConMaximaPrioridad() {

        CrearGradoRequest request =
                new CrearGradoRequest();

        request.setNivel(10);

        when(gradoRepository.existsByNivel(10))
                .thenReturn(false);

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GradoResponse response =
                gradoService.registrarGrado(request);

        assertEquals(10, response.getNivel());
        assertEquals("DÉCIMO", response.getNombre());
        assertTrue(response.isEsMedia());
        assertEquals(1, response.getPrioridadAsignacion());
        assertEquals(37, response.getHorasSemanalesEsperadas());
    }

    // RN-12.G02: undécimo también pertenece a educación media
    // y conserva la máxima prioridad de asignación.
    @Test
    void registrarGrado_debeConfigurarUndecimoConMaximaPrioridad() {

        CrearGradoRequest request =
                new CrearGradoRequest();

        request.setNivel(11);

        when(gradoRepository.existsByNivel(11))
                .thenReturn(false);

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GradoResponse response =
                gradoService.registrarGrado(request);

        assertEquals(11, response.getNivel());
        assertEquals("UNDÉCIMO", response.getNombre());
        assertTrue(response.isEsMedia());
        assertEquals(1, response.getPrioridadAsignacion());
        assertEquals(37, response.getHorasSemanalesEsperadas());
    }

    // Verifica que consultar un grado inexistente genere el error correspondiente.
    @Test
    void consultarGrado_debeRechazarGradoInexistente() {

        UUID gradoId = UUID.randomUUID();

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> gradoService.consultarGrado(gradoId)
        );
    }

    // Al cambiar el nivel, todos los datos dependientes
    // deben recalcularse automáticamente.
    @Test
    void actualizarGrado_debeRecalcularDatosSegunNuevoNivel() {

        UUID gradoId = UUID.randomUUID();

        Grado existente = new Grado();
        existente.setNivel(9);
        existente.setNombre("NOVENO");
        existente.setEsMedia(false);
        existente.setPrioridadAsignacion(2);
        existente.setHorasSemanalesEsperadas(30);

        ActualizarGradoRequest request =
                new ActualizarGradoRequest();

        request.setNivel(10);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(existente));

        when(gradoRepository.existsByNivelAndIdNot(
                10,
                gradoId))
                .thenReturn(false);

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GradoResponse response =
                gradoService.actualizarGrado(
                        gradoId,
                        request);

        assertEquals(10, response.getNivel());
        assertEquals("DÉCIMO", response.getNombre());
        assertTrue(response.isEsMedia());
        assertEquals(1, response.getPrioridadAsignacion());
        assertEquals(37, response.getHorasSemanalesEsperadas());
    }

    // No permite actualizar un grado usando un nivel
    // que ya pertenece a otro registro.
    @Test
    void actualizarGrado_debeRechazarNivelDuplicado() {

        UUID gradoId = UUID.randomUUID();

        Grado existente = new Grado();
        existente.setNivel(8);

        ActualizarGradoRequest request =
                new ActualizarGradoRequest();

        request.setNivel(9);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(existente));

        when(gradoRepository.existsByNivelAndIdNot(
                9,
                gradoId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> gradoService.actualizarGrado(
                        gradoId,
                        request)
        );
    }
}
