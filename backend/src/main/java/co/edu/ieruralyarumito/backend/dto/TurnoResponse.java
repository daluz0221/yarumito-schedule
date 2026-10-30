package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalTime;
import java.util.UUID;

public class TurnoResponse {

    private UUID id;
    private String nombre;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionClaseMinutos;
    private int clasesPorDia;
    private int clasesAntesDeDescanso;
    private int duracionDescansoMinutos;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
