package co.edu.ieruralyarumito.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.UUID;

// Representa una jornada institucional para la organización del horario académico.
@Entity
@Table(name = "turno", schema = "catalogoacademico")
public class Turno {

    // Identificador único del turno.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nombre institucional del turno, por ejemplo: Mañana, Tarde o Contrajornada.
    @Column(nullable = false, length = 50)
    private String nombre;

    // Hora de inicio del turno.
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    // Hora de finalización del turno.
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // Duración en minutos de cada clase dentro del turno.
    @Column(name = "duracion_clase_minutos", nullable = false)
    private int duracionClaseMinutos;

    // Cantidad de clases académicas programadas por día.
    @Column(name = "clases_por_dia", nullable = false)
    private int clasesPorDia;

    // Cantidad de clases académicas antes del descanso institucional.
    @Column(name = "clases_antes_de_descanso", nullable = false)
    private int clasesAntesDeDescanso;

    // Duración en minutos del descanso institucional.
    @Column(name = "duracion_descanso_minutos", nullable = false)
    private int duracionDescansoMinutos;

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public int getDuracionClaseMinutos() {
        return duracionClaseMinutos;
    }

    public void setDuracionClaseMinutos(int duracionClaseMinutos) {
        this.duracionClaseMinutos = duracionClaseMinutos;
    }

    public int getClasesPorDia() {
        return clasesPorDia;
    }

    public void setClasesPorDia(int clasesPorDia) {
        this.clasesPorDia = clasesPorDia;
    }

    public int getClasesAntesDeDescanso() {
        return clasesAntesDeDescanso;
    }

    public void setClasesAntesDeDescanso(int clasesAntesDeDescanso) {
        this.clasesAntesDeDescanso = clasesAntesDeDescanso;
    }

    public int getDuracionDescansoMinutos() {
        return duracionDescansoMinutos;
    }

    public void setDuracionDescansoMinutos(int duracionDescansoMinutos) {
        this.duracionDescansoMinutos = duracionDescansoMinutos;
    }
}
