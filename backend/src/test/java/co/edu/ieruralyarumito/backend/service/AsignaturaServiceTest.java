package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignaturaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignaturaRequest;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de AsignaturaService.
@ExtendWith(MockitoExtension.class)
public class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AsignaturaService asignaturaService;

    @Test
    void registrarAsignatura_debeRechazarCodigoDuplicado() {

        CrearAsignaturaRequest request =
                new CrearAsignaturaRequest();

        request.setCodigo("MAT-01");

        when(asignaturaRepository.existsByCodigo("MAT-01"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> asignaturaService.registrarAsignatura(request)
        );
    }

    @Test
    void registrarAsignatura_debeRechazarAreaInexistente() {

        UUID areaId = UUID.randomUUID();

        CrearAsignaturaRequest request =
                new CrearAsignaturaRequest();

        request.setCodigo("MAT-01");
        request.setAreaId(areaId);

        when(asignaturaRepository.existsByCodigo("MAT-01"))
                .thenReturn(false);

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> asignaturaService.registrarAsignatura(request)
        );
    }

    @Test
    void consultarAsignatura_debeRechazarAsignaturaInexistente() {

        UUID asignaturaId = UUID.randomUUID();

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> asignaturaService.consultarAsignatura(asignaturaId)
        );
    }

    @Test
    void registrarAsignatura_debeRegistrarCorrectamente() {

        UUID areaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        CrearAsignaturaRequest request =
                new CrearAsignaturaRequest();

        request.setAreaId(areaId);
        request.setNombre("Matemáticas");
        request.setCodigo("MAT-01");
        request.setAbreviatura("MAT");
        request.setColorUi("#336699");
        request.setExigeIdoneidadEstricta(true);
        request.setEsMediaTecnica(false);
        request.setRequiereDocenteExclusivo(false);
        request.setTipoAulaRequerida(TipoAulaRequerida.AULA);
        request.setMaxClasesConsecutivas(2);
        request.setActiva(true);

        when(asignaturaRepository.existsByCodigo("MAT-01"))
                .thenReturn(false);

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.save(any(Asignatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AsignaturaResponse response =
                asignaturaService.registrarAsignatura(request);

        assertEquals(areaId, response.getAreaId());
        assertEquals("Matemáticas", response.getNombre());
        assertEquals("MAT-01", response.getCodigo());
        assertEquals("MAT", response.getAbreviatura());
        assertEquals("#336699", response.getColorUi());
        assertTrue(response.isExigeIdoneidadEstricta());
        assertFalse(response.isEsMediaTecnica());
        assertFalse(response.isRequiereDocenteExclusivo());
        assertEquals(
                TipoAulaRequerida.AULA,
                response.getTipoAulaRequerida()
        );
        assertEquals(2, response.getMaxClasesConsecutivas());
        assertTrue(response.isActiva());
    }

    @Test
    void actualizarAsignatura_debeRechazarCodigoDuplicado() {

        UUID asignaturaId = UUID.randomUUID();

        Asignatura asignaturaExistente =
                new Asignatura();

        ActualizarAsignaturaRequest request =
                new ActualizarAsignaturaRequest();

        request.setCodigo("SOC-01");

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignaturaExistente));

        when(asignaturaRepository.existsByCodigoAndIdNot(
                "SOC-01",
                asignaturaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> asignaturaService.actualizarAsignatura(
                        asignaturaId,
                        request
                )
        );
    }

    @Test
    void actualizarAsignatura_debeActualizarCorrectamente() {

        UUID asignaturaId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        Asignatura asignaturaExistente =
                new Asignatura();

        ActualizarAsignaturaRequest request =
                new ActualizarAsignaturaRequest();

        request.setAreaId(areaId);
        request.setNombre("Ciencias Naturales");
        request.setCodigo("CN-01");
        request.setAbreviatura("CN");
        request.setColorUi("#228833");
        request.setExigeIdoneidadEstricta(true);
        request.setEsMediaTecnica(false);
        request.setRequiereDocenteExclusivo(false);
        request.setTipoAulaRequerida(
                TipoAulaRequerida.LABORATORIO
        );
        request.setMaxClasesConsecutivas(2);
        request.setActiva(true);

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignaturaExistente));

        when(asignaturaRepository.existsByCodigoAndIdNot(
                "CN-01",
                asignaturaId))
                .thenReturn(false);

        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        when(asignaturaRepository.save(any(Asignatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AsignaturaResponse response =
                asignaturaService.actualizarAsignatura(
                        asignaturaId,
                        request
                );

        assertEquals(areaId, response.getAreaId());
        assertEquals("Ciencias Naturales", response.getNombre());
        assertEquals("CN-01", response.getCodigo());
        assertEquals(
                TipoAulaRequerida.LABORATORIO,
                response.getTipoAulaRequerida()
        );
        assertTrue(response.isActiva());
    }

    @Test
    void cambiarEstado_debeDesactivarAsignatura() {

        UUID asignaturaId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        Area area = spy(new Area());
        doReturn(areaId).when(area).getId();

        Asignatura asignatura = new Asignatura();
        asignatura.setArea(area);
        asignatura.setNombre("Matemáticas");
        asignatura.setCodigo("MAT-01");
        asignatura.setActiva(true);

        when(asignaturaRepository.findById(asignaturaId))
                .thenReturn(Optional.of(asignatura));

        when(asignaturaRepository.save(any(Asignatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AsignaturaResponse response =
                asignaturaService.cambiarEstado(
                        asignaturaId,
                        false
                );

        assertFalse(response.isActiva());
    }
}
