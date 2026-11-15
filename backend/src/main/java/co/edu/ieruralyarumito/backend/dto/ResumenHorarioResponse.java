package co.edu.ieruralyarumito.backend.dto;

// Contadores del tablero de construcción de horario (M-11).
// Una hora académica equivale a un bloque discreto de la rejilla.
public class ResumenHorarioResponse {

    private int asignacionesProgramadas;
    private int totalAsignaciones;
    private int horasProgramadas;
    private int totalHoras;
    private int horasPendientes;
    private int conflictos;
    private int advertencias;

    public int getAsignacionesProgramadas() {
        return asignacionesProgramadas;
    }

    public void setAsignacionesProgramadas(int asignacionesProgramadas) {
        this.asignacionesProgramadas = asignacionesProgramadas;
    }

    public int getTotalAsignaciones() {
        return totalAsignaciones;
    }

    public void setTotalAsignaciones(int totalAsignaciones) {
        this.totalAsignaciones = totalAsignaciones;
    }

    public int getHorasProgramadas() {
        return horasProgramadas;
    }

    public void setHorasProgramadas(int horasProgramadas) {
        this.horasProgramadas = horasProgramadas;
    }

    public int getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(int totalHoras) {
        this.totalHoras = totalHoras;
    }

    public int getHorasPendientes() {
        return horasPendientes;
    }

    public void setHorasPendientes(int horasPendientes) {
        this.horasPendientes = horasPendientes;
    }

    public int getConflictos() {
        return conflictos;
    }

    public void setConflictos(int conflictos) {
        this.conflictos = conflictos;
    }

    public int getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(int advertencias) {
        this.advertencias = advertencias;
    }
}
