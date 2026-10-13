package co.edu.ieruralyarumito.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos que envía el formulario de login del frontend.
public class LoginRequest {

    // Correo electrónico. El form lo envía como "username".
    @NotBlank(message = "El usuario es obligatorio")
    @Email(message = "El usuario debe ser un correo válido")
    @JsonAlias({"correo", "email"})
    private String username;

    // Contraseña en texto plano; solo se usa para validar el hash.
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // Si es true, el token JWT dura más tiempo.
    private boolean remember;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }
}
