package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Docente;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HU-04 - Mejora Informe de Planta Docente.
 *
 * Verifica:
 * - Horas contratadas.
 * - Horas academicas asignadas.
 * - Horas extras.
 * - Separacion por año escolar.
 * - Compatibilidad con el informe original.
 *
 * No utiliza ni modifica la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class InformePlantaDocenteCargaTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AsignacionAcademicaRepository
            asignacionAcademicaRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @InjectMocks
    private InformePlantaDocenteService informeService;

    /**
     * Caso 1:
     *
     * Un docente con 22 horas contratadas
     * recibe exactamente 22 horas asignadas.
     *
     * No deben generarse horas extras.
     */
    @Test
    void debeSumar22HorasSinGenerarExtras() {

        UUID anioId = UUID.randomUUID();

        AnioEscolar anio = crearAnio(anioId, 2026);
        Docente docente = crearDocente(22);

        List<AsignacionAcademica> asignaciones =
                List.of(
                        crearAsignacion(
                                anio, 5,
                                EstadoAsignacionAcademica.CONFIRMADA
                        ),
                        crearAsignacion(
                                anio, 5,
                                EstadoAsignacionAcademica.CONFIRMADA
                        ),
                        crearAsignacion(
                                anio, 5,
                                EstadoAsignacionAcademica.CONFIRMADA
                        ),
                        crearAsignacion(
                                anio, 5,
                                EstadoAsignacionAcademica.CONFIRMADA
                        ),
                        crearAsignacion(
                                anio, 2,
                                EstadoAsignacionAcademica.CONFIRMADA
                        )
                );

        when(anioEscolarRepository.findById(anioId))
                .thenReturn(Optional.of(anio));

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(docente));

        when(asignacionAcademicaRepository
                .findByDocente_Id(docente.getId()))
                .thenReturn(asignaciones);

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null, anioId
                );

        assertEquals(
                1,
                resultado.cantidadDocentes()
        );

        assertEquals(
                22,
                resultado.totalHorasSemanalesContratadas()
        );

        assertEquals(
                22,
                resultado.totalHorasSemanalesAsignadas()
        );

        assertEquals(
                0,
                resultado.totalHorasSemanalesExtras()
        );

        var detalle = resultado.docentes().get(0);

        assertEquals(
                22,
                detalle.horasSemanalesContratadas()
        );

        assertEquals(
                22,
                detalle.horasSemanalesAsignadas()
        );

        assertEquals(
                0,
                detalle.horasSemanalesExtras()
        );

        assertEquals(
                anioId,
                resultado.anioEscolarId()
        );

        assertEquals(
                2026,
                resultado.anioEscolar()
        );
    }

    /**
     * Caso 2:
     *
     * Año 2026:
     * 15 horas confirmadas.
     * 12 horas en borrador.
     *
     * Total: 27 horas.
     * Contratadas: 22.
     * Extras: 5.
     *
     * Las asignaciones de 2025 se excluyen.
     */
    @Test
    void debeCalcularExtrasSinMezclarAniosEscolares() {

        UUID anioActualId = UUID.randomUUID();
        UUID anioAnteriorId = UUID.randomUUID();

        AnioEscolar actual =
                crearAnio(anioActualId, 2026);

        AnioEscolar anterior =
                mock(AnioEscolar.class);

        when(anterior.getId())
                .thenReturn(anioAnteriorId);

        Docente docente = crearDocente(22);

        List<AsignacionAcademica> asignaciones =
                List.of(
                        crearAsignacion(
                                actual, 15,
                                EstadoAsignacionAcademica.CONFIRMADA
                        ),
                        crearAsignacion(
                                actual, 12,
                                EstadoAsignacionAcademica.BORRADOR
                        ),
                        crearAsignacion(
                                anterior, 10,
                                EstadoAsignacionAcademica.CONFIRMADA
                        )
                );

        when(anioEscolarRepository.findById(anioActualId))
                .thenReturn(Optional.of(actual));

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(docente));

        when(asignacionAcademicaRepository
                .findByDocente_Id(docente.getId()))
                .thenReturn(asignaciones);

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null, anioActualId
                );

        assertEquals(
                22,
                resultado.totalHorasSemanalesContratadas()
        );

        assertEquals(
                27,
                resultado.totalHorasSemanalesAsignadas()
        );

        assertEquals(
                5,
                resultado.totalHorasSemanalesExtras()
        );

        var detalle = resultado.docentes().get(0);

        assertEquals(
                27,
                detalle.horasSemanalesAsignadas()
        );

        assertEquals(
                5,
                detalle.horasSemanalesExtras()
        );
    }

    /**
     * Caso 3:
     *
     * El informe original debe continuar funcionando
     * cuando no se selecciona un año escolar.
     */
    @Test
    void debeMantenerLaConsultaOriginalSinAnioEscolar() {

        Docente docente = crearDocente(22);

        when(docenteRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(docente));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null
                );

        assertEquals(
                1,
                resultado.cantidadDocentes()
        );

        assertEquals(
                22,
                resultado.totalHorasSemanalesContratadas()
        );

        assertEquals(
                0,
                resultado.totalHorasSemanalesAsignadas()
        );

        assertEquals(
                0,
                resultado.totalHorasSemanalesExtras()
        );

        assertNull(resultado.anioEscolarId());
        assertNull(resultado.anioEscolar());

        verifyNoInteractions(
                anioEscolarRepository,
                asignacionAcademicaRepository
        );
    }

    /**
     * Caso 4:
     *
     * Un año escolar inexistente debe devolver
     * una excepcion de recurso no encontrado.
     */
    @Test
    void debeRechazarAnioEscolarInexistente() {

        UUID anioId = UUID.randomUUID();

        when(anioEscolarRepository.findById(anioId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> informeService.generarInforme(
                        null, null, null, anioId
                )
        );

        verifyNoInteractions(
                docenteRepository,
                asignacionAcademicaRepository
        );
    }

    /**
     * Crea un año escolar simulado.
     *
     * Se simula porque la entidad genera
     * automaticamente su UUID.
     */
    private AnioEscolar crearAnio(
            UUID id,
            int numero) {

        AnioEscolar anio = mock(AnioEscolar.class);

        when(anio.getId()).thenReturn(id);
        when(anio.getAnio()).thenReturn(numero);

        return anio;
    }

    /**
     * Crea un docente ficticio.
     */
    private Docente crearDocente(
            int horasContratadas) {

        Docente docente = mock(Docente.class);
        Area area = mock(Area.class);

        UUID docenteId = UUID.randomUUID();

        when(docente.getId())
                .thenReturn(docenteId);

        when(docente.getNombres())
                .thenReturn("Docente");

        when(docente.getApellidos())
                .thenReturn("Prueba");

        when(docente.getAreaNombramiento())
                .thenReturn(area);

        when(area.getNombre())
                .thenReturn("Ciencias Naturales");

        when(docente.getEstado())
                .thenReturn(EstadoDocente.ACTIVO);

        when(docente.getTipoVinculacion())
                .thenReturn(TipoVinculacion.PLANTA);

        when(docente.getHorasSemanalesContratadas())
                .thenReturn(horasContratadas);

        return docente;
    }

    /**
     * Crea una asignacion academica real
     * en memoria, sin persistirla.
     *
     * Evita el anidamiento de configuraciones
     * Mockito que producia UnfinishedStubbing.
     */
    private AsignacionAcademica crearAsignacion(
            AnioEscolar anio,
            int horas,
            EstadoAsignacionAcademica estado) {

        AsignacionAcademica asignacion =
                new AsignacionAcademica();

        asignacion.setAnioEscolar(anio);
        asignacion.setHorasSemanales(horas);
        asignacion.setEstado(estado);

        return asignacion;
    }
}
