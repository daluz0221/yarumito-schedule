package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaService areaService;

    @Test
    void listarAreas_debeDevolverAreasActivas() {
        UUID areaId = UUID.randomUUID();
        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();
        area.setNombre("Matemáticas");
        area.setCodigo("MAT");

        when(areaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(area));

        List<AreaResponse> response = areaService.listarAreas();

        assertEquals(1, response.size());
        assertEquals(areaId, response.get(0).getId());
        assertEquals("Matemáticas", response.get(0).getNombre());
        assertEquals("MAT", response.get(0).getCodigo());
    }
}
