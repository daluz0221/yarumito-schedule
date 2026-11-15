
package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros combinables para la consulta del Informe de
 * Horarios Academicos de la sede principal.
 *
 * Los identificadores corresponden a registros existentes
 * en la base de datos.
 *
 * Los valores null representan filtros no aplicados.
 *
 * Este DTO no almacena horarios ni resultados.
 */
public record InformeHorariosFiltroRequest(

        UUID anioEscolarId,

        UUID periodoAcademicoId,

        LocalDate fechaInicio,

        LocalDate fechaFin,

        UUID docenteId,

        UUID gradoId,

        UUID grupoId,

        UUID asignaturaId,

        String jornada,

        String dia,

        String estado

) {

    /**
     * Comprueba exclusivamente la coherencia
     * cronologica del rango solicitado.
     *
     * La existencia de los registros, las jornadas,
     * los estados y la vigencia de cada version del
     * horario se validaran en la capa de servicio.
     */
    public boolean tieneRangoFechasValido() {

        if (fechaInicio == null && fechaFin == null) {
            return true;
        }

        if (fechaInicio == null || fechaFin == null) {
            return false;
        }

        return !fechaInicio.isAfter(fechaFin);
    }

    /**
     * Indica si se solicito un rango personalizado.
     */
    public boolean tieneRangoPersonalizado() {
        return fechaInicio != null && fechaFin != null;
    }
}
