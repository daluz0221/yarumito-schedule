package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.config.JwtProperties;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

// Genera y valida los tokens JWT usados tras el login.
@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // Emite un token firmado con el correo, el rol y el identificador del usuario.
    public String generarToken(Usuario usuario, Duration duracion) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(duracion);

        var builder = Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(clave());

        if (usuario.getId() != null) {
            builder.claim("userId", usuario.getId().toString());
        }

        return builder.compact();
    }

    // Lee y verifica la firma del token.
    public Claims leerToken(String token) {
        return Jwts.parser()
                .verifyWith(clave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
