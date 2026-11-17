package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.UsuarioResponse;
import co.edu.ieruralyarumito.backend.service.AuthService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Lista las cuentas para que el rector consulte auditoría sin escribir un UUID.
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final AuthService authService;

    public UsuarioController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<UsuarioResponse> listar(Authentication authentication) {
        exigirRector(authentication);
        return authService.listarUsuarios();
    }

    private void exigirRector(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere autenticacion.");
        }

        boolean esRector = authentication.getAuthorities().stream()
                .anyMatch(autoridad -> {
                    String nombre = autoridad.getAuthority();
                    return "RECTOR".equals(nombre) || "ROLE_RECTOR".equals(nombre);
                });

        if (!esRector) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo del rector.");
        }
    }
}
