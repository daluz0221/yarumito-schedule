package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos que la API devuelve al consultar un grupo escolar.
public class GrupoResponse {

    private UUID id;
    private String codigo;

    private UUID gradoId;
    private int gradoNivel;
    private String gradoNombre;

    private UUID anioEscolarId;
    private int anioEscolar;

    private UUID sedeId;
    private String sedeNombre;

    private UUID directorGrupoId;
    private String directorGrupoNombre;

    private Integer cantidadEstudiantes;
    private UUID aulaFijaId;
    private boolean activo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public UUID getGradoId() {
        return gradoId;
    }

    public void setGradoId(UUID gradoId) {
        this.gradoId = gradoId;
    }

    public int getGradoNivel() {
        return gradoNivel;
    }

    public void setGradoNivel(int gradoNivel) {
        this.gradoNivel = gradoNivel;
    }

    public String getGradoNombre() {
        return gradoNombre;
    }

    public void setGradoNombre(String gradoNombre) {
        this.gradoNombre = gradoNombre;
    }

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public int getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(int anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public UUID getDirectorGrupoId() {
        return directorGrupoId;
    }

    public void setDirectorGrupoId(UUID directorGrupoId) {
        this.directorGrupoId = directorGrupoId;
    }

    public String getDirectorGrupoNombre() {
        return directorGrupoNombre;
    }

    public void setDirectorGrupoNombre(String directorGrupoNombre) {
        this.directorGrupoNombre = directorGrupoNombre;
    }

    public Integer getCantidadEstudiantes() {
        return cantidadEstudiantes;
    }

    public void setCantidadEstudiantes(Integer cantidadEstudiantes) {
        this.cantidadEstudiantes = cantidadEstudiantes;
    }

    public UUID getAulaFijaId() {
        return aulaFijaId;
    }

    public void setAulaFijaId(UUID aulaFijaId) {
        this.aulaFijaId = aulaFijaId;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
