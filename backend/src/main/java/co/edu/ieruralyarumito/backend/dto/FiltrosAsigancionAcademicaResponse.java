package co.edu.ieruralyarumito.backend.dto;
import java.util.List;

public class FiltrosAsigancionAcademicaResponse {
    private List<AnioLectivoOpcionResponse> aniosLectivos;
    private List<GrupoOpcionResponse> grupos;

    public FiltrosAsigancionAcademicaResponse() {}

    public FiltrosAsigancionAcademicaResponse(List<AnioLectivoOpcionResponse> aniosLectivos, List<GrupoOpcionResponse> grupos) {
        this.aniosLectivos = aniosLectivos;
        this.grupos = grupos;
    }

    public List<AnioLectivoOpcionResponse> getAniosLectivos() {return aniosLectivos;}
    public List<GrupoOpcionResponse> getGrupos() {return grupos;}
    public void setAniosLectivos(List<AnioLectivoOpcionResponse> aniosLectivos) {this.aniosLectivos = aniosLectivos;}
    public void setGrupos(List<GrupoOpcionResponse> grupos) {this.grupos = grupos;}
}
