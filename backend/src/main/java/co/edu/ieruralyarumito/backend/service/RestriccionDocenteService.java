package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.CrearRestriccionDocenteRequest;
import co.edu.ieruralyarumito.backend.dto.RestriccionDocenteResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.Usuario;
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
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class RestriccionDocenteService {

    private final RestriccionDocenteRepository restriccionDocenteRepository;
    private final DocenteRepository docenteRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;

    // H-16: integración con Control y Auditoría.
    private UsuarioRepository usuarioRepository;
    private RegistroAuditoriaService registroAuditoriaService;

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

    // H-16: conserva el constructor original para las pruebas
    // y conecta la auditoría en el contexto de Spring.
    @Autowired
    public void configurarAuditoria(
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.usuarioRepository =
                Objects.requireNonNull(usuarioRepository);

        this.registroAuditoriaService =
                Objects.requireNonNull(registroAuditoriaService);
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
         * autenticado cuando corresponda.
         *
         * No se recibe usuarioId desde el frontend.
         */

        RestriccionDocente guardada =
                restriccionDocenteRepository.save(restriccion);

        // H-16: auditoría de creación exitosa.
        if (registroAuditoriaService != null) {

            auditarRestriccion(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardada,
                    null,
                    datosRestriccion(guardada));
        }

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

        // H-04: una restricción retirada debe reactivarse
        // antes de permitir modificaciones.
        validarRestriccionActiva(restriccion);

        validarDocenteActivo(
                restriccion.getDocente());

        validarFranjaAsignable(
                restriccion.getFranjaHoraria());

        // H-16: instantánea antes de modificar.
        Map<String, Object> datosAntes =
                registroAuditoriaService == null
                        ? null
                        : datosRestriccion(restriccion);

        restriccion.setTipo(request.getTipo());
        restriccion.setMotivo(request.getMotivo());

        restriccion.setDocumentoSoporte(
                normalizarDocumentoSoporte(
                        request.getDocumentoSoporte()));

        RestriccionDocente actualizada =
                restriccionDocenteRepository.save(restriccion);

        // H-16: auditoría de actualización.
        if (registroAuditoriaService != null) {

            auditarRestriccion(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizada,
                    datosAntes,
                    datosRestriccion(actualizada));
        }

        return convertirAResponse(actualizada);
    }

    // H-04: retira una restricción sin eliminar su historial.
    @Transactional
    public RestriccionDocenteResponse retirarRestriccion(
            UUID id) {

        RestriccionDocente restriccion =
                obtenerRestriccion(id);

        validarRestriccionActiva(restriccion);

        // H-16: conservar el estado activo previo.
        Map<String, Object> datosAntes =
                registroAuditoriaService == null
                        ? null
                        : datosRestriccion(restriccion);

        restriccion.setActivo(false);

        RestriccionDocente retirada =
                restriccionDocenteRepository.save(restriccion);

        // Una baja lógica es una actualización de estado.
        if (registroAuditoriaService != null) {

            auditarRestriccion(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    retirada,
                    datosAntes,
                    datosRestriccion(retirada));
        }

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

        validarDocenteActivo(
                restriccion.getDocente());

        validarFranjaAsignable(
                restriccion.getFranjaHoraria());

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

        // H-16: conservar el estado retirado previo.
        Map<String, Object> datosAntes =
                registroAuditoriaService == null
                        ? null
                        : datosRestriccion(restriccion);

        restriccion.setActivo(true);

        RestriccionDocente reactivada =
                restriccionDocenteRepository.save(restriccion);

        // H-16: auditoría de reactivación.
        if (registroAuditoriaService != null) {

            auditarRestriccion(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    reactivada,
                    datosAntes,
                    datosRestriccion(reactivada));
        }

        return convertirAResponse(reactivada);
    }

    // Obtiene una restricción existente.
    private RestriccionDocente obtenerRestriccion(UUID id) {

        return restriccionDocenteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La restricción docente no existe"));
    }

    // Obtiene el docente asociado.
    private Docente obtenerDocente(UUID docenteId) {

        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Obtiene el año escolar asociado.
    private AnioEscolar obtenerAnioEscolar(UUID anioEscolarId) {

        return anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));
    }

    // Obtiene la franja horaria asociada.
    private FranjaHoraria obtenerFranjaHoraria(UUID franjaHorariaId) {

        return franjaHorariaRepository.findById(franjaHorariaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La franja horaria no existe"));
    }

    // Los docentes deben estar activos.
    private void validarDocenteActivo(Docente docente) {

        if (docente.getEstado() != EstadoDocente.ACTIVO) {

            throw new RelacionAcademicaInvalidaException(
                    "Solo un docente activo puede participar "
                            + "en la programación académica");
        }
    }

    // No se registran restricciones sobre descansos.
    private void validarFranjaAsignable(
            FranjaHoraria franjaHoraria) {

        if (franjaHoraria.isEsDescanso()) {

            throw new RelacionAcademicaInvalidaException(
                    "No se pueden registrar restricciones docentes "
                            + "sobre una franja de descanso");
        }
    }

    // H-04: impide modificar registros retirados.
    private void validarRestriccionActiva(
            RestriccionDocente restriccion) {

        if (!restriccion.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "La restricción docente se encuentra retirada. "
                            + "Debe reactivarla primero");
        }
    }

    // Evita duplicados incluso cuando el original está retirado.
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

    // H-16: registra cambios dentro de la transacción
    // que realiza la operación de negocio.
    private void auditarRestriccion(
            String accion,
            RestriccionDocente restriccion,
            Object datosAntes,
            Object datosDespues) {

        // Compatibilidad con las pruebas unitarias existentes.
        if (registroAuditoriaService == null) {
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        String correo = authentication.getName();

        Usuario usuario =
                usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no identificado "
                                                + "para auditoría"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "RestriccionDocente",
                restriccion.getId(),
                datosAntes,
                datosDespues,
                null
        );
    }

    // H-16: instantánea de los datos relevantes.
    // No se almacena el contenido binario del soporte.
    private Map<String, Object> datosRestriccion(
            RestriccionDocente restriccion) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        datos.put("id", restriccion.getId());

        datos.put("docenteId",
                restriccion.getDocente().getId());

        datos.put("anioEscolarId",
                restriccion.getAnioEscolar().getId());

        datos.put("dia",
                restriccion.getDia().name());

        datos.put("franjaHorariaId",
                restriccion.getFranjaHoraria().getId());

        datos.put("tipo",
                restriccion.getTipo().name());

        datos.put("motivo",
                restriccion.getMotivo());

        datos.put("documentoSoportePresente",
                restriccion.getDocumentoSoporte() != null
                        && restriccion.getDocumentoSoporte().length > 0);

        datos.put("aprobadaPorId",
                restriccion.getAprobadaPor() == null
                        ? null
                        : restriccion.getAprobadaPor().getId());

        datos.put("activo",
                restriccion.isActivo());

        return datos;
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

        response.setActivo(restriccion.isActivo());

        return response;
    }
}
