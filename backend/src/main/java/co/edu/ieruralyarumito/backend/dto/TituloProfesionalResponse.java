package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;

import java.util.UUID;

// Datos que la API devuelve al consultar un título profesional.
public class TituloProfesionalResponse {

    // Identificador único del título profesional.
    private UUID id;

    // Identificador del docente propietario del título.
    private UUID docenteId;

    // Nivel académico del título profesional.
    private NivelTituloProfesional nivel;

    // Nombre del título profesional.
    private String nombreTitulo;

    // Institución que otorgó el título.
    private String institucion;

    // Año de graduación.
    private Integer anioGraduacion;

    // Referencia al archivo o soporte documental del título.
    private String archivoSoporte;

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

    public NivelTituloProfesional getNivel() {
        return nivel;
    }

    public void setNivel(NivelTituloProfesional nivel) {
        this.nivel = nivel;
    }

    public String getNombreTitulo() {
        return nombreTitulo;
    }

    public void setNombreTitulo(String nombreTitulo) {
        this.nombreTitulo = nombreTitulo;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public Integer getAnioGraduacion() {
        return anioGraduacion;
    }

    public void setAnioGraduacion(Integer anioGraduacion) {
        this.anioGraduacion = anioGraduacion;
    }

    public String getArchivoSoporte() {
        return archivoSoporte;
    }

    public void setArchivoSoporte(String archivoSoporte) {
        this.archivoSoporte = archivoSoporte;
    }
}
