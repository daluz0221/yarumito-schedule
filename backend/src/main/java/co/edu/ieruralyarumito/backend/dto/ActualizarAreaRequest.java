package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos permitidos para actualizar un área.
public class ActualizarAreaRequest {

    @NotBlank(message = "El nombre del área es obligatorio")
    private String nombre;

    @NotBlank(message = "El código del área es obligatorio")
    private String codigo;

    @NotNull(message = "Debe indicar si el área es obligatoria")
    private Boolean obligatoria;

    @NotNull(message = "Debe indicar si el área aplica solo a media")
    private Boolean soloMedia;

    @NotNull(message = "Debe indicar el estado del área")
    private Boolean activa;

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

    public Boolean getObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(Boolean obligatoria) {
        this.obligatoria = obligatoria;
    }

    public Boolean getSoloMedia() {
        return soloMedia;
    }

    public void setSoloMedia(Boolean soloMedia) {
        this.soloMedia = soloMedia;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
