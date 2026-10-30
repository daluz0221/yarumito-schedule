package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

// Datos necesarios para registrar una nueva idoneidad docente.
public class CrearIdoneidadRequest {

    // Docente al que se le registra la idoneidad.
    @NotNull(message = "El docente es obligatorio")
    private UUID docenteId;

    // Área académica cubierta por la idoneidad.
    @NotNull(message = "El área es obligatoria")
    private UUID areaId;

    // Asignatura específica, cuando la idoneidad aplica a una materia concreta.
    private UUID asignaturaId;

    // Tipo de idoneidad que se registra.
    @NotNull(message = "El tipo de idoneidad es obligatorio")
    private TipoIdoneidad tipo;

    // Título profesional que soporta la idoneidad, cuando aplica.
    private UUID tituloSoporteId;

    // Justificación de la idoneidad, especialmente para casos excepcionales.
    private String justificacion;

    // Fecha desde la cual comienza la vigencia de la idoneidad.
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenteDesde;

    // Fecha hasta la cual estará vigente; puede ser null si sigue vigente.
    private LocalDate vigenteHasta;

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
}

