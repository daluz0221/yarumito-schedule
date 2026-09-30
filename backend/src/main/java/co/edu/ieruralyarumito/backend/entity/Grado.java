package co.edu.ieruralyarumito.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

// Representa un grado escolar de sexto a undécimo.
@Entity
@Table(name = "grado", schema = "catalogoacademico")
public class Grado {

    // Identificador único del grado.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Grado escolar cursado por el estudiante: 6, 7, 8, 9, 10 u 11.
    @Column(nullable = false)
    private int nivel;

    // Nombre correspondiente al grado: SEXTO, SÉPTIMO, OCTAVO, etc.
    @Column(nullable = false)
    private String nombre;

    // Indica si el grado pertenece a educación media.
    @Column(name = "es_media", nullable = false)
    private boolean esMedia;

    // Define el orden de prioridad para la asignación académica.
    @Column(name = "prioridad_asignacion", nullable = false)
    private int prioridadAsignacion;

    // Cantidad de horas semanales esperadas para el grado.
    @Column(name = "horas_semanales_esperadas", nullable = false)
    private int horasSemanalesEsperadas;

    public UUID getId() {
        return id;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsMedia() {
        return esMedia;
    }

    public void setEsMedia(boolean esMedia) {
        this.esMedia = esMedia;
    }

    public int getPrioridadAsignacion() {
        return prioridadAsignacion;
    }

    public void setPrioridadAsignacion(int prioridadAsignacion) {
        this.prioridadAsignacion = prioridadAsignacion;
    }

    public int getHorasSemanalesEsperadas() {
        return horasSemanalesEsperadas;
    }

    public void setHorasSemanalesEsperadas(int horasSemanalesEsperadas) {
        this.horasSemanalesEsperadas = horasSemanalesEsperadas;
    }
}
