package co.edu.ieruralyarumito.backend.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public class FranjaHorariaResponse {

    private UUID id;
    private UUID turnoId;
    private String turnoNombre;
    private int numero;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean esDescanso;
    private BigDecimal horasAcademicasEquivalentes;
    private int orden;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(UUID turnoId) {
        this.turnoId = turnoId;
    }

    public String getTurnoNombre() {
        return turnoNombre;
    }

    public void setTurnoNombre(String turnoNombre) {
        this.turnoNombre = turnoNombre;
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

    public boolean isEsDescanso() {
        return esDescanso;
    }

    public void setEsDescanso(boolean esDescanso) {
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