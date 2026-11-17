
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;

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
 * Pruebas unitarias del informe de planta docente.
 *
 * Utiliza datos ficticios y un repositorio simulado.
 * No modifica la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class InformePlantaDocenteServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @InjectMocks
    private InformePlantaDocenteService informeService;

    private UUID areaMatematicasId;

    @BeforeEach
    void preparar() {
        areaMatematicasId = UUID.randomUUID();
    }

    @Test
    void debeCalcularCantidadYHorasContratadas() {

        Docente carlos = crearDocente(
                "Carlos", "Perez", "Matematicas", 22);

        Docente ana = crearDocente(
                "Ana", "Gomez", "Matematicas", 22);

        Docente luis = crearDocente(
                "Luis", "Ruiz", "Matematicas", 20);

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(carlos, ana, luis));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        areaMatematicasId,
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA
                );

        assertEquals(3, resultado.cantidadDocentes());
        assertEquals(64, resultado.totalHorasSemanalesContratadas());
        assertEquals(3, resultado.docentes().size());

        verify(docenteRepository).findAll(
                any(Specification.class));
    }

    @Test
    void noDebeContabilizarDocentesDuplicados() {

        Docente carlos = crearDocente(
                "Carlos", "Perez", "Matematicas", 22);

        Docente ana = crearDocente(
                "Ana", "Gomez", "Matematicas", 20);

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(carlos, carlos, ana));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null);

        assertEquals(2, resultado.cantidadDocentes());
        assertEquals(42, resultado.totalHorasSemanalesContratadas());
        assertEquals(2, resultado.docentes().size());
    }

    @Test
    void debeRetornarCerosCuandoNoExistenDocentes() {

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of());

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        areaMatematicasId,
                        null,
                        null
                );

        assertEquals(0, resultado.cantidadDocentes());
        assertEquals(0, resultado.totalHorasSemanalesContratadas());
        assertTrue(resultado.docentes().isEmpty());
    }

    @Test
    void debeConservarElDetalleDeCadaDocente() {

        Docente docente = crearDocente(
                "Carlos", "Perez", "Matematicas", 22);

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(docente));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null);

        var detalle = resultado.docentes().get(0);

        assertEquals("Carlos Perez", detalle.nombreCompleto());
        assertEquals("Matematicas", detalle.areaNombramiento());
        assertEquals(EstadoDocente.ACTIVO, detalle.estado());
        assertEquals(TipoVinculacion.PLANTA,
                detalle.tipoVinculacion());
        assertEquals(22, detalle.horasSemanalesContratadas());
    }

    /**
     * Construye un docente ficticio sin persistirlo.
     */
    private Docente crearDocente(
            String nombres,
            String apellidos,
            String nombreArea,
            int horas) {

        Docente docente = mock(Docente.class);
        Area area = mock(Area.class);

        UUID id = UUID.randomUUID();

        when(docente.getId()).thenReturn(id);
        when(docente.getNombres()).thenReturn(nombres);
        when(docente.getApellidos()).thenReturn(apellidos);
        when(docente.getAreaNombramiento()).thenReturn(area);
        when(area.getNombre()).thenReturn(nombreArea);
        when(docente.getHorasSemanalesContratadas())
                .thenReturn(horas);
        when(docente.getEstado())
                .thenReturn(EstadoDocente.ACTIVO);
        when(docente.getTipoVinculacion())
                .thenReturn(TipoVinculacion.PLANTA);

        return docente;
    }
}
