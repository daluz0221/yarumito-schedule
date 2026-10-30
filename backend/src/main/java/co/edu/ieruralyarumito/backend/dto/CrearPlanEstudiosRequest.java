package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Datos necesarios para registrar una asignatura
// dentro del plan de estudios de un grado y año escolar.
public class CrearPlanEstudiosRequest {

    // Año escolar al que pertenece el plan.
    @NotNull(message = "El año escolar es obligatorio")
    private UUID anioEscolarId;

    // Grado al que aplica la asignatura.
    @NotNull(message = "El grado es obligatorio")
    private UUID gradoId;

    // Asignatura incluida en el plan de estudios.
    @NotNull(message = "La asignatura es obligatoria")
    private UUID asignaturaId;

    // Intensidad semanal definida para la asignatura.
    @NotNull(message = "Las horas semanales son obligatorias")
    private Integer horasSemanales;

    // Turno esperado.
    // La relación definitiva se establecerá cuando se implemente Turno.
    private UUID turnoId;

    // Observaciones adicionales.
    private String observacion;

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public UUID getGradoId() {
        return gradoId;
    }

    public void setGradoId(UUID gradoId) {
        this.gradoId = gradoId;
    }

    public UUID getAsignaturaId() {
        return asignaturaId;
    }

    public void setAsignaturaId(UUID asignaturaId) {
        this.asignaturaId = asignaturaId;
    }

    public Integer getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(Integer horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public UUID getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(UUID turnoId) {
        this.turnoId = turnoId;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
