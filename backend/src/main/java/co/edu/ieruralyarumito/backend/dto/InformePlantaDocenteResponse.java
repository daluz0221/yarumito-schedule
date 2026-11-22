package co.edu.ieruralyarumito.backend.dto;

import java.util.List;
import java.util.UUID;

/**
 * HU-04 - Informe de Planta Docente.
 *
 * Consolida la capacidad contratada y la carga
 * academica semanal de los docentes.
 *
 * La carga asignada corresponde al año escolar
 * seleccionado y no mezcla periodos diferentes.
 *
 * No modifica datos ni genera persistencia.
 */
public record InformePlantaDocenteResponse(

        long cantidadDocentes,

        long totalHorasSemanalesContratadas,

        List<InformePlantaDocenteDetalleResponse> docentes,

        UUID anioEscolarId,

        Integer anioEscolar,

        long totalHorasSemanalesAsignadas,

        long totalHorasSemanalesExtras

) {

    /**
     * Conserva la compatibilidad con el constructor
     * utilizado por los informes y pruebas anteriores.
     */
    public InformePlantaDocenteResponse(
            long cantidadDocentes,
            long totalHorasSemanalesContratadas,
            List<InformePlantaDocenteDetalleResponse> docentes) {

        this(
                cantidadDocentes,
                totalHorasSemanalesContratadas,
                docentes,
                null,
                null,
                0L,
                0L
        );
    }

    /**
     * Protege la lista de docentes contra
     * modificaciones externas.
     */
    public InformePlantaDocenteResponse {
        docentes = List.copyOf(docentes);
    }
}
