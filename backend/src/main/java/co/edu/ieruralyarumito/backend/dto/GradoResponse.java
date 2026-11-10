package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos que se exponen al consultar un grado escolar.
public class GradoResponse {

    private UUID id;
    private int nivel;
    private String nombre;
    private boolean esMedia;
    private int prioridadAsignacion;
    private int horasSemanalesEsperadas;

    public GradoResponse(
            UUID id,
            int nivel,
            String nombre,
            boolean esMedia,
            int prioridadAsignacion,
            int horasSemanalesEsperadas) {
        this.id = id;
        this.nivel = nivel;
        this.nombre = nombre;
        this.esMedia = esMedia;
        this.prioridadAsignacion = prioridadAsignacion;
        this.horasSemanalesEsperadas = horasSemanalesEsperadas;
    }

    public UUID getId() {
        return id;
    }

    public int getNivel() {
        return nivel;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEsMedia() {
        return esMedia;
    }

    public int getPrioridadAsignacion() {
        return prioridadAsignacion;
    }

    public int getHorasSemanalesEsperadas() {
        return horasSemanalesEsperadas;
    }
}
