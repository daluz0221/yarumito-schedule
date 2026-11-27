package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;

import java.time.LocalDateTime;
import java.util.UUID;

// Representa la información de un conflicto detectado
// durante la validación de un horario.
public class ConflictoResponse {

    private UUID id;
    private UUID horarioId;
    private TipoConflicto tipo;
    private SeveridadConflicto severidad;
    private String mensaje;
    private boolean resuelto;
    private LocalDateTime fechaDeteccion;
    private String dia;
    private String franja;
    private String elementoAfectado;
    private String asignatura;
    private String docente;
    private String aula;
    private String grupo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(UUID horarioId) {
        this.horarioId = horarioId;
    }

    public TipoConflicto getTipo() {
        return tipo;
    }

    public void setTipo(TipoConflicto tipo) {
        this.tipo = tipo;
    }

    public SeveridadConflicto getSeveridad() {
        return severidad;
    }

    public void setSeveridad(SeveridadConflicto severidad) {
        this.severidad = severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isResuelto() {
        return resuelto;
    }

    public void setResuelto(boolean resuelto) {
        this.resuelto = resuelto;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(LocalDateTime fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getFranja() {
        return franja;
    }

    public void setFranja(String franja) {
        this.franja = franja;
    }

    public String getElementoAfectado() {
        return elementoAfectado;
    }

    public void setElementoAfectado(String elementoAfectado) {
        this.elementoAfectado = elementoAfectado;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
