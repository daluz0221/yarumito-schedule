package co.edu.ieruralyarumito.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

// Representa una franja horaria perteneciente a un turno institucional.
@Entity
@Table(name = "franja_horaria", schema = "catalogoacademico")
public class FranjaHoraria {

    // Identificador único de la franja horaria.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Turno institucional al que pertenece la franja.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    // Número correlativo de la franja académica.
    // Para descansos puede utilizarse el valor 0.
    @Column(nullable = false)
    private int numero;

    // Hora de inicio de la franja.
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    // Hora de finalización de la franja.
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // Indica si la franja corresponde a un descanso institucional.
    @Column(name = "es_descanso", nullable = false)
    private boolean esDescanso;

    // Equivalencia de la franja en horas académicas.
    @Column(
            name = "horas_academicas_equivalentes",
            nullable = false,
            precision = 6,
            scale = 2
    )
    private BigDecimal horasAcademicasEquivalentes;

    // Define la posición visual de la franja dentro del turno.
    @Column(nullable = false)
    private int orden;

    public UUID getId() {
        return id;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
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
