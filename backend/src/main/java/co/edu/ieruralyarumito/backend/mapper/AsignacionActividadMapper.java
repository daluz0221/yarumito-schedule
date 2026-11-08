package co.edu.ieruralyarumito.backend.mapper;

import co.edu.ieruralyarumito.backend.dto.AsignacionActividadResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionActividadRequest;
import co.edu.ieruralyarumito.backend.entity.ActividadInstitucional;
import co.edu.ieruralyarumito.backend.entity.AsignacionActividad;
import co.edu.ieruralyarumito.backend.entity.Docente;
import org.springframework.stereotype.Component;

// Convierte entre la entidad AsignacionActividad y sus DTOs.
@Component
public class AsignacionActividadMapper {

    // Construye la asignación de una actividad a un docente.
    public AsignacionActividad toEntity(
            CrearAsignacionActividadRequest request,
            Docente docente,
            ActividadInstitucional actividad) {

        AsignacionActividad entity = new AsignacionActividad();
        entity.setDocenteId(docente);
        entity.setActividadId(actividad);
        entity.setHorasSemanales(request.getHorasSemanales());
        entity.setJustificacion(request.getJustificacion());

        return entity;
    }

    // Convierte la entidad en la respuesta expuesta por la API.
    public AsignacionActividadResponse toResponse(
            AsignacionActividad entity) {

        AsignacionActividadResponse response =
                new AsignacionActividadResponse();

        response.setId(entity.getId());

        Docente docente = entity.getDocenteId();
        response.setDocenteId(docente.getId());
        response.setDocenteNombre(
                docente.getNombres() + " " + docente.getApellidos());

        ActividadInstitucional actividad = entity.getActividadId();
        response.setActividadId(actividad.getId());
        response.setActividadNombre(actividad.getNombre());
        response.setTipoActividad(actividad.getTipo());

        response.setHorasSemanales(entity.getHorasSemanales());
        response.setJustificacion(entity.getJustificacion());

        return response;
    }
}
