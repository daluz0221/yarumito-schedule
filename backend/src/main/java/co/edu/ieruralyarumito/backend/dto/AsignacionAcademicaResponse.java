package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

import java.util.UUID;

// Datos que la API devuelve al consultar una asignación académica.
public class AsignacionAcademicaResponse {

    private UUID id;

    private UUID anioEscolarId;
    private int anioEscolar;

    private UUID docenteId;
    private String docenteNombre;

    private UUID grupoId;
    private String grupoCodigo;

    private UUID asignaturaId;
    private String asignaturaNombre;

    private int horasSemanales;
    private int horasExtras;

    private EstadoAsignacionAcademica estado;
    private String observacion;

    private UUID asignadaPorId;
    private String asignadaPorCorreo;

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

    public UUID getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(UUID grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoCodigo() {
        return grupoCodigo;
    }

    public void setGrupoCodigo(String grupoCodigo) {
        this.grupoCodigo = grupoCodigo;
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

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public int getHorasExtras() {
        return horasExtras;
    }

    public void setHorasExtras(int horasExtras) {
        this.horasExtras = horasExtras;
    }

    public EstadoAsignacionAcademica getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsignacionAcademica estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public UUID getAsignadaPorId() {
        return asignadaPorId;
    }

    public void setAsignadaPorId(UUID asignadaPorId) {
        this.asignadaPorId = asignadaPorId;
    }

    public String getAsignadaPorCorreo() {
        return asignadaPorCorreo;
    }

    public void setAsignadaPorCorreo(String asignadaPorCorreo) {
        this.asignadaPorCorreo = asignadaPorCorreo;
    }
}
