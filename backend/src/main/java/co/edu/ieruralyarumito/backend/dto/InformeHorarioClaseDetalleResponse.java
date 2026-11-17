
package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Detalle de una clase programada del
 * Informe de Horarios Academicos.
 *
 * Representa informacion obtenida del Gestor
 * de Horarios, sin persistencia propia.
 *
 * Regla institucional:
 *
 * Una clase de 55 minutos equivale a
 * una hora academica para efectos de contabilizacion.
 *
 * La contrajornada no implica automaticamente
 * que una clase sea una hora extra.
 */
public record InformeHorarioClaseDetalleResponse(

        UUID horarioId,
        Integer versionHorario,
        String estadoHorario,

        UUID docenteId,
        String nombreDocente,

        UUID grupoId,
        String grado,
        String nombreGrupo,

        UUID asignaturaId,
        String nombreAsignatura,

        String diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,

        String jornada,

        UUID aulaId,
        String nombreAula,

        boolean esHoraExtra

) {
}
