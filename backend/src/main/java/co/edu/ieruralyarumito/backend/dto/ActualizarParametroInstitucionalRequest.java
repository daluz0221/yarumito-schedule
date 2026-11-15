package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotBlank;

// Representa los datos permitidos para actualizar un parámetro institucional.
public class ActualizarParametroInstitucionalRequest {

    // Nuevo valor del parámetro.
    @NotBlank(message = "El valor es obligatorio")
    private String valor;

    // Descripción funcional del parámetro.
    private String descripcion;

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
