package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos necesarios para registrar una nueva sede.
public class CrearSedeRequest {

    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El código de la sede es obligatorio")
    @Size(max = 20, message = "El código no puede superar los 20 caracteres")
    private String codigo;

    @Size(max = 100, message = "La dirección no puede superar los 100 caracteres")
    private String direccion;

    @NotNull(message = "Debe indicar si la sede es principal")
    private Boolean esPrincipal;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
}
