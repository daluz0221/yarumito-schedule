package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;

import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

        Docente docente =
                obtenerDocente(request.getDocenteId());

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

        // H-04: toda restricción nueva comienza activa.
        restriccion.setActivo(true);

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
    // Incluye restricciones retiradas.
    @Transactional(readOnly = true)
    public RestriccionDocenteResponse consultarRestriccion(
            UUID id) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        return convertirAResponse(restriccion);
    }

    // Lista únicamente las restricciones activas
    // de un docente para un año escolar.
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

    // Lista únicamente las restricciones activas
    // registradas para un año escolar.
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

    // H-04: consulta las restricciones retiradas
    // de un docente durante un año escolar.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarRetiradasPorDocenteYAnio(
            UUID docenteId,
            UUID anioEscolarId) {

        obtenerDocente(docenteId);
        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findByDocente_IdAndAnioEscolar_IdAndActivoFalse(
                        docenteId,
                        anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // H-04: consulta las restricciones retiradas
    // de todos los docentes durante un año escolar.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarRetiradasPorAnio(
            UUID anioEscolarId) {

        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findByAnioEscolar_IdAndActivoFalse(
                        anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // H-04: consulta el histórico completo de un docente.
    // Incluye restricciones activas y retiradas.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarHistoricoPorDocenteYAnio(
            UUID docenteId,
            UUID anioEscolarId) {

        obtenerDocente(docenteId);
        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findAllByDocente_IdAndAnioEscolar_Id(
                        docenteId,
                        anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // H-04: consulta el histórico completo de un año escolar.
    @Transactional(readOnly = true)
    public List<RestriccionDocenteResponse> listarHistoricoPorAnio(
            UUID anioEscolarId) {

        obtenerAnioEscolar(anioEscolarId);

        return restriccionDocenteRepository
                .findAllByAnioEscolar_Id(anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Modifica una restricción existente.
    @Transactional
    public RestriccionDocenteResponse actualizarRestriccion(
            UUID id,
            ActualizarRestriccionDocenteRequest request) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        // H-04: las restricciones retiradas no pueden
        // modificarse sin reactivarse previamente.
        validarRestriccionActiva(restriccion);

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

    // H-04: retira una restricción docente sin eliminarla
    // físicamente de la base de datos.
    @Transactional
    public RestriccionDocenteResponse retirarRestriccion(
            UUID id) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        validarRestriccionActiva(restriccion);

        restriccion.setActivo(false);

        RestriccionDocente retirada =
                restriccionDocenteRepository.save(restriccion);

        return convertirAResponse(retirada);
    }

    // H-04: reactiva una restricción docente retirada.
    @Transactional
    public RestriccionDocenteResponse reactivarRestriccion(
            UUID id) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        if (restriccion.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "La restricción docente ya se encuentra activa");
        }

        // Conserva las reglas originales de disponibilidad.
        validarDocenteActivo(
                restriccion.getDocente());

        validarFranjaAsignable(
                restriccion.getFranjaHoraria());

        // Impide reactivar un registro si ya existe
        // otra condición activa para la misma combinación.
        Optional<RestriccionDocente> condicionActiva =
                restriccionDocenteRepository
                        .findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                                restriccion.getDocente().getId(),
                                restriccion.getAnioEscolar().getId(),
                                restriccion.getDia(),
                                restriccion.getFranjaHoraria().getId());

        if (condicionActiva.isPresent()
                && !condicionActiva.get().getId().equals(id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otra restricción activa para "
                            + "el mismo docente, año, día y franja horaria");
        }

        restriccion.setActivo(true);

        RestriccionDocente reactivada =
                restriccionDocenteRepository.save(restriccion);

        return convertirAResponse(reactivada);
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

    // Los docentes deben estar activos para participar
    // en la programación académica.
    private void validarDocenteActivo(
            Docente docente) {

        if (docente.getEstado() != EstadoDocente.ACTIVO) {

            throw new RelacionAcademicaInvalidaException(
                    "Solo un docente activo puede participar "
                            + "en la programación académica");
        }
    }

    // No se permiten restricciones sobre descansos oficiales.
    private void validarFranjaAsignable(
            FranjaHoraria franjaHoraria) {

        if (franjaHoraria.isEsDescanso()) {

            throw new RelacionAcademicaInvalidaException(
                    "No se pueden registrar restricciones docentes "
                            + "sobre una franja de descanso");
        }
    }

    // H-04: protege los registros históricos retirados.
    private void validarRestriccionActiva(
            RestriccionDocente restriccion) {

        if (!restriccion.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "La restricción docente se encuentra retirada. "
                            + "Debe reactivarla primero");
        }
    }

    // Evita duplicados de docente-año-día-franja,
    // incluyendo las restricciones retiradas.
    private void validarCondicionDuplicada(
            UUID docenteId,
            UUID anioEscolarId,
            DiaSemana dia,
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
                    "El docente ya tiene una condición registrada "
                            + "para ese día y franja horaria. "
                            + "Si fue retirada, debe reactivarse");
        }
    }

    // Convierte documentos vacíos en null.
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

        // H-04: informa el estado activo o retirado.
        response.setActivo(restriccion.isActivo());

        return response;
    }
}
