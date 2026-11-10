package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.LoginRequest;
import co.edu.ieruralyarumito.backend.dto.LoginResponse;
import co.edu.ieruralyarumito.backend.dto.UsuarioResponse;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.exception.CredencialesInvalidasException;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas unitarias de los endpoints de autenticación.
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void iniciarSesion_debeRetornarOk() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        UsuarioResponse usuario = new UsuarioResponse();
        usuario.setId(usuarioId);
        usuario.setCorreo("rector@yarumito.edu.co");
        usuario.setRol(RolUsuario.RECTOR);
        usuario.setActivo(true);

        LoginResponse response = new LoginResponse();
        response.setToken("token.jwt");
        response.setExpiresIn(28800);
        response.setUsuario(usuario);

        when(authService.iniciarSesion(any(LoginRequest.class)))
                .thenReturn(response);

        String json = """
            {
              "username": "rector@yarumito.edu.co",
              "password": "Admin123!",
              "remember": false
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.usuario.correo").value("rector@yarumito.edu.co"))
                .andExpect(jsonPath("$.usuario.rol").value("RECTOR"));
    }

    @Test
    void iniciarSesion_debeRetornarUnauthorizedConCredencialesInvalidas() throws Exception {
        when(authService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException(
                        "Correo o contraseña incorrectos"));

        String json = """
            {
              "username": "rector@yarumito.edu.co",
              "password": "incorrecta"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje")
                        .value("Correo o contraseña incorrectos"));
    }

    @Test
    void iniciarSesion_debeRetornarBadRequestConDatosInvalidos() throws Exception {
        String json = """
            {
              "username": "",
              "password": ""
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
