package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos que la API devuelve al consultar un área.
public class AreaResponse {

    private UUID id;
    private String nombre;
    private String codigo;
    private boolean obligatoria;
    private boolean soloMedia;
    private boolean activa;

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

    public boolean isObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }

    public boolean isSoloMedia() {
        return soloMedia;
    }

    public void setSoloMedia(boolean soloMedia) {
        this.soloMedia = soloMedia;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}