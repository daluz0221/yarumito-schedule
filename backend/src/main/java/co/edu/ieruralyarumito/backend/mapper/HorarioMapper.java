package co.edu.ieruralyarumito.backend.mapper;

import co.edu.ieruralyarumito.backend.dto.AsignacionPendienteResponse;
import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import org.springframework.stereotype.Component;

// Convierte las entidades del horario en los DTOs que consume el frontend.
@Component
public class HorarioMapper {

    // Convierte una franja horaria en su representación de rejilla.
    public FranjaHorariaResponse toFranjaResponse(FranjaHoraria franja) {

        FranjaHorariaResponse response = new FranjaHorariaResponse();
        response.setId(franja.getId());

        if (franja.getTurno() != null) {
            response.setTurnoId(franja.getTurno().getId());
            response.setTurnoNombre(franja.getTurno().getNombre());
        }

        response.setNumero(franja.getNumero());
        response.setHoraInicio(franja.getHoraInicio());
        response.setHoraFin(franja.getHoraFin());
        response.setEsDescanso(franja.isEsDescanso());
        response.setHorasAcademicasEquivalentes(
                franja.getHorasAcademicasEquivalentes());
        response.setOrden(franja.getOrden());

        return response;
    }

    // Convierte un bloque programado en su representación de rejilla.
    public ClaseProgramadaResponse toClaseResponse(ClaseProgramada clase) {

        ClaseProgramadaResponse response = new ClaseProgramadaResponse();
        response.setId(clase.getId());

        AsignacionAcademica asignacion = clase.getAsignacionAcademicaId();
        response.setAsignacionAcademicaId(asignacion.getId());

        Asignatura asignatura = asignacion.getAsignatura();
        response.setAsignaturaId(asignatura.getId());
        response.setAsignaturaNombre(asignatura.getNombre());
        response.setAsignaturaAbreviatura(asignatura.getAbreviatura());
        response.setAsignaturaColorUi(asignatura.getColorUi());

        Docente docente = asignacion.getDocente();
        response.setDocenteId(docente.getId());
        response.setDocenteNombre(
                docente.getNombres() + " " + docente.getApellidos());

        Aula aula = clase.getAulaId();
        response.setAulaId(aula.getId());
        response.setAulaNombre(aula.getNombre());

        response.setDia(clase.getDia());

        FranjaHoraria franja = clase.getFranjaHorariaId();
        response.setFranjaHorariaId(franja.getId());
        response.setFranjaNumero(franja.getNumero());
        response.setFranjaOrden(franja.getOrden());
        response.setHoraInicio(franja.getHoraInicio());
        response.setHoraFin(franja.getHoraFin());

        response.setEsHoraExtra(clase.isEsHoraExtra());
        response.setBloqueada(clase.isBloqueada());

        return response;
    }

    // Convierte una asignación de M-10 en una fila del panel de pendientes.
    // Una hora programada equivale a un bloque de la rejilla.
    public AsignacionPendienteResponse toPendienteResponse(
            AsignacionAcademica asignacion,
            int horasProgramadas) {

        AsignacionPendienteResponse response =
                new AsignacionPendienteResponse();

        response.setAsignacionAcademicaId(asignacion.getId());

        Asignatura asignatura = asignacion.getAsignatura();
        response.setAsignaturaId(asignatura.getId());
        response.setAsignaturaNombre(asignatura.getNombre());
        response.setAsignaturaAbreviatura(asignatura.getAbreviatura());
        response.setAsignaturaColorUi(asignatura.getColorUi());

        Docente docente = asignacion.getDocente();
        response.setDocenteId(docente.getId());
        response.setDocenteNombre(
                docente.getNombres() + " " + docente.getApellidos());

        int horasSemanales = asignacion.getHorasSemanales();
        response.setHorasSemanales(horasSemanales);
        response.setHorasProgramadas(horasProgramadas);
        response.setHorasPendientes(
                Math.max(0, horasSemanales - horasProgramadas));

        return response;
    }
}
