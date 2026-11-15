
package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Idoneidad académica registrada.
 *
 * vigenteEnFecha se calcula usando la fecha
 * de referencia del informe.
 */
public record InformeIdoneidadDetalleResponse(
        UUID idoneidadId,
        UUID areaId,
        String nombreArea,
        UUID asignaturaId,
        String nombreAsignatura,
        TipoIdoneidad tipo,
        UUID tituloSoporteId,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta,
        boolean vigenteEnFecha
) {
}
