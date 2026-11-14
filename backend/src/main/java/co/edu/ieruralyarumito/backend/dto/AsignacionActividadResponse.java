package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoActividadInstitucional;

import java.util.UUID;

// Datos que la API devuelve al consultar una actividad asignada.
public class AsignacionActividadResponse {

    private UUID id;

    private UUID docenteId;
    private String docenteNombre;

    private UUID actividadId;
    private String actividadNombre;
    private TipoActividadInstitucional tipoActividad;

    private int horasSemanales;
    private String justificacion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(UUID docenteId) {
        this.docenteId = docenteId;
    }

    public String getDocenteNombre() {
        return docenteNombre;
    }

    public void setDocenteNombre(String docenteNombre) {
        this.docenteNombre = docenteNombre;
    }

    public UUID getActividadId() {
        return actividadId;
    }

    public void setActividadId(UUID actividadId) {
        this.actividadId = actividadId;
    }

    public String getActividadNombre() {
        return actividadNombre;
    }

    public void setActividadNombre(String actividadNombre) {
        this.actividadNombre = actividadNombre;
    }

    public TipoActividadInstitucional getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(TipoActividadInstitucional tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
}
