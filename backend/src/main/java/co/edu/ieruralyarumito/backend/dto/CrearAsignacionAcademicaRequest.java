package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
                                 
public class CrearAsignacionAcademicaRequest {

    @NotNull (message = "El año escolar es obligatorio")
    private UUID anioEscolarId;

    @NotNull (message = "Debe asignar un docente valido")
    private UUID docenteId;

    @NotNull (message = "Debe asignar un grupo valido")
    private UUID grupoId;

    @NotNull (message = "Debe seleccionar una asignatura valida")
    private UUID asignaturaId;
    
    @NotNull (message = "El tipo de idoneidad es obligatorio")
    private TipoIdoneidad tipoIdoneidad;

    @Positive (message = "La cantidad de horas semanales debe ser un número positivo")
    private int horasSemanales;

    @NotNull
    private String observacion;

}
