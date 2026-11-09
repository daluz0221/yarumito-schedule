package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalTime;
import java.util.List;

/**
 * HU-04 / SCRUM-31.
 *
 * Representa una posicion del horario semanal
 * para presentacion en PDF o Excel.
 *
 * Cada celda corresponde a un dia y una franja
 * de tiempo determinada.
 *
 * Puede contener varias clases cuando la consulta
 * abarca varios docentes o grupos.
 *
 * No es una entidad JPA y no genera persistencia.
 */
public record InformeHorarioCeldaSemanalResponse(

        String diaSemana,

        LocalTime horaInicio,

        LocalTime horaFin,

        String jornada,

        List<InformeHorarioClaseDetalleResponse> clases

) {

    /**
     * Protege la lista de clases frente
     * a modificaciones externas.
     */
    public InformeHorarioCeldaSemanalResponse {

        clases = clases == null
                ? List.of()
                : List.copyOf(clases);
    }

    /**
     * Una celda sin clases representa un espacio
     * sin programacion dentro de la cuadricula.
     *
     * No significa necesariamente un error
     * o incumplimiento academico.
     */
    public boolean estaLibre() {
        return clases.isEmpty();
    }

    /**
     * Informa cuando una celda contiene mas de
     * una clase.
     *
     * Esto no constituye automaticamente un
     * conflicto: pueden ser clases de distintos
     * grupos, docentes o aulas.
     */
    public boolean tieneVariasClases() {
        return clases.size() > 1;
    }
}
