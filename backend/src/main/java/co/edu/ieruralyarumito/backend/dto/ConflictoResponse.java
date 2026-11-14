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
}
