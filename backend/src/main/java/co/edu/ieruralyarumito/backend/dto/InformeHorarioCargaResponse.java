
package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

/**
 * Resumen de carga academica del Informe de Horarios.
 *
 * Se calcula a partir de la programacion consultada
 * en el Gestor de Horarios.
 *
 * Regla institucional:
 * Una clase programada equivale a una hora
 * academica de 60 minutos.
 *
 * Este DTO no genera persistencia propia.
 */
public record InformeHorarioCargaResponse(

        UUID docenteId,
        String nombreDocente,

        UUID grupoId,
        String nombreGrupo,

        UUID asignaturaId,
        String nombreAsignatura,

        Long horasOrdinarias,
        Long horasExtras,

        Long horasJornadaRegular,
        Long horasContrajornada,

        Long totalHorasAcademicas

) {
}
