package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

// Datos necesarios para registrar una nueva asignación académica.
public class CrearAsignacionAcademicaRequest {

    @NotNull(message = "El año escolar es obligatorio")
    private UUID anioEscolarId;

    @NotNull(message = "Debe asignar un docente válido")
    private UUID docenteId;

    @NotNull(message = "Debe asignar un grupo válido")
    private UUID grupoId;

    @NotNull(message = "Debe seleccionar una asignatura válida")
    private UUID asignaturaId;

    @NotNull(message = "La cantidad de horas semanales es obligatoria")
    @Positive(message = "La cantidad de horas semanales debe ser un número positivo")
    private Integer horasSemanales;

    // La observación es opcional.
    private String observacion;

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public UUID getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(UUID docenteId) {
        this.docenteId = docenteId;
    }

    public UUID getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(UUID grupoId) {
        this.grupoId = grupoId;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
