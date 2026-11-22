package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
 * HU-04 - Filtro individual de Planta Docente.
 *
 * Verifica:
 * 1. Consulta de un docente especifico.
 * 2. Consulta general sin docenteId.
 * 3. Consulta de un docente inexistente.
 *
 * No modifica la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class InformePlantaDocenteFiltroDocenteTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AsignacionAcademicaRepository
            asignacionAcademicaRepository;

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @InjectMocks
    private InformePlantaDocenteService informeService;

    @Captor
    private ArgumentCaptor<Specification<Docente>> filtrosCaptor;

    /**
     * La consulta individual debe construir
     * una condicion por el UUID solicitado.
     */
    @Test
    void debeFiltrarPorDocenteId() {

        UUID docenteId = UUID.randomUUID();

        Docente docente = crearDocente(
                docenteId, "Carlos", "Perez", 22
        );

        when(docenteRepository.findAll(
                any(Specification.class)
        )).thenReturn(List.of(docente));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null, null, docenteId
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(docenteId,
                resultado.docentes().get(0).docenteId());
        assertEquals(22,
                resultado.totalHorasSemanalesContratadas());

        /*
         * Comprueba que la Specification enviada
         * al repositorio contiene el filtro por ID.
         */
        verify(docenteRepository)
                .findAll(filtrosCaptor.capture());

        @SuppressWarnings("unchecked")
        Root<Docente> root = mock(Root.class);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<UUID> rutaId = mock(Path.class);

        when(root.<UUID>get("id")).thenReturn(rutaId);

        filtrosCaptor.getValue()
                .toPredicate(root, null, cb);

        verify(cb).equal(rutaId, docenteId);
    }

    /**
     * Sin docenteId se mantiene la consulta
     * completa de la planta docente.
     */
    @Test
    void debeMantenerConsultaGeneral() {

        Docente docente1 = crearDocente(
                UUID.randomUUID(), "Carlos", "Perez", 22
        );

        Docente docente2 = crearDocente(
                UUID.randomUUID(), "Ana", "Gomez", 20
        );

        when(docenteRepository.findAll(
                any(Specification.class)
        )).thenReturn(List.of(docente1, docente2));

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null, null, null
                );

        assertEquals(2, resultado.cantidadDocentes());
        assertEquals(42,
                resultado.totalHorasSemanalesContratadas());

        assertEquals(2, resultado.docentes().size());

        assertNull(resultado.anioEscolarId());

        verifyNoInteractions(
                anioEscolarRepository,
                asignacionAcademicaRepository
        );
    }

    /**
     * Si el docente no existe, el informe
     * retorna una lista vacia y totales cero.
     */
    @Test
    void debeRetornarVacioParaDocenteInexistente() {

        UUID docenteId = UUID.randomUUID();

        when(docenteRepository.findAll(
                any(Specification.class)
        )).thenReturn(List.of());

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null, null, docenteId
                );

        assertEquals(0, resultado.cantidadDocentes());

        assertTrue(resultado.docentes().isEmpty());

        assertEquals(0,
                resultado.totalHorasSemanalesContratadas());

        assertEquals(0,
                resultado.totalHorasSemanalesAsignadas());

        assertEquals(0,
                resultado.totalHorasSemanalesExtras());
    }

    /**
     * Crea un docente ficticio para las pruebas.
     */
    private Docente crearDocente(
            UUID id,
            String nombres,
            String apellidos,
            int horas) {

        Docente docente = mock(Docente.class);
        Area area = mock(Area.class);

        when(docente.getId()).thenReturn(id);
        when(docente.getNombres()).thenReturn(nombres);
        when(docente.getApellidos()).thenReturn(apellidos);
        when(docente.getAreaNombramiento())
                .thenReturn(area);
        when(area.getNombre())
                .thenReturn("Matematicas");

        when(docente.getEstado())
                .thenReturn(EstadoDocente.ACTIVO);

        when(docente.getTipoVinculacion())
                .thenReturn(TipoVinculacion.PLANTA);

        when(docente.getHorasSemanalesContratadas())
                .thenReturn(horas);

        return docente;
    }
}
