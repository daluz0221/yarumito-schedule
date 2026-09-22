package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos públicos de un área para catálogos y selectores.
public class AreaResponse {

    private UUID id;
    private String nombre;
    private String codigo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
