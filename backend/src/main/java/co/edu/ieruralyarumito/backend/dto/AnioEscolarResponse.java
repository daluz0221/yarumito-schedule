package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;

import java.time.LocalDate;
import java.util.UUID;

// Datos que la API devuelve al consultar un año escolar.
public class AnioEscolarResponse {

    private UUID id;
    private int anio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAnioEscolar estado;
    private boolean esActual;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoAnioEscolar getEstado() {
        return estado;
    }

    public void setEstado(EstadoAnioEscolar estado) {
        this.estado = estado;
    }

    public boolean isEsActual() {
        return esActual;
    }

    public void setEsActual(boolean esActual) {
        this.esActual = esActual;
    }
}
