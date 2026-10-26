package co.edu.ieruralyarumito.backend.dto;

public class AsignacionAcademicaFiltrosResponse {

    private UUID anioLectivoId;
    private int anio;

    private UUID grupoId;
    private String codigoGrupo;

    public AsignacionAcademicaFiltrosResponse(){}

    public AsignacionAcademicaFiltrosResponse(UUID anioLectivoId, 
                                              int anio, 
                                              UUID grupoId, 
                                              String codigoGrupo) {
        this.anioLectivoId = anioLectivoId;
        this.anio = anio;
        this.grupoId = grupoId;
        this.codigoGrupo = codigoGrupo;
    }

    public UUID getAnioLectivoId() {
        return anioLectivoId;
    }

    public void setAnioLectivoId(UUID anioLectivoId) {
        this.anioLectivoId = anioLectivoId;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public UUID getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(UUID grupoId) {
        this.grupoId = grupoId;
    }

    public String getCodigoGrupo() {
        return codigoGrupo;
    }

    public void setCodigoGrupo(String codigoGrupo) {
        this.codigoGrupo = codigoGrupo;
    }

}
