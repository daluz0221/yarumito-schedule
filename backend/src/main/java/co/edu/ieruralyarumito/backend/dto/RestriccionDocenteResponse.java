package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;

import java.util.UUID;

// Información de una restricción docente devuelta por la API.
public class RestriccionDocenteResponse {

    private UUID id;
    private UUID docenteId;
    private UUID anioEscolarId;
    private DiaSemana dia;
    private UUID franjaHorariaId;
    private TipoRestriccionDocente tipo;
    private String motivo;
    private byte[] documentoSoporte;
    private UUID aprobadaPorId;

    // H-04: indica si la restricción está activa o retirada.
    private boolean activo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public UUID getAprobadaPorId() {
        return aprobadaPorId;
    }

    public void setAprobadaPorId(UUID aprobadaPorId) {
        this.aprobadaPorId = aprobadaPorId;
    }

    // H-04: consulta el estado de la restricción.
    public boolean isActivo() {
        return activo;
    }

    // H-04: establece el estado de la restricción.
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
