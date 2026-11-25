package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;

import java.time.LocalDateTime;
import java.util.UUID;

// Resultado de la publicación de un horario.
public class PublicarHorarioResponse {

    private UUID horarioId;
    private EstadoHorario estado;
    private LocalDateTime fechaPublicacion;
    private int version;

    public UUID getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(UUID horarioId) {
        this.horarioId = horarioId;
    }

    public EstadoHorario getEstado() {
        return estado;
    }

    public void setEstado(EstadoHorario estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
