package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.config.JwtProperties;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias de generación y lectura de JWT.
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void configurar() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("yarumito-dev-jwt-secret-key-32chars");
        jwtService = new JwtService(properties);
    }

    @Test
    void generarToken_debeIncluirCorreoYRol() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("rector@yarumito.edu.co");
        usuario.setRol(RolUsuario.RECTOR);

        String token = jwtService.generarToken(usuario, Duration.ofHours(8));
        Claims claims = jwtService.leerToken(token);

        assertEquals("rector@yarumito.edu.co", claims.getSubject());
        assertEquals("RECTOR", claims.get("rol", String.class));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}
