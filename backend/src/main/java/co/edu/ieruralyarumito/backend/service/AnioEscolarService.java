package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.dto.AnioEscolarResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAnioEscolarRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de años escolares.
@Service
public class AnioEscolarService {

    private final AnioEscolarRepository anioEscolarRepository;

    // Inyección de dependencias mediante constructor.
    public AnioEscolarService(AnioEscolarRepository anioEscolarRepository) {
        this.anioEscolarRepository = anioEscolarRepository;
    }

    // RN-12.01: valida que no exista otro año lectivo con el mismo número.
    private void validarAnioDuplicado(int anio) {

        if (anioEscolarRepository.existsByAnio(anio)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un año escolar registrado para " + anio);
        }
    }

    // RN-12.01: valida duplicidad al actualizar, excluyendo el registro actual.
    private void validarAnioDuplicadoAlActualizar(
            int anio,
            UUID id) {

        if (anioEscolarRepository.existsByAnioAndIdNot(anio, id)) {
            throw new RecursoDuplicadoException(
                    "Ya existe otro año escolar registrado para " + anio);
        }
    }

    // RN-12.02: la fecha de inicio debe ser anterior a la fecha de finalización.
    private void validarFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (!fechaInicio.isBefore(fechaFin)) {
            throw new FechaVigenciaInvalidaException(
                    "La fecha de inicio debe ser anterior a la fecha de finalización");
        }
    }

    // RN-12.03: solo puede existir un año escolar marcado como actual.
    private void validarUnicoAnioActual(boolean esActual) {

        if (esActual && anioEscolarRepository.existsByEsActualTrue()) {
            throw new RecursoDuplicadoException(
                    "Ya existe un año escolar marcado como actual");
        }
    }

    // RN-12.03: valida el año actual al modificar, excluyendo el registro actual.
    private void validarUnicoAnioActualAlActualizar(
            boolean esActual,
            UUID id) {

        if (esActual
                && anioEscolarRepository.existsByEsActualTrueAndIdNot(id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro año escolar marcado como actual");
        }
    }

    // RN-12.04: el año ACTIVO debe coincidir con el año marcado como actual.
    private void validarCoherenciaEstadoActual(
            EstadoAnioEscolar estado,
            boolean esActual) {

        if (esActual && estado != EstadoAnioEscolar.ACTIVO) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo un año escolar en estado ACTIVO puede ser marcado como actual");
        }

        if (estado == EstadoAnioEscolar.ACTIVO && !esActual) {
            throw new TransicionEstadoNoPermitidaException(
                    "Un año escolar en estado ACTIVO debe estar marcado como actual");
        }
    }

    // RN-12.05: controla las transiciones permitidas entre estados.
    private void validarTransicionEstado(
            EstadoAnioEscolar estadoActual,
            EstadoAnioEscolar nuevoEstado) {

        // Mantener el mismo estado es permitido mientras el año no esté cerrado.
        if (estadoActual == nuevoEstado) {
            return;
        }

        boolean transicionPermitida =
                (estadoActual == EstadoAnioEscolar.PLANEACION
                        && nuevoEstado == EstadoAnioEscolar.ACTIVO)
                        ||
                        (estadoActual == EstadoAnioEscolar.ACTIVO
                                && nuevoEstado == EstadoAnioEscolar.CERRADO);

        if (!transicionPermitida) {
            throw new TransicionEstadoNoPermitidaException(
                    "Transición de estado no permitida: "
                            + estadoActual
                            + " -> "
                            + nuevoEstado);
        }
    }

    // RN-12.06: un año cerrado conserva su información histórica.
    private void validarAnioNoCerrado(AnioEscolar anioEscolar) {

        if (anioEscolar.getEstado() == EstadoAnioEscolar.CERRADO) {
            throw new TransicionEstadoNoPermitidaException(
                    "Un año escolar cerrado no puede ser modificado");
        }
    }

    // Convierte la entidad AnioEscolar en el DTO devuelto por la API.
    private AnioEscolarResponse convertirAResponse(
            AnioEscolar anioEscolar) {

        AnioEscolarResponse response = new AnioEscolarResponse();

        response.setId(anioEscolar.getId());
        response.setAnio(anioEscolar.getAnio());
        response.setFechaInicio(anioEscolar.getFechaInicio());
        response.setFechaFin(anioEscolar.getFechaFin());
        response.setEstado(anioEscolar.getEstado());
        response.setEsActual(anioEscolar.isEsActual());

        return response;
    }

    // Registra un nuevo año escolar.
    @Transactional
    public AnioEscolarResponse registrarAnioEscolar(
            CrearAnioEscolarRequest request) {

        validarAnioDuplicado(request.getAnio());

        validarFechas(
                request.getFechaInicio(),
                request.getFechaFin());

        validarCoherenciaEstadoActual(
                request.getEstado(),
                request.getEsActual());

        validarUnicoAnioActual(
                request.getEsActual());

        AnioEscolar anioEscolar = new AnioEscolar();

        anioEscolar.setAnio(request.getAnio());
        anioEscolar.setFechaInicio(request.getFechaInicio());
        anioEscolar.setFechaFin(request.getFechaFin());
        anioEscolar.setEstado(request.getEstado());
        anioEscolar.setEsActual(request.getEsActual());

        AnioEscolar anioEscolarGuardado =
                anioEscolarRepository.save(anioEscolar);

        return convertirAResponse(anioEscolarGuardado);
    }

    // Consulta un año escolar por su identificador.
    @Transactional(readOnly = true)
    public AnioEscolarResponse consultarAnioEscolar(UUID id) {

        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));

        return convertirAResponse(anioEscolar);
    }

    // Lista los años escolares con paginación.
    @Transactional(readOnly = true)
    public Page<AnioEscolarResponse> listarAniosEscolares(
            Pageable pageable) {

        return anioEscolarRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un año escolar existente respetando sus reglas de negocio.
    @Transactional
    public AnioEscolarResponse actualizarAnioEscolar(
            UUID id,
            ActualizarAnioEscolarRequest request) {

        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));

        validarAnioNoCerrado(anioEscolar);

        validarAnioDuplicadoAlActualizar(
                request.getAnio(),
                id);

        validarFechas(
                request.getFechaInicio(),
                request.getFechaFin());

        validarTransicionEstado(
                anioEscolar.getEstado(),
                request.getEstado());

        validarCoherenciaEstadoActual(
                request.getEstado(),
                request.getEsActual());

        validarUnicoAnioActualAlActualizar(
                request.getEsActual(),
                id);

        anioEscolar.setAnio(request.getAnio());
        anioEscolar.setFechaInicio(request.getFechaInicio());
        anioEscolar.setFechaFin(request.getFechaFin());
        anioEscolar.setEstado(request.getEstado());
        anioEscolar.setEsActual(request.getEsActual());

        AnioEscolar anioEscolarActualizado =
                anioEscolarRepository.save(anioEscolar);

        return convertirAResponse(anioEscolarActualizado);
    }
}
