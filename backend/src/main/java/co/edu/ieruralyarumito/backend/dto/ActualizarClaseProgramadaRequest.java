package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Datos para mover un bloque ya programado dentro de la rejilla.
// No permite cambiar la asignación académica original.
public class ActualizarClaseProgramadaRequest {

    @NotNull(message = "El día es obligatorio")
    private DiasSemana dia;

    @NotNull(message = "La franja horaria es obligatoria")
    private UUID franjaHorariaId;

    @NotNull(message = "El aula es obligatoria")
    private UUID aulaId;

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

    public UUID getAulaId() {
        return aulaId;
    }

    public void setAulaId(UUID aulaId) {
        this.aulaId = aulaId;
    }
}
