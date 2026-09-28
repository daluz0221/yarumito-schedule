package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Datos necesarios para registrar un nuevo título profesional.
public class CrearTituloProfesionalRequest {

    // Docente propietario del título profesional.
    @NotNull(message = "El docente es obligatorio")
    private UUID docenteId;

    // Nivel académico del título profesional.
    @NotNull(message = "El nivel del título es obligatorio")
    private NivelTituloProfesional nivel;

    // Nombre del título profesional.
    @NotBlank(message = "El nombre del título es obligatorio")
    private String nombreTitulo;

    // Institución que otorgó el título.
    private String institucion;

    // Año de graduación.
    private Integer anioGraduacion;

    // Referencia al archivo o soporte documental del título.
    private String archivoSoporte;

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
