package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarSedeRequest;
import co.edu.ieruralyarumito.backend.dto.CrearSedeRequest;
import co.edu.ieruralyarumito.backend.dto.SedeResponse;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de SedeService.
@ExtendWith(MockitoExtension.class)
public class SedeServiceTest {

    @Mock
    private SedeRepository sedeRepository;

    @InjectMocks
    private SedeService sedeService;

    // RN-12.S01: no permite registrar dos sedes con el mismo código.
    @Test
    void registrarSede_debeRechazarCodigoDuplicado() {

        CrearSedeRequest request = new CrearSedeRequest();
        request.setNombre("C.E.R. Popalito");
        request.setCodigo("POP");
        request.setEsPrincipal(false);

        when(sedeRepository.existsByCodigo("POP"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sedeService.registrarSede(request)
        );
    }

    // RN-12.S02: no permite registrar una segunda sede principal.
    @Test
    void registrarSede_debeRechazarSegundaSedePrincipal() {

        CrearSedeRequest request = new CrearSedeRequest();
        request.setNombre("I.E.R. Yarumito");
        request.setCodigo("YAR");
        request.setEsPrincipal(true);

        when(sedeRepository.existsByCodigo("YAR"))
                .thenReturn(false);

        when(sedeRepository.existsByEsPrincipalTrue())
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sedeService.registrarSede(request)
        );
    }

    // Verifica que nombre y código se normalicen a mayúsculas.
    @Test
    void registrarSede_debeNormalizarNombreYCodigo() {

        CrearSedeRequest request = new CrearSedeRequest();
        request.setNombre("  I.e.r. Yarumito  ");
        request.setCodigo("  yar  ");
        request.setDireccion(
                "  Vereda Yarumito, kilómetro 4 vía a Puerto Berrío  "
        );
        request.setEsPrincipal(false);

        when(sedeRepository.existsByCodigo("YAR"))
                .thenReturn(false);

        when(sedeRepository.save(any(Sede.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SedeResponse response =
                sedeService.registrarSede(request);

        assertEquals(
                "I.E.R. YARUMITO",
                response.getNombre()
        );

        assertEquals(
                "YAR",
                response.getCodigo()
        );

        assertEquals(
                "Vereda Yarumito, kilómetro 4 vía a Puerto Berrío",
                response.getDireccion()
        );

        assertFalse(response.isEsPrincipal());
    }

    // Verifica que consultar una sede inexistente genere el error correspondiente.
    @Test
    void consultarSede_debeRechazarSedeInexistente() {

        UUID sedeId = UUID.randomUUID();

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> sedeService.consultarSede(sedeId)
        );
    }

    // RN-12.S01: no permite actualizar una sede usando el código de otra.
    @Test
    void actualizarSede_debeRechazarCodigoDuplicado() {

        UUID sedeId = UUID.randomUUID();

        Sede sede = new Sede();

        ActualizarSedeRequest request =
                new ActualizarSedeRequest();

        request.setNombre("C.E.R. La Cejita");
        request.setCodigo("CEJ");
        request.setEsPrincipal(false);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(sedeRepository.existsByCodigoAndIdNot(
                "CEJ",
                sedeId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sedeService.actualizarSede(
                        sedeId,
                        request)
        );
    }

    // RN-12.S02: no permite convertir otra sede en principal
    // cuando ya existe una diferente marcada como principal.
    @Test
    void actualizarSede_debeRechazarSegundaSedePrincipal() {

        UUID sedeId = UUID.randomUUID();

        Sede sede = new Sede();
        sede.setEsPrincipal(false);

        ActualizarSedeRequest request =
                new ActualizarSedeRequest();

        request.setNombre("C.E.R. Popalito");
        request.setCodigo("POP");
        request.setEsPrincipal(true);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(sedeRepository.existsByCodigoAndIdNot(
                "POP",
                sedeId))
                .thenReturn(false);

        when(sedeRepository.existsByEsPrincipalTrueAndIdNot(
                sedeId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sedeService.actualizarSede(
                        sedeId,
                        request)
        );
    }

}
