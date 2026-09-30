package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos que la API devuelve al consultar
// un registro del plan de estudios.
public class PlanEstudiosResponse {

    private UUID id;

    private UUID anioEscolarId;
    private int anioEscolar;

    private UUID gradoId;
    private int gradoNivel;
    private String gradoNombre;

    private UUID asignaturaId;
    private String asignaturaNombre;
    private String asignaturaCodigo;

    private int horasSemanales;

    private UUID turnoId;

    private String observacion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public int getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(int anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public UUID getGradoId() {
        return gradoId;
    }

    public void setGradoId(UUID gradoId) {
        this.gradoId = gradoId;
    }

    public int getGradoNivel() {
        return gradoNivel;
    }

    public void setGradoNivel(int gradoNivel) {
        this.gradoNivel = gradoNivel;
    }

    public String getGradoNombre() {
        return gradoNombre;
    }

    public void setGradoNombre(String gradoNombre) {
        this.gradoNombre = gradoNombre;
    }

    public UUID getAsignaturaId() {
        return asignaturaId;
    }

    public void setAsignaturaId(UUID asignaturaId) {
        this.asignaturaId = asignaturaId;
    }

    public String getAsignaturaNombre() {
        return asignaturaNombre;
    }

    public void setAsignaturaNombre(String asignaturaNombre) {
        this.asignaturaNombre = asignaturaNombre;
    }

    public String getAsignaturaCodigo() {
        return asignaturaCodigo;
    }

    public void setAsignaturaCodigo(String asignaturaCodigo) {
        this.asignaturaCodigo = asignaturaCodigo;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
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
