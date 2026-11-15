
package co.edu.ieruralyarumito.backend.dto;

import java.util.List;
import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Representa un área académica y las asignaturas
 * que pertenecen a ella.
 */
public record InformeAreaDetalleResponse(
        UUID areaId,
        String nombre,
        String codigo,
        boolean activa,
        boolean obligatoria,
        boolean soloMedia,
        long cantidadAsignaturas,
        List<InformeAsignaturaDetalleResponse> asignaturas
) {

    public InformeAreaDetalleResponse {
        asignaturas = List.copyOf(asignaturas);
    }
}
