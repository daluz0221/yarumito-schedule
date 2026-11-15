package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

// Datos necesarios para asignar una actividad institucional a un docente.
public class CrearAsignacionActividadRequest {

    @NotNull(message = "Debe seleccionar una actividad institucional válida")
    private UUID actividadId;

    @NotNull(message = "La cantidad de horas semanales es obligatoria")
    @Positive(message = "La cantidad de horas semanales debe ser un número positivo")
    private Integer horasSemanales;

    @NotBlank(message = "La justificación es obligatoria")
    private String justificacion;

    public UUID getActividadId() {
        return actividadId;
    }

    public void setActividadId(UUID actividadId) {
        this.actividadId = actividadId;
    }

    public Integer getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(Integer horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
}
