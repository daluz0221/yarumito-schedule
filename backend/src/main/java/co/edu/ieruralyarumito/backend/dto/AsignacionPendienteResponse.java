package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Asignación de M-10 que aún tiene horas por ubicar en la rejilla.
public class AsignacionPendienteResponse {

    private UUID asignacionAcademicaId;

    private UUID asignaturaId;
    private String asignaturaNombre;
    private String asignaturaAbreviatura;
    private String asignaturaColorUi;

    private UUID docenteId;
    private String docenteNombre;

    private int horasSemanales;
    private int horasProgramadas;
    private int horasPendientes;

    public UUID getAsignacionAcademicaId() {
        return asignacionAcademicaId;
    }

    public void setAsignacionAcademicaId(UUID asignacionAcademicaId) {
        this.asignacionAcademicaId = asignacionAcademicaId;
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

    public String getAsignaturaAbreviatura() {
        return asignaturaAbreviatura;
    }

    public void setAsignaturaAbreviatura(String asignaturaAbreviatura) {
        this.asignaturaAbreviatura = asignaturaAbreviatura;
    }

    public String getAsignaturaColorUi() {
        return asignaturaColorUi;
    }

    public void setAsignaturaColorUi(String asignaturaColorUi) {
        this.asignaturaColorUi = asignaturaColorUi;
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

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public int getHorasProgramadas() {
        return horasProgramadas;
    }

    public void setHorasProgramadas(int horasProgramadas) {
        this.horasProgramadas = horasProgramadas;
    }

    public int getHorasPendientes() {
        return horasPendientes;
    }

    public void setHorasPendientes(int horasPendientes) {
        this.horasPendientes = horasPendientes;
    }
}
