package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AnioEscolarResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import co.edu.ieruralyarumito.backend.dto.ActualizarAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;


// Pruebas unitarias de la lógica de negocio de AnioEscolarService.
@ExtendWith(MockitoExtension.class)
public class AnioEscolarServiceTest {

    @Mock
    private AnioEscolarRepository anioEscolarRepository;

    @InjectMocks
    private AnioEscolarService anioEscolarService;

    // RN-12.01: no permite registrar dos veces el mismo año lectivo.
    @Test
    void registrarAnioEscolar_debeRechazarAnioDuplicado() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> anioEscolarService.registrarAnioEscolar(request)
        );
    }

    // RN-12.02: la fecha de inicio debe ser anterior a la fecha final.
    @Test
    void registrarAnioEscolar_debeRechazarFechasInvalidas() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 11, 30));
        request.setFechaFin(LocalDate.of(2027, 1, 20));
        request.setEstado(EstadoAnioEscolar.PLANEACION);
        request.setEsActual(false);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(false);

        assertThrows(
                FechaVigenciaInvalidaException.class,
                () -> anioEscolarService.registrarAnioEscolar(request)
        );
    }

    // Verifica que un año escolar válido pueda registrarse correctamente.
    @Test
    void registrarAnioEscolar_debeRegistrarCorrectamente() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.PLANEACION);
        request.setEsActual(false);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(false);

        when(anioEscolarRepository.save(any(AnioEscolar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnioEscolarResponse response =
                anioEscolarService.registrarAnioEscolar(request);

        assertEquals(2027, response.getAnio());
        assertEquals(
                LocalDate.of(2027, 1, 18),
                response.getFechaInicio()
        );
        assertEquals(
                LocalDate.of(2027, 11, 30),
                response.getFechaFin()
        );
        assertEquals(
                EstadoAnioEscolar.PLANEACION,
                response.getEstado()
        );
        assertFalse(response.isEsActual());
    }

    // RN-12.04: un año en PLANEACION no puede marcarse como actual.
    @Test
    void registrarAnioEscolar_debeRechazarPlaneacionComoActual() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.PLANEACION);
        request.setEsActual(true);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(false);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> anioEscolarService.registrarAnioEscolar(request)
        );
    }


    // RN-12.04: un año ACTIVO debe estar marcado como actual.
    @Test
    void registrarAnioEscolar_debeRechazarActivoNoActual() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.ACTIVO);
        request.setEsActual(false);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(false);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> anioEscolarService.registrarAnioEscolar(request)
        );
    }


    // RN-12.03: no permite tener dos años escolares actuales.
    @Test
    void registrarAnioEscolar_debeRechazarSegundoAnioActual() {

        CrearAnioEscolarRequest request =
                new CrearAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.ACTIVO);
        request.setEsActual(true);

        when(anioEscolarRepository.existsByAnio(2027))
                .thenReturn(false);

        when(anioEscolarRepository.existsByEsActualTrue())
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> anioEscolarService.registrarAnioEscolar(request)
        );
    }


    // RN-12.05: permite pasar de PLANEACION a ACTIVO.
    @Test
    void actualizarAnioEscolar_debePermitirPlaneacionAActivo() {

        UUID anioEscolarId = UUID.randomUUID();

        AnioEscolar existente = new AnioEscolar();
        existente.setAnio(2027);
        existente.setFechaInicio(LocalDate.of(2027, 1, 18));
        existente.setFechaFin(LocalDate.of(2027, 11, 30));
        existente.setEstado(EstadoAnioEscolar.PLANEACION);
        existente.setEsActual(false);

        ActualizarAnioEscolarRequest request =
                new ActualizarAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.ACTIVO);
        request.setEsActual(true);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(existente));

        when(anioEscolarRepository.existsByAnioAndIdNot(
                2027,
                anioEscolarId))
                .thenReturn(false);

        when(anioEscolarRepository.existsByEsActualTrueAndIdNot(
                anioEscolarId))
                .thenReturn(false);

        when(anioEscolarRepository.save(any(AnioEscolar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnioEscolarResponse response =
                anioEscolarService.actualizarAnioEscolar(
                        anioEscolarId,
                        request);

        assertEquals(
                EstadoAnioEscolar.ACTIVO,
                response.getEstado()
        );

        assertTrue(response.isEsActual());
    }

    // RN-12.05: permite pasar de ACTIVO a CERRADO.
    @Test
    void actualizarAnioEscolar_debePermitirActivoACerrado() {

        UUID anioEscolarId = UUID.randomUUID();

        AnioEscolar existente = new AnioEscolar();
        existente.setAnio(2027);
        existente.setFechaInicio(LocalDate.of(2027, 1, 18));
        existente.setFechaFin(LocalDate.of(2027, 11, 30));
        existente.setEstado(EstadoAnioEscolar.ACTIVO);
        existente.setEsActual(true);

        ActualizarAnioEscolarRequest request =
                new ActualizarAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.CERRADO);
        request.setEsActual(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(existente));

        when(anioEscolarRepository.existsByAnioAndIdNot(
                2027,
                anioEscolarId))
                .thenReturn(false);

        when(anioEscolarRepository.save(any(AnioEscolar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnioEscolarResponse response =
                anioEscolarService.actualizarAnioEscolar(
                        anioEscolarId,
                        request);

        assertEquals(
                EstadoAnioEscolar.CERRADO,
                response.getEstado()
        );

        assertFalse(response.isEsActual());
    }


    // RN-12.05: no permite regresar de ACTIVO a PLANEACION.
    @Test
    void actualizarAnioEscolar_debeRechazarActivoAPlaneacion() {

        UUID anioEscolarId = UUID.randomUUID();

        AnioEscolar existente = new AnioEscolar();
        existente.setAnio(2027);
        existente.setFechaInicio(LocalDate.of(2027, 1, 18));
        existente.setFechaFin(LocalDate.of(2027, 11, 30));
        existente.setEstado(EstadoAnioEscolar.ACTIVO);
        existente.setEsActual(true);

        ActualizarAnioEscolarRequest request =
                new ActualizarAnioEscolarRequest();

        request.setAnio(2027);
        request.setFechaInicio(LocalDate.of(2027, 1, 18));
        request.setFechaFin(LocalDate.of(2027, 11, 30));
        request.setEstado(EstadoAnioEscolar.PLANEACION);
        request.setEsActual(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(existente));

        when(anioEscolarRepository.existsByAnioAndIdNot(
                2027,
                anioEscolarId))
                .thenReturn(false);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> anioEscolarService.actualizarAnioEscolar(
                        anioEscolarId,
                        request)
        );
    }


    // RN-12.06: un año CERRADO no puede modificarse.
    @Test
    void actualizarAnioEscolar_debeRechazarModificacionDeCerrado() {

        UUID anioEscolarId = UUID.randomUUID();

        AnioEscolar existente = new AnioEscolar();
        existente.setAnio(2026);
        existente.setFechaInicio(LocalDate.of(2026, 1, 19));
        existente.setFechaFin(LocalDate.of(2026, 11, 30));
        existente.setEstado(EstadoAnioEscolar.CERRADO);
        existente.setEsActual(false);

        ActualizarAnioEscolarRequest request =
                new ActualizarAnioEscolarRequest();

        request.setAnio(2026);
        request.setFechaInicio(LocalDate.of(2026, 1, 20));
        request.setFechaFin(LocalDate.of(2026, 11, 30));
        request.setEstado(EstadoAnioEscolar.CERRADO);
        request.setEsActual(false);

        when(anioEscolarRepository.findById(anioEscolarId))
                .thenReturn(Optional.of(existente));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> anioEscolarService.actualizarAnioEscolar(
                        anioEscolarId,
                        request)
        );
    }
}
