package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Datos permitidos para actualizar un año escolar.
public class ActualizarAnioEscolarRequest {

    @NotNull(message = "El año escolar es obligatorio")
    private Integer anio;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El estado del año escolar es obligatorio")
    private EstadoAnioEscolar estado;

    @NotNull(message = "Debe indicar si es el año escolar actual")
    private Boolean esActual;

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
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

    public Boolean getEsActual() {
        return esActual;
    }

    public void setEsActual(Boolean esActual) {
        this.esActual = esActual;
    }
}
