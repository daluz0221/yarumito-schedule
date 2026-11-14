
package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import java.util.UUID;

/**
 * Título profesional registrado para un docente.
 */
public record InformeTituloProfesionalDetalleResponse(
        UUID tituloId,
        String nombreTitulo,
        NivelTituloProfesional nivel,
        String institucion,
        Integer anioGraduacion)

{
}
