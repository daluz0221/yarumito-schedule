package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HU-04 / SCRUM-31.
 *
 * Metadatos para identificar y dar trazabilidad
 * a los informes institucionales PDF y Excel.
 *
 * No modifica entidades ni requiere tablas nuevas.
 */
public record MetadatosInformeResponse(

        String nombreInstitucion,

        String nombreSede,

        String codigoSede,

        Map<String, String> filtrosAplicados,

        LocalDateTime fechaHoraGeneracion,

        String generadoPor

) {

    public MetadatosInformeResponse {

        filtrosAplicados = filtrosAplicados == null
                ? Map.of()
                : Map.copyOf(filtrosAplicados);
    }
}
