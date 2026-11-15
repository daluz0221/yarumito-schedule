
package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Representa una asignatura dentro del informe
 * de áreas y asignaturas institucionales.
 */
public record InformeAsignaturaDetalleResponse(
        UUID asignaturaId,
        String nombre,
        String codigo,
        boolean activa,
        boolean esMediaTecnica,
        boolean exigeIdoneidadEstricta,
        boolean requiereDocenteExclusivo
) {
}
