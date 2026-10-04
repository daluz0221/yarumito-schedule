package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public class CrearFranjaHorariaRequest {

    @NotNull(message = "El turno es obligatorio")
    private UUID turnoId;

    @Min(value = 0, message = "El número de la franja no puede ser negativo")
    private int numero;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "Debe indicar si la franja corresponde a un descanso")
    private Boolean esDescanso;

    @NotNull(message = "Las horas académicas equivalentes son obligatorias")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Las horas académicas equivalentes no pueden ser negativas"
    )
    private BigDecimal horasAcademicasEquivalentes;

    @Min(value = 1, message = "El orden debe ser mayor que cero")
    private int orden;

    public UUID getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(UUID turnoId) {
        this.turnoId = turnoId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getEsDescanso() {
        return esDescanso;
    }

    public void setEsDescanso(Boolean esDescanso) {
        this.esDescanso = esDescanso;
    }

    public BigDecimal getHorasAcademicasEquivalentes() {
        return horasAcademicasEquivalentes;
    }

    public void setHorasAcademicasEquivalentes(
            BigDecimal horasAcademicasEquivalentes) {
        this.horasAcademicasEquivalentes =
                horasAcademicasEquivalentes;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
