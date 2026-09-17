package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

// Datos permitidos para actualizar una idoneidad docente.
public class ActualizarIdoneidadRequest {

    // Área académica cubierta por la idoneidad.
    @NotNull(message = "El área es obligatoria")
    private UUID areaId;

    // Asignatura específica, cuando aplica.
    private UUID asignaturaId;

    // Tipo de idoneidad.
    @NotNull(message = "El tipo de idoneidad es obligatorio")
    private TipoIdoneidad tipo;

    // Título profesional que soporta la idoneidad, cuando aplica.
    private UUID tituloSoporteId;

    // Justificación asociada a la idoneidad.
    private String justificacion;

    // Fecha desde la cual comienza la vigencia.
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenteDesde;

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
}
