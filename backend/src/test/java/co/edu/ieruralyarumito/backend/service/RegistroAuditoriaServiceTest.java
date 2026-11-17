package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.entity.RegistroAuditoria;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.repository.RegistroAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroAuditoriaServiceTest {

    @Mock
    private RegistroAuditoriaRepository registroAuditoriaRepository;

    private RegistroAuditoriaService registroAuditoriaService;

    @BeforeEach
    void setUp() {
        registroAuditoriaService =
                new RegistroAuditoriaService(
                        registroAuditoriaRepository,
                        new ObjectMapper()
                );
    }

    @Test
    void registrarCreate_debeGuardarEstadoPosterior() {

        Usuario usuario = new Usuario();
        UUID idEntidad = UUID.randomUUID();

        Map<String, Object> datosDespues =
                Map.of(
                        "tipo", "PREFERENTE",
                        "motivo", "Preferencia registrada"
                );

        when(registroAuditoriaRepository
                .save(any(RegistroAuditoria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RegistroAuditoria registro =
                registroAuditoriaService.registrar(
                        usuario,
                        RegistroAuditoriaService.ACCION_CREATE,
                        "RestriccionDocente",
                        idEntidad,
                        null,
                        datosDespues,
                        "127.0.0.1"
                );

        assertEquals(
                RegistroAuditoriaService.ACCION_CREATE,
                registro.getAccion());

        assertEquals(
                "RestriccionDocente",
                registro.getEntidad());

        assertEquals(
                idEntidad,
                registro.getIdEntidad());

        assertNull(
                registro.getDatosAntes());

        assertNotNull(
                registro.getDatosDespues());

        assertNotNull(
                registro.getFecha());

        assertEquals(
                "127.0.0.1",
                registro.getIp());

        verify(registroAuditoriaRepository)
                .save(any(RegistroAuditoria.class));
    }

    @Test
    void registrarUpdate_debeGuardarEstadoAnteriorYPosterior() {

        Usuario usuario = new Usuario();
        UUID idEntidad = UUID.randomUUID();

        Map<String, Object> datosAntes =
                Map.of(
                        "tipo", "PREFERENTE"
                );

        Map<String, Object> datosDespues =
                Map.of(
                        "tipo", "NO_DISPONIBLE"
                );

        when(registroAuditoriaRepository
                .save(any(RegistroAuditoria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RegistroAuditoria registro =
                registroAuditoriaService.registrar(
                        usuario,
                        RegistroAuditoriaService.ACCION_UPDATE,
                        "RestriccionDocente",
                        idEntidad,
                        datosAntes,
                        datosDespues,
                        null
                );

        assertNotNull(
                registro.getDatosAntes());

        assertNotNull(
                registro.getDatosDespues());

        assertEquals(
                RegistroAuditoriaService.ACCION_UPDATE,
                registro.getAccion());

        assertNull(
                registro.getIp());
    }

    @Test
    void registrar_debeRechazarUsuarioNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> registroAuditoriaService.registrar(
                        null,
                        RegistroAuditoriaService.ACCION_CREATE,
                        "RestriccionDocente",
                        UUID.randomUUID(),
                        null,
                        Map.of("tipo", "PREFERENTE"),
                        null
                )
        );

        verify(registroAuditoriaRepository, never())
                .save(any());
    }

    @Test
    void registrar_debeRechazarAccionInvalida() {

        Usuario usuario = new Usuario();

        assertThrows(
                IllegalArgumentException.class,
                () -> registroAuditoriaService.registrar(
                        usuario,
                        "CONSULTAR",
                        "RestriccionDocente",
                        UUID.randomUUID(),
                        null,
                        null,
                        null
                )
        );

        verify(registroAuditoriaRepository, never())
                .save(any());
    }

    @Test
    void registrar_debeNormalizarIpVaciaANull() {

        Usuario usuario = new Usuario();

        when(registroAuditoriaRepository
                .save(any(RegistroAuditoria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RegistroAuditoria registro =
                registroAuditoriaService.registrar(
                        usuario,
                        RegistroAuditoriaService.ACCION_CREATE,
                        "ParametroInstitucional",
                        UUID.randomUUID(),
                        null,
                        Map.of("valor", "22"),
                        "   "
                );

        assertNull(
                registro.getIp());
    }
}
