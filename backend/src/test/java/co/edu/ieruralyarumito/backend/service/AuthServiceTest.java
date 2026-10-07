package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.config.JwtProperties;
import co.edu.ieruralyarumito.backend.dto.LoginRequest;
import co.edu.ieruralyarumito.backend.dto.LoginResponse;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.exception.CredencialesInvalidasException;
import co.edu.ieruralyarumito.backend.exception.CuentaInactivaException;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la autenticación con el formulario de login.
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private Usuario rector;

    @BeforeEach
    void configurar() {
        rector = new Usuario();
        rector.setCorreo("rector@yarumito.edu.co");
        rector.setContrasenaHash("hash");
        rector.setRol(RolUsuario.RECTOR);
        rector.setActivo(true);
    }

    @Test
    void iniciarSesion_debeRechazarUsuarioInexistente() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nadie@yarumito.edu.co");
        request.setPassword("Admin123!");

        when(usuarioRepository.findByCorreo("nadie@yarumito.edu.co"))
                .thenReturn(Optional.empty());

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(request)
        );
    }

    @Test
    void iniciarSesion_debeRechazarContrasenaIncorrecta() {
        LoginRequest request = new LoginRequest();
        request.setUsername("rector@yarumito.edu.co");
        request.setPassword("otra");

        when(usuarioRepository.findByCorreo("rector@yarumito.edu.co"))
                .thenReturn(Optional.of(rector));
        when(passwordEncoder.matches("otra", "hash")).thenReturn(false);

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(request)
        );
    }

    @Test
    void iniciarSesion_debeRechazarCuentaInactiva() {
        rector.setActivo(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("rector@yarumito.edu.co");
        request.setPassword("Admin123!");

        when(usuarioRepository.findByCorreo("rector@yarumito.edu.co"))
                .thenReturn(Optional.of(rector));
        when(passwordEncoder.matches("Admin123!", "hash")).thenReturn(true);

        assertThrows(
                CuentaInactivaException.class,
                () -> authService.iniciarSesion(request)
        );
    }

    @Test
    void iniciarSesion_debeRetornarTokenCuandoLasCredencialesSonValidas() {
        LoginRequest request = new LoginRequest();
        request.setUsername("rector@yarumito.edu.co");
        request.setPassword("Admin123!");
        request.setRemember(false);

        when(usuarioRepository.findByCorreo("rector@yarumito.edu.co"))
                .thenReturn(Optional.of(rector));
        when(passwordEncoder.matches("Admin123!", "hash")).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(rector);
        when(jwtProperties.getExpiration()).thenReturn(Duration.ofHours(8));
        when(jwtService.generarToken(rector, Duration.ofHours(8)))
                .thenReturn("token.jwt");

        LoginResponse response = authService.iniciarSesion(request);

        assertEquals("token.jwt", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(Duration.ofHours(8).toSeconds(), response.getExpiresIn());
        assertEquals("rector@yarumito.edu.co", response.getUsuario().getCorreo());
        assertEquals(RolUsuario.RECTOR, response.getUsuario().getRol());
    }
}
