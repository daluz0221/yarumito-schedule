package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

// Datos necesarios para actualizar un grupo escolar.
public class ActualizarGrupoRequest {

    // Código identificador del grupo.
    @NotBlank(message = "El código del grupo es obligatorio")
    private String codigo;

    // Grado al que pertenece el grupo.
    @NotNull(message = "El grado es obligatorio")
    private UUID gradoId;

    // Año escolar al que pertenece el grupo.
    @NotNull(message = "El año escolar es obligatorio")
    private UUID anioEscolarId;

    // Sede en la que funciona el grupo.
    @NotNull(message = "La sede es obligatoria")
    private UUID sedeId;

    // Docente director del grupo; puede permanecer sin asignar.
    private UUID directorGrupoId;

    // Cantidad de estudiantes matriculados.
    @PositiveOrZero(message = "La cantidad de estudiantes no puede ser negativa")
    private Integer cantidadEstudiantes;

    // Aula habitual del grupo; puede permanecer sin asignar.
    private UUID aulaFijaId;

    // Indica si el grupo se encuentra activo.
    @NotNull(message = "Debe indicar si el grupo está activo")
    private Boolean activo;

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

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public UUID getDirectorGrupoId() {
        return directorGrupoId;
    }

    public void setDirectorGrupoId(UUID directorGrupoId) {
        this.directorGrupoId = directorGrupoId;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
