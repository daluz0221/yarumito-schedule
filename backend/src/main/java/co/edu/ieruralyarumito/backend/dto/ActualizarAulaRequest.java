package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ActualizarAulaRequest {

    @NotNull(message = "La sede es obligatoria")
    private UUID sedeId;

    @NotBlank(message = "El nombre del aula es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de aula es obligatorio")
    private TipoAulaRequerida tipo;

    // Puede permanecer null cuando la institución
    // no tenga una capacidad formal registrada.
    private Integer capacidad;

    @NotNull(message = "Debe indicar el estado del aula")
    private Boolean activa;

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoAulaRequerida getTipo() {
        return tipo;
    }

    public void setTipo(TipoAulaRequerida tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
