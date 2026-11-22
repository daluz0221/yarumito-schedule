package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import java.util.UUID;

/**
 * HU-04 - Informe de Planta Docente.
 *
 * Incluye la capacidad contratada y la carga
 * academica semanal asignada al docente.
 *
 * Las horas asignadas y extras se calculan
 * para el año escolar consultado.
 *
 * No modifica la entidad Docente.
 */
public record InformePlantaDocenteDetalleResponse(

        UUID docenteId,

        String nombreCompleto,

        String areaNombramiento,

        EstadoDocente estado,

        TipoVinculacion tipoVinculacion,

        int horasSemanalesContratadas,

        long horasSemanalesAsignadas,

        long horasSemanalesExtras

) {

    /**
     * Conserva el constructor original para
     * mantener compatibilidad con los informes
     * y pruebas desarrollados anteriormente.
     */
    public InformePlantaDocenteDetalleResponse(
            UUID docenteId,
            String nombreCompleto,
            String areaNombramiento,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            int horasSemanalesContratadas) {

        this(
                docenteId,
                nombreCompleto,
                areaNombramiento,
                estado,
                tipoVinculacion,
                horasSemanalesContratadas,
                0L,
                0L
        );
    }
}
