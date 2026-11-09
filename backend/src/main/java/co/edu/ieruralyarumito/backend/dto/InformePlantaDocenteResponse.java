
package co.edu.ieruralyarumito.backend.dto;

import java.util.List;

/**
 * Resultado consolidado del informe de planta docente.
 *
 * Los totales corresponden a todos los registros
 * filtrados y no solamente a una página.
 */
public record InformePlantaDocenteResponse(
        long cantidadDocentes,
        long totalHorasSemanalesContratadas,
        List<InformePlantaDocenteDetalleResponse> docentes
) {

    public InformePlantaDocenteResponse {
        docentes = List.copyOf(docentes);
    }
}
