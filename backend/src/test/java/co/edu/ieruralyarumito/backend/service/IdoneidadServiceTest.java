package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.ieruralyarumito.backend.dto.FinalizarVigenciaIdoneidadRequest;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

// Pruebas unitarias de la lógica de negocio de IdoneidadService.
@ExtendWith(MockitoExtension.class)
public class IdoneidadServiceTest {

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private TituloProfesionalRepository tituloProfesionalRepository;

    @InjectMocks
    private IdoneidadService idoneidadService;

    // Verifica que no se permita finalizar una idoneidad con una fecha anterior al inicio.
    @Test
    void finalizarVigencia_debeRechazarFechaAnteriorAlInicio() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad existente con fecha de inicio definida.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 18));

        // Prepara una fecha de finalización anterior al inicio.
        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 17));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Verifica que el servicio rechace la fecha inválida.
        assertThrows(
                FechaVigenciaInvalidaException.class,
                () -> idoneidadService.finalizarVigencia(idoneidadId, request)
        );
    }

    // Verifica que una fecha válida finalice correctamente la vigencia.
    @Test
    void finalizarVigencia_debeFinalizarCorrectamente() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad existente.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setDocente(new Docente());
        idoneidad.setArea(new Area());
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 18));

        // Prepara una fecha válida de finalización.
        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 20));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Devuelve la misma entidad que el servicio guarda.
        when(idoneidadRepository.save(any(Idoneidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta la finalización.
        IdoneidadResponse response =
                idoneidadService.finalizarVigencia(idoneidadId, request);

        // Verifica que la fecha haya quedado registrada.
        assertEquals(
                LocalDate.of(2026, 9, 20),
                response.getVigenteHasta()
        );
    }

    // Verifica que no se pueda finalizar nuevamente una idoneidad ya cerrada.
    @Test
    void finalizarVigencia_debeRechazarIdoneidadYaFinalizada() {

        UUID idoneidadId = UUID.randomUUID();

        // Simula una idoneidad que ya tiene fecha de finalización.
        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setVigenteDesde(LocalDate.of(2026, 9, 10));
        idoneidad.setVigenteHasta(LocalDate.of(2026, 9, 15));

        FinalizarVigenciaIdoneidadRequest request =
                new FinalizarVigenciaIdoneidadRequest();

        request.setVigenteHasta(LocalDate.of(2026, 9, 20));

        // Simula que la idoneidad existe.
        when(idoneidadRepository.findById(idoneidadId))
                .thenReturn(Optional.of(idoneidad));

        // Verifica que el servicio rechace una segunda finalización.
        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> idoneidadService.finalizarVigencia(idoneidadId, request)
        );
    }
}
