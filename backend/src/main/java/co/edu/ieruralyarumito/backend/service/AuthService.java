package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.config.JwtProperties;
import co.edu.ieruralyarumito.backend.dto.LoginRequest;
import co.edu.ieruralyarumito.backend.dto.LoginResponse;
import co.edu.ieruralyarumito.backend.dto.UsuarioResponse;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.exception.CredencialesInvalidasException;
import co.edu.ieruralyarumito.backend.exception.CuentaInactivaException;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Valida las credenciales del formulario de login y emite el JWT.
@Service
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS =
            "Correo o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    // Autentica al usuario y devuelve el token junto con sus datos.
    @Transactional
    public LoginResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByCorreo(request.getUsername().trim())
                .orElseThrow(() ->
                        new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "La cuenta de usuario no está activa");
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        Duration duracion = request.isRemember()
                ? jwtProperties.getRememberExpiration()
                : jwtProperties.getExpiration();

        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generarToken(usuario, duracion));
        response.setExpiresIn(duracion.toSeconds());
        response.setUsuario(mapearUsuario(usuario));
        return response;
    }

    // Recupera el usuario autenticado a partir del correo del token.
    @Transactional(readOnly = true)
    public UsuarioResponse consultarUsuarioAutenticado(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new CredencialesInvalidasException("No autenticado"));

        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "La cuenta de usuario no está activa");
        }

        return mapearUsuario(usuario);
    }

    private UsuarioResponse mapearUsuario(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setCorreo(usuario.getCorreo());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.isActivo());
        return response;
    }
}
