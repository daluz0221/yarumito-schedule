package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Representa la información de un parámetro institucional devuelta por la API.
public class ParametroInstitucionalResponse {

    private UUID id;
    private UUID anioEscolarId;
    private String clave;
    private String valor;
    private String tipoDato;
    private String descripcion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
