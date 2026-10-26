package co.edu.ieruralyarumito.backend.dto;

public class GrupoOpcionResponse {
    private UUID grupoId;
    private String codigo;

    public GrupoOpcionResponse() {}

    public GrupoOpcionResponse(UUID grupoId, String codigo) {
        this.grupoId = grupoId;
        this.codigo = codigo;
    }

    public UUID getGrupoId() {return grupoId;}
    public String getCodigo() {return codigo;}
    public void setGrupoId(UUID grupoId) {this.grupoId = grupoId;}
    public void setCodigo(String codigo) {this.codigo = codigo;}
}
