package co.edu.ieruralyarumito.backend.mapper;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import org.springframework.stereotype.Component;

// Convierte entre la entidad AsignacionAcademica y sus DTOs.
@Component
public class AsignacionAcademicaMapper {

    // Construye una asignación nueva forzando el estado BORRADOR.
    public AsignacionAcademica toEntity(
            CrearAsignacionAcademicaRequest request,
            AnioEscolar anioEscolar,
            Docente docente,
            Grupo grupo,
            Asignatura asignatura,
            Usuario asignadaPor) {

        AsignacionAcademica entity = new AsignacionAcademica();
        entity.setAnioEscolar(anioEscolar);
        entity.setDocente(docente);
        entity.setGrupo(grupo);
        entity.setAsignatura(asignatura);
        entity.setHorasSemanales(request.getHorasSemanales());
        entity.setHorasExtras(0);
        entity.setEstado(EstadoAsignacionAcademica.BORRADOR);
        entity.setObservacion(request.getObservacion());
        entity.setAsignadaPor(asignadaPor);

        return entity;
    }

    // Convierte la entidad en la respuesta expuesta por la API.
    public AsignacionAcademicaResponse toResponse(
            AsignacionAcademica entity) {

        AsignacionAcademicaResponse response =
                new AsignacionAcademicaResponse();

        response.setId(entity.getId());

        AnioEscolar anioEscolar = entity.getAnioEscolar();
        response.setAnioEscolarId(anioEscolar.getId());
        response.setAnioEscolar(anioEscolar.getAnio());

        Docente docente = entity.getDocente();
        response.setDocenteId(docente.getId());
        response.setDocenteNombre(
                docente.getNombres() + " " + docente.getApellidos());

        Grupo grupo = entity.getGrupo();
        response.setGrupoId(grupo.getId());
        response.setGrupoCodigo(grupo.getCodigo());

        Asignatura asignatura = entity.getAsignatura();
        response.setAsignaturaId(asignatura.getId());
        response.setAsignaturaNombre(asignatura.getNombre());

        response.setHorasSemanales(entity.getHorasSemanales());
        response.setHorasExtras(entity.getHorasExtras());
        response.setEstado(entity.getEstado());
        response.setObservacion(entity.getObservacion());

        Usuario asignadaPor = entity.getAsignadaPor();
        if (asignadaPor != null) {
            response.setAsignadaPorId(asignadaPor.getId());
            response.setAsignadaPorCorreo(asignadaPor.getCorreo());
        }

        return response;
    }
}
