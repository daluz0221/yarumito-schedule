package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarFranjaHorariaRequest;
import co.edu.ieruralyarumito.backend.dto.CrearFranjaHorariaRequest;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de franjas horarias.
@Service
public class FranjaHorariaService {

    private final FranjaHorariaRepository franjaHorariaRepository;
    private final TurnoRepository turnoRepository;

    public FranjaHorariaService(
            FranjaHorariaRepository franjaHorariaRepository,
            TurnoRepository turnoRepository) {

        this.franjaHorariaRepository = franjaHorariaRepository;
        this.turnoRepository = turnoRepository;
    }

    // Obtiene el turno asociado o rechaza la operación si no existe.
    private Turno obtenerTurno(UUID turnoId) {

        return turnoRepository.findById(turnoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El turno no existe"));
    }

    // Valida la semántica del número según el tipo de franja.
    private void validarNumero(
            int numero,
            boolean esDescanso) {

        if (esDescanso && numero != 0) {
            throw new IllegalArgumentException(
                    "Una franja de descanso debe tener número 0");
        }

        if (!esDescanso && numero <= 0) {
            throw new IllegalArgumentException(
                    "Una franja académica debe tener un número mayor que cero");
        }
    }

    // Valida el peso académico de la franja.
    private void validarHorasAcademicasEquivalentes(
            BigDecimal horasAcademicasEquivalentes,
            boolean esDescanso) {

        if (esDescanso) {

            if (horasAcademicasEquivalentes.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                        "Una franja de descanso debe tener 0 horas académicas equivalentes");
            }

            return;
        }

        if (horasAcademicasEquivalentes.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Una franja académica debe tener una equivalencia académica mayor que cero");
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

    // Valida la duración física según la configuración del turno.
    private void validarDuracion(
            Turno turno,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean esDescanso) {

        long duracionMinutos =
                Duration.between(
                        horaInicio,
                        horaFin
                ).toMinutes();

        int duracionEsperada =
                esDescanso
                        ? turno.getDuracionDescansoMinutos()
                        : turno.getDuracionClaseMinutos();

        if (duracionMinutos != duracionEsperada) {

            if (esDescanso) {
                throw new IllegalArgumentException(
                        "La duración del descanso debe coincidir con la configurada en el turno");
            }

            throw new IllegalArgumentException(
                    "La duración de la franja académica debe coincidir con la configurada en el turno");
        }
    }

    // Valida número académico duplicado al crear.
    // Solo aplica a números mayores que cero.
    private void validarNumeroDuplicadoAlCrear(
            UUID turnoId,
            int numero) {

        if (numero > 0
                && franjaHorariaRepository
                .existsByTurno_IdAndNumero(
                        turnoId,
                        numero)) {

            throw new RecursoDuplicadoException(
                    "Ya existe una franja académica con ese número en el turno");
        }
    }

    // Valida número académico duplicado al actualizar,
    // excluyendo la franja actual.
    private void validarNumeroDuplicadoAlActualizar(
            UUID turnoId,
            int numero,
            UUID franjaId) {

        if (numero > 0
                && franjaHorariaRepository
                .existsByTurno_IdAndNumeroAndIdNot(
                        turnoId,
                        numero,
                        franjaId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe una franja académica con ese número en el turno");
        }
    }

    // Valida orden duplicado al crear.
    private void validarOrdenDuplicadoAlCrear(
            UUID turnoId,
            int orden) {

        if (franjaHorariaRepository
                .existsByTurno_IdAndOrden(
                        turnoId,
                        orden)) {

            throw new RecursoDuplicadoException(
                    "Ya existe una franja con ese orden en el turno");
        }
    }

    // Valida orden duplicado al actualizar,
    // excluyendo la franja actual.
    private void validarOrdenDuplicadoAlActualizar(
            UUID turnoId,
            int orden,
            UUID franjaId) {

        if (franjaHorariaRepository
                .existsByTurno_IdAndOrdenAndIdNot(
                        turnoId,
                        orden,
                        franjaId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe una franja con ese orden en el turno");
        }
    }

    // Valida que la franja no se cruce temporalmente
    // con otra del mismo turno.
    private void validarSolapamiento(
            UUID turnoId,
            LocalTime horaInicio,
            LocalTime horaFin,
            UUID franjaActualId) {

        List<FranjaHoraria> franjas =
                franjaHorariaRepository
                        .findByTurno_IdOrderByOrdenAsc(turnoId);

        for (FranjaHoraria existente : franjas) {

            // En actualización, no se compara la franja consigo misma.
            if (franjaActualId != null
                    && existente.getId() != null
                    && existente.getId().equals(franjaActualId)) {
                continue;
            }

            boolean existeSolapamiento =
                    horaInicio.isBefore(existente.getHoraFin())
                            && horaFin.isAfter(existente.getHoraInicio());

            if (existeSolapamiento) {
                throw new IllegalArgumentException(
                        "La franja horaria se solapa con otra franja del mismo turno");
            }
        }
    }

    // Convierte la entidad en el DTO devuelto por la API.
    private FranjaHorariaResponse convertirAResponse(
            FranjaHoraria franja,
            UUID turnoId) {

        FranjaHorariaResponse response =
                new FranjaHorariaResponse();

        response.setId(franja.getId());
        response.setTurnoId(turnoId);
        response.setTurnoNombre(
                franja.getTurno().getNombre());
        response.setNumero(
                franja.getNumero());
        response.setHoraInicio(
                franja.getHoraInicio());
        response.setHoraFin(
                franja.getHoraFin());
        response.setEsDescanso(
                franja.isEsDescanso());
        response.setHorasAcademicasEquivalentes(
                franja.getHorasAcademicasEquivalentes());
        response.setOrden(
                franja.getOrden());

        return response;
    }

    // Registra una nueva franja horaria.
    @Transactional
    public FranjaHorariaResponse registrarFranjaHoraria(
            CrearFranjaHorariaRequest request) {

        Turno turno =
                obtenerTurno(request.getTurnoId());

        boolean esDescanso =
                request.getEsDescanso();

        validarNumero(
                request.getNumero(),
                esDescanso);

        validarHorasAcademicasEquivalentes(
                request.getHorasAcademicasEquivalentes(),
                esDescanso);

        validarRangoHorario(
                request.getHoraInicio(),
                request.getHoraFin());

        validarDuracion(
                turno,
                request.getHoraInicio(),
                request.getHoraFin(),
                esDescanso);

        validarNumeroDuplicadoAlCrear(
                request.getTurnoId(),
                request.getNumero());

        validarOrdenDuplicadoAlCrear(
                request.getTurnoId(),
                request.getOrden());

        validarSolapamiento(
                request.getTurnoId(),
                request.getHoraInicio(),
                request.getHoraFin(),
                null);

        FranjaHoraria franja =
                new FranjaHoraria();

        franja.setTurno(turno);
        franja.setNumero(
                request.getNumero());
        franja.setHoraInicio(
                request.getHoraInicio());
        franja.setHoraFin(
                request.getHoraFin());
        franja.setEsDescanso(
                esDescanso);
        franja.setHorasAcademicasEquivalentes(
                request.getHorasAcademicasEquivalentes());
        franja.setOrden(
                request.getOrden());

        FranjaHoraria franjaGuardada =
                franjaHorariaRepository.save(franja);

        return convertirAResponse(
                franjaGuardada,
                request.getTurnoId());
    }

    // Consulta una franja horaria por su identificador.
    @Transactional(readOnly = true)
    public FranjaHorariaResponse consultarFranjaHoraria(
            UUID id) {

        FranjaHoraria franja =
                franjaHorariaRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "La franja horaria no existe"));

        return convertirAResponse(
                franja,
                franja.getTurno().getId());
    }

    // Consulta todas las franjas pertenecientes a un turno.
    @Transactional(readOnly = true)
    public List<FranjaHorariaResponse> listarPorTurno(
            UUID turnoId) {

        obtenerTurno(turnoId);

        return franjaHorariaRepository
                .findByTurno_IdOrderByOrdenAsc(turnoId)
                .stream()
                .map(franja ->
                        convertirAResponse(
                                franja,
                                turnoId))
                .toList();
    }

    // Actualiza una franja horaria existente.
    @Transactional
    public FranjaHorariaResponse actualizarFranjaHoraria(
            UUID id,
            ActualizarFranjaHorariaRequest request) {

        FranjaHoraria franja =
                franjaHorariaRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "La franja horaria no existe"));

        Turno turno =
                obtenerTurno(request.getTurnoId());

        boolean esDescanso =
                request.getEsDescanso();

        validarNumero(
                request.getNumero(),
                esDescanso);

        validarHorasAcademicasEquivalentes(
                request.getHorasAcademicasEquivalentes(),
                esDescanso);

        validarRangoHorario(
                request.getHoraInicio(),
                request.getHoraFin());

        validarDuracion(
                turno,
                request.getHoraInicio(),
                request.getHoraFin(),
                esDescanso);

        validarNumeroDuplicadoAlActualizar(
                request.getTurnoId(),
                request.getNumero(),
                id);

        validarOrdenDuplicadoAlActualizar(
                request.getTurnoId(),
                request.getOrden(),
                id);

        validarSolapamiento(
                request.getTurnoId(),
                request.getHoraInicio(),
                request.getHoraFin(),
                id);

        franja.setTurno(turno);
        franja.setNumero(
                request.getNumero());
        franja.setHoraInicio(
                request.getHoraInicio());
        franja.setHoraFin(
                request.getHoraFin());
        franja.setEsDescanso(
                esDescanso);
        franja.setHorasAcademicasEquivalentes(
                request.getHorasAcademicasEquivalentes());
        franja.setOrden(
                request.getOrden());

        FranjaHoraria franjaActualizada =
                franjaHorariaRepository.save(franja);

        return convertirAResponse(
                franjaActualizada,
                request.getTurnoId());
    }
}
