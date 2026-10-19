package co.edu.ieruralyarumito.backend.entity;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

@Entity (name = "asignacionActividad",
    schema = "asignacionyhorario")
public class AsignacionActividad {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false)
    @JoinColumn (name = "docente_id")
    private Docente docenteId;

    @ManyToOne (optional = false)
    @JoinColumn (name = "actividad_id")
    private ActividadInstitucional actividadId;

    @Column (name = "horas_semanales", nullable = false)
    private int horasSemanales;

    @Column (name = "justificacion", nullable = false)
    private String justificacion;

    public AsignacionActividad(){}

    public UUID getId() {
        return id;
    }

    public Docente getDocenteId() {
        return docenteId;
    }

    public ActividadInstitucional getActividadId() {
        return actividadId;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setDocenteId(Docente docenteId) {
        this.docenteId = docenteId;
    }

    public void setActividadId(ActividadInstitucional actividadId) {
        this.actividadId = actividadId;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

}
