package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos permitidos para modificar una restricción docente existente.
public class ActualizarRestriccionDocenteRequest {

    @NotNull(message = "El tipo de restricción es obligatorio")
    private TipoRestriccionDocente tipo;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private byte[] documentoSoporte;

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
