package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;

import java.time.LocalTime;
import java.util.UUID;

// Bloque ya ubicado en la rejilla del horario.
public class ClaseProgramadaResponse {

    private UUID id;

    private UUID asignacionAcademicaId;

    private UUID asignaturaId;
    private String asignaturaNombre;
    private String asignaturaAbreviatura;
    private String asignaturaColorUi;

    private UUID docenteId;
    private String docenteNombre;

    private UUID aulaId;
    private String aulaNombre;

    private DiasSemana dia;

    private UUID franjaHorariaId;
    private int franjaNumero;
    private int franjaOrden;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    private boolean esHoraExtra;
    private boolean bloqueada;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAsignacionAcademicaId() {
        return asignacionAcademicaId;
    }

    public void setAsignacionAcademicaId(UUID asignacionAcademicaId) {
        this.asignacionAcademicaId = asignacionAcademicaId;
    }

    public UUID getAsignaturaId() {
        return asignaturaId;
    }

    public void setAsignaturaId(UUID asignaturaId) {
        this.asignaturaId = asignaturaId;
    }

    public String getAsignaturaNombre() {
        return asignaturaNombre;
    }

    public void setAsignaturaNombre(String asignaturaNombre) {
        this.asignaturaNombre = asignaturaNombre;
    }

    public String getAsignaturaAbreviatura() {
        return asignaturaAbreviatura;
    }

    public void setAsignaturaAbreviatura(String asignaturaAbreviatura) {
        this.asignaturaAbreviatura = asignaturaAbreviatura;
    }

    public String getAsignaturaColorUi() {
        return asignaturaColorUi;
    }

    public void setAsignaturaColorUi(String asignaturaColorUi) {
        this.asignaturaColorUi = asignaturaColorUi;
    }

    public UUID getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(UUID docenteId) {
        this.docenteId = docenteId;
    }

    public String getDocenteNombre() {
        return docenteNombre;
    }

    public void setDocenteNombre(String docenteNombre) {
        this.docenteNombre = docenteNombre;
    }

    public UUID getAulaId() {
        return aulaId;
    }

    public void setAulaId(UUID aulaId) {
        this.aulaId = aulaId;
    }

    public String getAulaNombre() {
        return aulaNombre;
    }

    public void setAulaNombre(String aulaNombre) {
        this.aulaNombre = aulaNombre;
    }

    public DiasSemana getDia() {
        return dia;
    }

    public void setDia(DiasSemana dia) {
        this.dia = dia;
    }

    public UUID getFranjaHorariaId() {
        return franjaHorariaId;
    }

    public void setFranjaHorariaId(UUID franjaHorariaId) {
        this.franjaHorariaId = franjaHorariaId;
    }

    public int getFranjaNumero() {
        return franjaNumero;
    }

    public void setFranjaNumero(int franjaNumero) {
        this.franjaNumero = franjaNumero;
    }

    public int getFranjaOrden() {
        return franjaOrden;
    }

    public void setFranjaOrden(int franjaOrden) {
        this.franjaOrden = franjaOrden;
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

    public boolean isEsHoraExtra() {
        return esHoraExtra;
    }

    public void setEsHoraExtra(boolean esHoraExtra) {
        this.esHoraExtra = esHoraExtra;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }
}
