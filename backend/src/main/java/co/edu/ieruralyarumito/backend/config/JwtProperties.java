package co.edu.ieruralyarumito.backend.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

// Configuración del token JWT leída desde application.yaml.
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    // Clave HMAC. Debe tener al menos 32 caracteres.
    private String secret;

    // Duración del token en un inicio de sesión normal.
    private Duration expiration = Duration.ofHours(8);

    // Duración del token cuando el form envía remember=true.
    private Duration rememberExpiration = Duration.ofDays(7);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public Duration getRememberExpiration() {
        return rememberExpiration;
    }

    public void setRememberExpiration(Duration rememberExpiration) {
        this.rememberExpiration = rememberExpiration;
    }
}
