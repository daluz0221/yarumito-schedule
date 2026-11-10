package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import jakarta.validation.constraints.NotNull;

// Solicitud para modificar el estado administrativo de un docente.
public class CambiarEstadoDocenteRequest {

    @NotNull(message = "El nuevo estado del docente es obligatorio")
    private EstadoDocente nuevoEstado;

    public EstadoDocente getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoDocente nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }
}
