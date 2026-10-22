package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.TurnoResponse;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de turnos institucionales.
@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;

    // Inyección de dependencias mediante constructor.
    public TurnoService(
            TurnoRepository turnoRepository,
            FranjaHorariaRepository franjaHorariaRepository) {

        this.turnoRepository = turnoRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
    }

    // Normaliza el nombre para evitar diferencias por espacios innecesarios.
    private String normalizarNombre(String nombre) {

        return nombre == null
                ? null
                : nombre.trim().replaceAll("\\s+", " ");
    }

    // Valida que no exista otro turno con nombre equivalente.
    private void validarNombreDuplicado(String nombre) {

        if (turnoRepository.existsByNombreIgnoreCase(nombre)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un turno con ese nombre");
        }
    }

    // Valida que la hora inicial sea anterior a la hora final.
    private void validarRangoHorario(
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (!horaInicio.isBefore(horaFin)) {

            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de finalización");
        }
    }

    // Valida coherencia básica de la estructura temporal del turno.
    private void validarEstructura(
            int clasesPorDia,
            int clasesAntesDeDescanso) {

        if (clasesAntesDeDescanso > clasesPorDia) {

            throw new IllegalArgumentException(
                    "Las clases antes del descanso no pueden superar las clases por día");
        }
    }

    // Comprueba que los cambios del Turno no dejen incompatibles
    // las franjas que ya fueron configuradas.
    //
    // La duración de clase corresponde al tiempo efectivo de clase.
    // En Yarumito, normalmente 55 minutos efectivos más
    // 5 minutos de descanso pedagógico equivalen a 1 hora de clase.
    private void validarCompatibilidadConFranjasExistentes(
            UUID turnoId,
            int duracionClaseMinutos,
            int duracionDescansoMinutos) {

        List<FranjaHoraria> franjas =
                franjaHorariaRepository
                        .findByTurno_IdOrderByOrdenAsc(turnoId);

        for (FranjaHoraria franja : franjas) {

            long duracionFranja =
                    Duration.between(
                                    franja.getHoraInicio(),
                                    franja.getHoraFin())
                            .toMinutes();

            if (franja.isEsDescanso()) {

                if (duracionFranja
                        != duracionDescansoMinutos) {

                    throw new IllegalArgumentException(
                            "No se puede actualizar el turno porque existen "
                                    + "franjas de descanso con una duración diferente");
                }

            } else {

                if (duracionFranja
                        != duracionClaseMinutos) {

                    throw new IllegalArgumentException(
                            "No se puede actualizar el turno porque existen "
                                    + "franjas académicas con una duración diferente");
                }
            }
        }
    }

    // Convierte la entidad Turno en el DTO devuelto por la API.
    private TurnoResponse convertirAResponse(
            Turno turno) {

        TurnoResponse response =
                new TurnoResponse();

        response.setId(
                turno.getId());

        response.setNombre(
                turno.getNombre());

        response.setHoraInicio(
                turno.getHoraInicio());

        response.setHoraFin(
                turno.getHoraFin());

        response.setDuracionClaseMinutos(
                turno.getDuracionClaseMinutos());

        response.setClasesPorDia(
                turno.getClasesPorDia());

        response.setClasesAntesDeDescanso(
                turno.getClasesAntesDeDescanso());

        response.setDuracionDescansoMinutos(
                turno.getDuracionDescansoMinutos());

        return response;
    }

    // Registra un nuevo turno institucional.
    @Transactional
    public TurnoResponse registrarTurno(
            CrearTurnoRequest request) {

        String nombreNormalizado =
                normalizarNombre(
                        request.getNombre());

        validarNombreDuplicado(
                nombreNormalizado);

        validarRangoHorario(
                request.getHoraInicio(),
                request.getHoraFin());

        validarEstructura(
                request.getClasesPorDia(),
                request.getClasesAntesDeDescanso());

        Turno turno =
                new Turno();

        turno.setNombre(
                nombreNormalizado);

        turno.setHoraInicio(
                request.getHoraInicio());

        turno.setHoraFin(
                request.getHoraFin());

        turno.setDuracionClaseMinutos(
                request.getDuracionClaseMinutos());

        turno.setClasesPorDia(
                request.getClasesPorDia());

        turno.setClasesAntesDeDescanso(
                request.getClasesAntesDeDescanso());

        turno.setDuracionDescansoMinutos(
                request.getDuracionDescansoMinutos());

        Turno turnoGuardado =
                turnoRepository.save(
                        turno);

        return convertirAResponse(
                turnoGuardado);
    }

    // Consulta un turno por su identificador.
    @Transactional(readOnly = true)
    public TurnoResponse consultarTurno(
            UUID id) {

        Turno turno =
                turnoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El turno no existe"));

        return convertirAResponse(
                turno);
    }

    // Lista los turnos registrados con paginación.
    @Transactional(readOnly = true)
    public Page<TurnoResponse> listarTurnos(
            Pageable pageable) {

        return turnoRepository
                .findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un turno existente.
    @Transactional
    public TurnoResponse actualizarTurno(
            UUID id,
            ActualizarTurnoRequest request) {

        Turno turno =
                turnoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El turno no existe"));

        String nombreNormalizado =
                normalizarNombre(
                        request.getNombre());

        if (!turno.getNombre()
                .equalsIgnoreCase(
                        nombreNormalizado)) {

            validarNombreDuplicado(
                    nombreNormalizado);
        }

        validarRangoHorario(
                request.getHoraInicio(),
                request.getHoraFin());

        validarEstructura(
                request.getClasesPorDia(),
                request.getClasesAntesDeDescanso());

        validarCompatibilidadConFranjasExistentes(
                id,
                request.getDuracionClaseMinutos(),
                request.getDuracionDescansoMinutos());

        turno.setNombre(
                nombreNormalizado);

        turno.setHoraInicio(
                request.getHoraInicio());

        turno.setHoraFin(
                request.getHoraFin());

        turno.setDuracionClaseMinutos(
                request.getDuracionClaseMinutos());

        turno.setClasesPorDia(
                request.getClasesPorDia());

        turno.setClasesAntesDeDescanso(
                request.getClasesAntesDeDescanso());

        turno.setDuracionDescansoMinutos(
                request.getDuracionDescansoMinutos());

        Turno turnoActualizado =
                turnoRepository.save(
                        turno);

        return convertirAResponse(
                turnoActualizado);
    }
}
