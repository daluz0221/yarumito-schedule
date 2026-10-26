package co.edu.ieruralyarumito.backend.dto;

public class AnioLectivoOpcionResponse {
    private UUID anioLectivoId;
    private int anioLectivo;

    public AnioLectivoOpcionResponse() {}

    public AnioLectivoOpcionResponse(UUID anioLectivoId, int anioLectivo) {
        this.anioLectivoId = anioLectivoId;
        this.anioLectivo = anioLectivo;
    }

    public UUID getAnioLectivoId() {return anioLectivoId;}
    public int getAnioLectivo() {return anioLectivo;}
    public void setAnioLectivoId(UUID anioLectivoId) {this.anioLectivoId = anioLectivoId;}
    public void setAnioLectivo(int anioLectivo) {this.anioLectivo = anioLectivo;}
}
