package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para las restricciones
// de disponibilidad de los docentes.
@Service
public class RestriccionDocenteService {

    private final RestriccionDocenteRepository restriccionDocenteRepository;
    private final DocenteRepository docenteRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;

    public RestriccionDocenteService(
            RestriccionDocenteRepository restriccionDocenteRepository,
            DocenteRepository docenteRepository,
            AnioEscolarRepository anioEscolarRepository,
            FranjaHorariaRepository franjaHorariaRepository) {

        this.restriccionDocenteRepository =
                restriccionDocenteRepository;

        this.docenteRepository =
                docenteRepository;

        this.anioEscolarRepository =
                anioEscolarRepository;

        this.franjaHorariaRepository =
                franjaHorariaRepository;
    }

    // Registra una condición de disponibilidad para un docente.
    @Transactional
    public RestriccionDocenteResponse registrarRestriccion(
            CrearRestriccionDocenteRequest request) {

        Docente docente = obtenerDocente(request.getDocenteId());

        validarDocenteActivo(docente);

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(request.getAnioEscolarId());

        FranjaHoraria franjaHoraria =
                obtenerFranjaHoraria(request.getFranjaHorariaId());

        validarFranjaAsignable(franjaHoraria);

        validarCondicionDuplicada(
                request.getDocenteId(),
                request.getAnioEscolarId(),
                request.getDia(),
                request.getFranjaHorariaId());

        RestriccionDocente restriccion =
                new RestriccionDocente();

        restriccion.setDocente(docente);
        restriccion.setAnioEscolar(anioEscolar);
        restriccion.setDia(request.getDia());
        restriccion.setFranjaHoraria(franjaHoraria);
        restriccion.setTipo(request.getTipo());
        restriccion.setMotivo(request.getMotivo());
        restriccion.setDocumentoSoporte(
                normalizarDocumentoSoporte(
                        request.getDocumentoSoporte()));

        /*
         * aprobadaPor se integrará con el Usuario RECTOR
         * autenticado cuando el módulo de autenticación esté
         * disponible en esta rama.
         *
         * No se recibe usuarioId desde el frontend.
         */

        RestriccionDocente guardada =
                restriccionDocenteRepository.save(restriccion);

        return convertirAResponse(guardada);
    }

    // Consulta una restricción por su identificador.
    @Transactional(readOnly = true)
    public RestriccionDocenteResponse consultarRestriccion(
            UUID id) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        return convertirAResponse(restriccion);
    }

    // Lista las restricciones de un docente para un año escolar.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarPorDocenteYAnio(
            UUID docenteId,
            UUID anioEscolarId) {

        obtenerDocente(docenteId);
        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findByDocente_IdAndAnioEscolar_Id(
                        docenteId,
                        anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Lista todas las restricciones registradas para un año escolar.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarPorAnio(
            UUID anioEscolarId) {

        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findByAnioEscolar_Id(anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Modifica la condición existente sin crear otra
    // para la misma combinación docente-año-día-franja.
    @Transactional
    public RestriccionDocenteResponse actualizarRestriccion(
            UUID id,
            ActualizarRestriccionDocenteRequest request) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        validarDocenteActivo(
                restriccion.getDocente());

        validarFranjaAsignable(
                restriccion.getFranjaHoraria());

        restriccion.setTipo(request.getTipo());
        restriccion.setMotivo(request.getMotivo());
        restriccion.setDocumentoSoporte(
                normalizarDocumentoSoporte(
                        request.getDocumentoSoporte()));

        RestriccionDocente actualizada =
                restriccionDocenteRepository.save(restriccion);

        return convertirAResponse(actualizada);
    }

    // Obtiene una restricción existente.
    private RestriccionDocente obtenerRestriccion(
            UUID id) {

        return restriccionDocenteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La restricción docente no existe"));
    }

    // Obtiene el docente asociado.
    private Docente obtenerDocente(
            UUID docenteId) {

        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Obtiene el año escolar asociado.
    private AnioEscolar obtenerAnioEscolar(
            UUID anioEscolarId) {

        return anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));
    }

    // Obtiene la franja horaria asociada.
    private FranjaHoraria obtenerFranjaHoraria(
            UUID franjaHorariaId) {

        return franjaHorariaRepository
                .findById(franjaHorariaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La franja horaria no existe"));
    }

    // La disponibilidad mediante restricciones aplica a docentes
    // que pueden participar actualmente en la programación académica.
    private void validarDocenteActivo(
            Docente docente) {

        if (docente.getEstado() != EstadoDocente.ACTIVO) {

            throw new RelacionAcademicaInvalidaException(
                    "Solo un docente activo puede participar en la programación académica");
        }
    }

    // Un descanso ya es una franja no asignable por definición,
    // por lo tanto no admite restricciones docentes.
    private void validarFranjaAsignable(
            FranjaHoraria franjaHoraria) {

        if (franjaHoraria.isEsDescanso()) {

            throw new RelacionAcademicaInvalidaException(
                    "No se pueden registrar restricciones docentes sobre una franja de descanso");
        }
    }

    // Para un mismo docente, año, día y franja solo puede
    // existir una condición efectiva.
    private void validarCondicionDuplicada(
            UUID docenteId,
            UUID anioEscolarId,
            co.edu.ieruralyarumito.backend.entity.enums.DiaSemana dia,
            UUID franjaHorariaId) {

        boolean existe =
                restriccionDocenteRepository
                        .existsByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                                docenteId,
                                anioEscolarId,
                                dia,
                                franjaHorariaId);

        if (existe) {

            throw new RecursoDuplicadoException(
                    "El docente ya tiene una condición registrada para ese día y franja horaria");
        }
    }

    // Convierte cadenas vacías de documento soporte en null.
    private byte[] normalizarDocumentoSoporte(
            byte[] documentoSoporte) {

        if (documentoSoporte == null
                || documentoSoporte.length == 0) {
            return null;
        }

        return documentoSoporte;
    }

    // Convierte la entidad al DTO devuelto por la API.
    private RestriccionDocenteResponse convertirAResponse(
            RestriccionDocente restriccion) {

        RestriccionDocenteResponse response =
                new RestriccionDocenteResponse();

        response.setId(restriccion.getId());

        response.setDocenteId(
                restriccion.getDocente().getId());

        response.setAnioEscolarId(
                restriccion.getAnioEscolar().getId());

        response.setDia(
                restriccion.getDia());

        response.setFranjaHorariaId(
                restriccion.getFranjaHoraria().getId());

        response.setTipo(
                restriccion.getTipo());

        response.setMotivo(
                restriccion.getMotivo());

        response.setDocumentoSoporte(
                restriccion.getDocumentoSoporte());

        if (restriccion.getAprobadaPor() != null) {

            response.setAprobadaPorId(
                    restriccion.getAprobadaPor().getId());
        }

        return response;
    }
}
