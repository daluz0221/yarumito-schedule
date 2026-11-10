package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import java.time.LocalDate;
import java.util.UUID;

// Datos que la API devuelve al consultar una idoneidad docente.
public class IdoneidadResponse {

    // Identificador único de la idoneidad.
    private UUID id;

    // Identificador del docente asociado.
    private UUID docenteId;

    // Identificador del área cubierta por la idoneidad.
    private UUID areaId;

    // Identificador de la asignatura específica, cuando aplica.
    private UUID asignaturaId;

    // Tipo de idoneidad registrada.
    private TipoIdoneidad tipo;

    // Identificador del título profesional que la soporta, cuando aplica.
    private UUID tituloSoporteId;

    // Justificación asociada a la idoneidad.
    private String justificacion;

    // Fecha desde la cual la idoneidad está vigente.
    private LocalDate vigenteDesde;

    // Fecha hasta la cual la idoneidad está vigente; null indica vigencia actual.
    private LocalDate vigenteHasta;

    // Identificador del usuario que aprobó la idoneidad, cuando aplica.
    private UUID aprobadaPorId;

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

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }

    public UUID getAsignaturaId() {
        return asignaturaId;
    }

    public void setAsignaturaId(UUID asignaturaId) {
        this.asignaturaId = asignaturaId;
    }

    public TipoIdoneidad getTipo() {
        return tipo;
    }

    public void setTipo(TipoIdoneidad tipo) {
        this.tipo = tipo;
    }

    public UUID getTituloSoporteId() {
        return tituloSoporteId;
    }

    public void setTituloSoporteId(UUID tituloSoporteId) {
        this.tituloSoporteId = tituloSoporteId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }

    public void setVigenteHasta(LocalDate vigenteHasta) {
        this.vigenteHasta = vigenteHasta;
    }

    public UUID getAprobadaPorId() {
        return aprobadaPorId;
    }

    public void setAprobadaPorId(UUID aprobadaPorId) {
        this.aprobadaPorId = aprobadaPorId;
    }
}
