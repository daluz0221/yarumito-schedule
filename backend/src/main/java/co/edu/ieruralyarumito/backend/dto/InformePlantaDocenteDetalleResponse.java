
package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import java.util.UUID;

/**
 * Representa una fila del informe de planta docente.
 *
 * No modifica la entidad Docente ni expone datos
 * personales innecesarios para este informe.
 */
public record InformePlantaDocenteDetalleResponse(
        UUID docenteId,
        String nombreCompleto,
        String areaNombramiento,
        EstadoDocente estado,
        TipoVinculacion tipoVinculacion,
        int horasSemanalesContratadas
) {
}
