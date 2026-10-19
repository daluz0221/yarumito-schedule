package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;

import java.util.UUID;

public class AulaResponse {

    private UUID id;
    private UUID sedeId;
    private String sedeNombre;
    private String nombre;
    private TipoAulaRequerida tipo;
    private Integer capacidad;
    private boolean activa;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
