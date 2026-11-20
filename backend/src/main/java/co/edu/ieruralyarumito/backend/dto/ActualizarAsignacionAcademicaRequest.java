package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

// Permite corregir los datos de una asignación en BORRADOR.
// El año escolar, grupo y asignatura permanecen sin cambios.
public class ActualizarAsignacionAcademicaRequest {

    @NotNull(message = "Debe seleccionar un docente válido")
    private UUID docenteId;

    @NotNull(message = "Las horas semanales son obligatorias")
    @Positive(message = "Las horas semanales deben ser positivas")
    private Integer horasSemanales;

    private String observacion;

    public UUID getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(UUID docenteId) {
        this.docenteId = docenteId;
    }

    public Integer getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(Integer horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
