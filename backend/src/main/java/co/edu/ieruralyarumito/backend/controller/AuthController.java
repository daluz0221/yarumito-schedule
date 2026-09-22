package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.LoginRequest;
import co.edu.ieruralyarumito.backend.dto.LoginResponse;
import co.edu.ieruralyarumito.backend.dto.UsuarioResponse;
import co.edu.ieruralyarumito.backend.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Expone el login y la consulta del usuario autenticado.
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Recibe username, password y remember desde el formulario de login.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }

    // Devuelve el usuario asociado al JWT enviado en Authorization.
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> consultarUsuarioAutenticado(
            Principal principal) {
        return ResponseEntity.ok(
                authService.consultarUsuarioAutenticado(principal.getName()));
    }
}
