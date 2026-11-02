package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class CrearTurnoRequest {

    @NotBlank(message = "El nombre del turno es obligatorio")
    private String nombre;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalTime horaFin;

    @Min(value = 1, message = "La duración de la clase debe ser mayor que cero")
    private int duracionClaseMinutos;

    @Min(value = 1, message = "La cantidad de clases por día debe ser mayor que cero")
    private int clasesPorDia;

    @Min(value = 0, message = "Las clases antes del descanso no pueden ser negativas")
    private int clasesAntesDeDescanso;

    @Min(value = 0, message = "La duración del descanso no puede ser negativa")
    private int duracionDescansoMinutos;

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
