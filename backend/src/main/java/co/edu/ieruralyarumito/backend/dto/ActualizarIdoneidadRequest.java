package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

// Datos editables de una idoneidad existente.
public class ActualizarIdoneidadRequest {

    // Nueva área académica cubierta por la idoneidad.
    @NotNull(message = "El área es obligatoria")
    private UUID areaId;

    // Nueva asignatura específica; null indica que aplica a toda el área.
    private UUID asignaturaId;

    // Nuevo tipo de idoneidad.
    @NotNull(message = "El tipo de idoneidad es obligatorio")
    private TipoIdoneidad tipo;

    // Nuevo título profesional que soporta la idoneidad, cuando aplica.
    private UUID tituloSoporteId;

    // Justificación administrativa de la idoneidad.
    private String justificacion;

    // Nueva fecha de inicio de vigencia.
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenteDesde;

    // Nueva fecha de finalización; null indica que no tiene fecha de cierre.
    private LocalDate vigenteHasta;

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
}