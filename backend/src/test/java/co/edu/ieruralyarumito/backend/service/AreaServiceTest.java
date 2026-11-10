package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAreaRequest;
import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAreaRequest;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
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

// Pruebas unitarias de la lógica de negocio de AreaService.
@ExtendWith(MockitoExtension.class)
public class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaService areaService;

    // Verifica que no se permita registrar un código de área ya existente.
    @Test
    void registrarArea_debeRechazarCodigoDuplicado() {

        CrearAreaRequest request = new CrearAreaRequest();
        request.setCodigo("MAT");

        when(areaRepository.existsByCodigo("MAT"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> areaService.registrarArea(request)
        );
    }

    // Verifica que consultar un área inexistente informe recurso no encontrado.
    @Test
    void consultarArea_debeRechazarAreaInexistente() {

        UUID areaId = UUID.randomUUID();

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> areaService.consultarArea(areaId)
        );
    }

    // Verifica que un área válida se registre correctamente.
    @Test
    void registrarArea_debeRegistrarCorrectamente() {

        CrearAreaRequest request = new CrearAreaRequest();
        request.setNombre("Matemáticas");
        request.setCodigo("MAT");
        request.setObligatoria(true);
        request.setSoloMedia(false);
        request.setActiva(true);

        when(areaRepository.existsByCodigo("MAT"))
                .thenReturn(false);

        when(areaRepository.save(any(Area.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaResponse response =
                areaService.registrarArea(request);

        assertEquals("Matemáticas", response.getNombre());
        assertEquals("MAT", response.getCodigo());
        assertTrue(response.isObligatoria());
        assertFalse(response.isSoloMedia());
        assertTrue(response.isActiva());
    }

    // Verifica que no se permita actualizar con el código de otra área.
    @Test
    void actualizarArea_debeRechazarCodigoDuplicado() {

        UUID areaId = UUID.randomUUID();

        Area areaExistente = new Area();

        ActualizarAreaRequest request =
                new ActualizarAreaRequest();

        request.setCodigo("SOC");

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(areaExistente));

        when(areaRepository.existsByCodigoAndIdNot(
                "SOC",
                areaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> areaService.actualizarArea(areaId, request)
        );
    }

    // Verifica que un área existente pueda actualizarse correctamente.
    @Test
    void actualizarArea_debeActualizarCorrectamente() {

        UUID areaId = UUID.randomUUID();

        Area areaExistente = new Area();

        ActualizarAreaRequest request =
                new ActualizarAreaRequest();

        request.setNombre("Ciencias Naturales");
        request.setCodigo("CN");
        request.setObligatoria(true);
        request.setSoloMedia(false);
        request.setActiva(true);

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(areaExistente));

        when(areaRepository.existsByCodigoAndIdNot(
                "CN",
                areaId))
                .thenReturn(false);

        when(areaRepository.save(any(Area.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaResponse response =
                areaService.actualizarArea(areaId, request);

        assertEquals("Ciencias Naturales", response.getNombre());
        assertEquals("CN", response.getCodigo());
        assertTrue(response.isObligatoria());
        assertFalse(response.isSoloMedia());
        assertTrue(response.isActiva());
    }

    // Verifica que el estado de un área pueda cambiarse correctamente.
    @Test
    void cambiarEstado_debeDesactivarArea() {

        UUID areaId = UUID.randomUUID();

        Area area = new Area();
        area.setNombre("Matemáticas");
        area.setCodigo("MAT");
        area.setActiva(true);

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(areaRepository.save(any(Area.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaResponse response =
                areaService.cambiarEstado(areaId, false);

        assertFalse(response.isActiva());
    }
}
