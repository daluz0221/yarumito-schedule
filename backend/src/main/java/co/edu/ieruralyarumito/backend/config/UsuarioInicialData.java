package co.edu.ieruralyarumito.backend.config;

import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea un rector activo la primera vez que arranca la aplicación.
@Component
@Order(1)
public class UsuarioInicialData implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public UsuarioInicialData(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.email}") String email,
            @Value("${app.seed.password}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByCorreo(email)) {
            return;
        }

        Usuario rector = new Usuario();
        rector.setCorreo(email);
        rector.setContrasenaHash(passwordEncoder.encode(password));
        rector.setRol(RolUsuario.RECTOR);
        rector.setActivo(true);
        usuarioRepository.save(rector);
    }
}
