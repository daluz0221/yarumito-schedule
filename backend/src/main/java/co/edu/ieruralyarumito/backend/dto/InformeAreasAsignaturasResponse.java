
package co.edu.ieruralyarumito.backend.dto;

import java.util.List;

/**
 * HU-04 / SCRUM-31.
 *
 * Resultado consolidado del informe institucional
 * de áreas y asignaturas.
 */
public record InformeAreasAsignaturasResponse(
        long cantidadAreas,
        long cantidadAsignaturas,
        List<InformeAreaDetalleResponse> areas
) {

    public InformeAreasAsignaturasResponse {
        areas = List.copyOf(areas);
    }
}
