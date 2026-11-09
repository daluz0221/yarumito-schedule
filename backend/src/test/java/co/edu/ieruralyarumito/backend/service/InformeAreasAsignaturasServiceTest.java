
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas unitarias del informe de áreas y asignaturas.
 * Utiliza repositorios simulados, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class InformeAreasAsignaturasServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @InjectMocks
    private InformeAreasAsignaturasService informeService;

    private Area matematicas;
    private Area ciencias;

    @BeforeEach
    void preparar() {
        matematicas = crearArea("Matemáticas", true);
        ciencias = crearArea("Ciencias", true);
    }

    @Test
    void debeContarAreasYAsignaturasCorrectamente() {

        Asignatura algebra = crearAsignatura(
                "Álgebra", matematicas);
        Asignatura geometria = crearAsignatura(
                "Geometría", matematicas);
        Asignatura estadistica = crearAsignatura(
                "Estadística", matematicas);

        Asignatura biologia = crearAsignatura(
                "Biología", ciencias);
        Asignatura quimica = crearAsignatura(
                "Química", ciencias);

        when(areaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(matematicas, ciencias));

        when(asignaturaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(
                        algebra, geometria, estadistica,
                        biologia, quimica));

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(2, resultado.cantidadAreas());
        assertEquals(5, resultado.cantidadAsignaturas());

        assertEquals(3,
                resultado.areas().stream()
                        .filter(a -> a.nombre().equals("Matemáticas"))
                        .findFirst().orElseThrow()
                        .cantidadAsignaturas());

        assertEquals(2,
                resultado.areas().stream()
                        .filter(a -> a.nombre().equals("Ciencias"))
                        .findFirst().orElseThrow()
                        .cantidadAsignaturas());
    }

    @Test
    void debeConservarAreasSinAsignaturas() {

        Area filosofia = crearArea("Filosofía", true);

        when(areaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(filosofia));

        when(asignaturaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(1, resultado.cantidadAreas());
        assertEquals(0, resultado.cantidadAsignaturas());
        assertEquals(0,
                resultado.areas().get(0).cantidadAsignaturas());
        assertTrue(resultado.areas().get(0).asignaturas().isEmpty());
    }

    @Test
    void debeEvitarContarAreasYAsignaturasDuplicadas() {

        Asignatura algebra = crearAsignatura(
                "Álgebra", matematicas);

        when(areaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(matematicas, matematicas));

        when(asignaturaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(algebra, algebra));

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(1, resultado.cantidadAreas());
        assertEquals(1, resultado.cantidadAsignaturas());
    }

    @Test
    void debeRetornarCerosSinAreas() {

        when(areaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(0, resultado.cantidadAreas());
        assertEquals(0, resultado.cantidadAsignaturas());
        assertTrue(resultado.areas().isEmpty());

        verifyNoInteractions(asignaturaRepository);
    }

    @Test
    void debeConservarInformacionAcademica() {

        Asignatura algebra = crearAsignatura(
                "Álgebra", matematicas);

        when(algebra.isActiva()).thenReturn(true);
        when(algebra.isEsMediaTecnica()).thenReturn(false);
        when(algebra.isExigeIdoneidadEstricta()).thenReturn(true);
        when(algebra.isRequiereDocenteExclusivo()).thenReturn(false);

        when(areaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(matematicas));

        when(asignaturaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(algebra));

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, true);

        var area = resultado.areas().get(0);
        var asignatura = area.asignaturas().get(0);

        assertEquals("Matemáticas", area.nombre());
        assertTrue(area.activa());
        assertEquals("Álgebra", asignatura.nombre());
        assertTrue(asignatura.activa());
        assertTrue(asignatura.exigeIdoneidadEstricta());
        assertFalse(asignatura.esMediaTecnica());
        assertFalse(asignatura.requiereDocenteExclusivo());
    }

    /**
     * Construye un área ficticia con identificador estable.
     */
    private Area crearArea(String nombre, boolean activa) {

        Area area = mock(Area.class);
        UUID id = UUID.randomUUID();

        lenient().when(area.getId()).thenReturn(id);
        lenient().when(area.getNombre()).thenReturn(nombre);
        lenient().when(area.getCodigo()).thenReturn("AREA-" + id);
        lenient().when(area.isActiva()).thenReturn(activa);

        return area;
    }

    /**
     * Construye una asignatura ficticia asociada a un área.
     */
    private Asignatura crearAsignatura(
            String nombre, Area area) {

        Asignatura asignatura = mock(Asignatura.class);
        UUID id = UUID.randomUUID();

        when(asignatura.getId()).thenReturn(id);
        when(asignatura.getArea()).thenReturn(area);
        when(asignatura.getNombre()).thenReturn(nombre);
        when(asignatura.getCodigo()).thenReturn("ASIG-" + id);

        return asignatura;
    }
}
