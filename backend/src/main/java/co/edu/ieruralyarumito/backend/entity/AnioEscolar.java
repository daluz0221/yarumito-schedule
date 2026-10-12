package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

// Representa un año lectivo dentro del Catálogo Académico.
@Entity
@Table(name = "anio_escolar", schema = "catalogoacademico")
public class AnioEscolar {

    // Identificador único del año escolar.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Año lectivo que representa el registro.
    @Column(nullable = false)
    private int anio;

    // Fecha de inicio del año escolar.
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Fecha de finalización del año escolar.
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Estado actual del año escolar.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAnioEscolar estado;

    // Indica si corresponde al año escolar actualmente en uso.
    @Column(name = "es_actual", nullable = false)
    private boolean esActual;

    public UUID getId() {
        return id;
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
