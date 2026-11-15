
package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta principal del Informe de Horarios Academicos.
 *
 * Agrupa las clases programadas y el resumen de carga
 * horaria obtenidos mediante consultas al Gestor
 * de Horarios.
 *
 * No es una entidad JPA y no almacena resultados
 * en una tabla independiente.
 *
 * Regla institucional:
 * Una clase equivale a una hora academica de 60 minutos.
 */
public record InformeHorariosResponse(

        UUID anioEscolarId,
        String nombreAnioEscolar,

        LocalDate fechaInicioConsulta,
        LocalDate fechaFinConsulta,

        String nombreInstitucion,
        String nombreSede,

        InformeHorariosFiltroRequest filtrosAplicados,

        List<InformeHorarioClaseDetalleResponse> clasesProgramadas,

        List<InformeHorarioCargaResponse> resumenCargaHoraria,

        Integer totalClasesProgramadas,
        Integer totalDocentes,
        Integer totalGrupos,
        Integer totalAsignaturas,

        Long horasAcademicasProgramadas,
        Long horasAcademicasJornadaRegular,
        Long horasAcademicasContrajornada

) {

    /**
     * Constructor compacto.
     *
     * Protege las listas frente a modificaciones externas.
     */
    public InformeHorariosResponse {

        clasesProgramadas = clasesProgramadas == null
                ? List.of()
                : List.copyOf(clasesProgramadas);

        resumenCargaHoraria = resumenCargaHoraria == null
                ? List.of()
                : List.copyOf(resumenCargaHoraria);
    }
}
