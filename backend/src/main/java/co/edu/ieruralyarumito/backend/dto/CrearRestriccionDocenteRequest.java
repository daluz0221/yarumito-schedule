package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Datos necesarios para registrar una restricción de disponibilidad docente.
public class CrearRestriccionDocenteRequest {

    @NotNull(message = "El docente es obligatorio")
    private UUID docenteId;

    @NotNull(message = "El año escolar es obligatorio")
    private UUID anioEscolarId;

    @NotNull(message = "El día es obligatorio")
    private DiaSemana dia;

    @NotNull(message = "La franja horaria es obligatoria")
    private UUID franjaHorariaId;

    @NotNull(message = "El tipo de restricción es obligatorio")
    private TipoRestriccionDocente tipo;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private byte[] documentoSoporte;

    public UUID getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(UUID docenteId) {
        this.docenteId = docenteId;
    }

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public DiaSemana getDia() {
        return dia;
    }

    public void setDia(DiaSemana dia) {
        this.dia = dia;
    }

    public UUID getFranjaHorariaId() {
        return franjaHorariaId;
    }

    public void setFranjaHorariaId(UUID franjaHorariaId) {
        this.franjaHorariaId = franjaHorariaId;
    }

    public TipoRestriccionDocente getTipo() {
        return tipo;
    }

    public void setTipo(TipoRestriccionDocente tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public byte[] getDocumentoSoporte() {
        return documentoSoporte;
    }

    public void setDocumentoSoporte(byte[] documentoSoporte) {
        this.documentoSoporte = documentoSoporte;
    }
}
