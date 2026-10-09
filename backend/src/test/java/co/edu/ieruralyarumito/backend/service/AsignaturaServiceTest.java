package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
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
public class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @InjectMocks
    private AsignaturaService asignaturaService;

    @Test
    void listarPorArea_debeDevolverAsignaturasDelArea() {
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();
        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();
        area.setNombre("Matemáticas");

        Asignatura asignatura = spy(new Asignatura());
        doReturn(asignaturaId).when(asignatura).getId();
        asignatura.setArea(area);
        asignatura.setNombre("Álgebra");
        asignatura.setCodigo("ALG");
        asignatura.setAbreviatura("ALG");
        asignatura.setExigeIdoneidadEstricta(true);
        asignatura.setEsMediaTecnica(false);
        asignatura.setRequiereDocenteExclusivo(false);
        asignatura.setTipoAulaRequerida(TipoAulaRequerida.AULA);
        asignatura.setMaxClasesConsecutivas(2);
        asignatura.setActiva(true);

        when(asignaturaRepository.findByAreaIdOrderByNombreAsc(areaId))
                .thenReturn(List.of(asignatura));

        List<AsignaturaResponse> response = asignaturaService.listarPorArea(areaId);

        assertEquals(1, response.size());
        assertEquals(asignaturaId, response.get(0).getId());
        assertEquals(areaId, response.get(0).getAreaId());
        assertEquals("Álgebra", response.get(0).getNombre());
        assertEquals("ALG", response.get(0).getCodigo());
        assertEquals(true, response.get(0).isExigeIdoneidadEstricta());
        assertEquals(false, response.get(0).isEsMediaTecnica());
        assertEquals(2, response.get(0).getMaxClasesConsecutivas());
        assertEquals("AULA", response.get(0).getTipoAulaRequerida());
        assertEquals(true, response.get(0).isActiva());
    }
}
