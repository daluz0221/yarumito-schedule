package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos permitidos para actualizar una idoneidad existente.
public class ActualizarIdoneidadRequest {

    // Nuevo título profesional que soporta la idoneidad, cuando aplica.
    private UUID tituloSoporteId;

    // Justificación administrativa de la idoneidad.
    private String justificacion;

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
}