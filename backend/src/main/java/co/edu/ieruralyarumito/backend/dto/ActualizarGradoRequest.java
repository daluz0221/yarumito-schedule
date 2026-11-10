package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Datos necesarios para actualizar un grado escolar.
public class ActualizarGradoRequest {

    // El nivel determina automáticamente nombre, tipo de educación,
    // prioridad de asignación y horas semanales esperadas.
    @NotNull(message = "El nivel del grado es obligatorio")
    @Min(value = 6, message = "El grado mínimo permitido es 6")
    @Max(value = 11, message = "El grado máximo permitido es 11")
    private Integer nivel;

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
}
