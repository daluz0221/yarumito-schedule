package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGradoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGradoRequest;
import co.edu.ieruralyarumito.backend.dto.GradoResponse;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradoServiceUnicidadTest {

    @Mock
    private GradoRepository gradoRepository;

    @InjectMocks
    private GradoService gradoService;

    @Test
    void H06_debeRechazarGradoDuplicado() {

        CrearGradoRequest request =
                new CrearGradoRequest();

        request.setNivel(7);

        when(gradoRepository.existsByNivel(7))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> gradoService.registrarGrado(request));

        verify(gradoRepository, never())
                .save(any(Grado.class));
    }

    @Test
    void H06_debeRechazarActualizarANivelExistente() {

        UUID gradoId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);

        ActualizarGradoRequest request =
                new ActualizarGradoRequest();

        request.setNivel(7);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(gradoRepository.existsByNivelAndIdNot(7, gradoId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> gradoService.actualizarGrado(
                        gradoId, request));

        verify(gradoRepository, never())
                .save(any(Grado.class));
    }

    @Test
    void H06_debePermitirActualizarMismoNivel() {

        UUID gradoId = UUID.randomUUID();

        Grado grado = new Grado();
        grado.setNivel(6);

        ActualizarGradoRequest request =
                new ActualizarGradoRequest();

        request.setNivel(6);

        when(gradoRepository.findById(gradoId))
                .thenReturn(Optional.of(grado));

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GradoResponse resultado =
                gradoService.actualizarGrado(
                        gradoId, request);

        assertEquals(6, resultado.getNivel());
        assertEquals("SEXTO", resultado.getNombre());
    }
}
