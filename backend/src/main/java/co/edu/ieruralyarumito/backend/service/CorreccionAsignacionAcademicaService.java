package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CorreccionAsignacionAcademicaService {

    private final AsignacionAcademicaRepository asignacionRepository;
    private final DocenteRepository docenteRepository;
    private final IdoneidadRepository idoneidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionAcademicaMapper mapper;
    private final RegistroAuditoriaService auditoria;
    private final EntityManager entityManager;

    public CorreccionAsignacionAcademicaService(
            AsignacionAcademicaRepository asignacionRepository,
            DocenteRepository docenteRepository,
            IdoneidadRepository idoneidadRepository,
            UsuarioRepository usuarioRepository,
            AsignacionAcademicaMapper mapper,
            RegistroAuditoriaService auditoria,
            EntityManager entityManager) {

        this.asignacionRepository = asignacionRepository;
        this.docenteRepository = docenteRepository;
        this.idoneidadRepository = idoneidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.auditoria = auditoria;
        this.entityManager = entityManager;
    }

    // H-04: permite corregir docente, horas y observación
    // únicamente antes de confirmar y programar clases.
    @Transactional
    public AsignacionAcademicaResponse actualizar(
            UUID asignacionId,
            ActualizarAsignacionAcademicaRequest request,
            String correoUsuario) {

        Usuario rector = obtenerRector(correoUsuario);
        AsignacionAcademica asignacion =
                obtenerAsignacion(asignacionId);

        validarModificable(asignacion);

        if (request == null
                || request.getDocenteId() == null
                || request.getHorasSemanales() == null
                || request.getHorasSemanales() <= 0) {

            throw new RelacionAcademicaInvalidaException(
                    "Debe indicar un docente y una cantidad "
                            + "positiva de horas semanales");
        }

        Docente docente = docenteRepository
                .findById(request.getDocenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El docente no existe"));

        if (docente.getEstado() != EstadoDocente.ACTIVO) {
            throw new RelacionAcademicaInvalidaException(
                    "El docente debe estar activo");
        }

        validarIdoneidad(docente, asignacion.getAsignatura());

        validarCargaDocente(
                asignacion,
                docente,
                request.getHorasSemanales());

        Map<String, Object> antes = datosAsignacion(asignacion);

        asignacion.setDocente(docente);
        asignacion.setHorasSemanales(request.getHorasSemanales());
        asignacion.setObservacion(request.getObservacion());

        AsignacionAcademica guardada =
                asignacionRepository.save(asignacion);

        auditoria.registrar(
                rector,
                RegistroAuditoriaService.ACCION_UPDATE,
                "AsignacionAcademica",
                guardada.getId(),
                antes,
                datosAsignacion(guardada),
                null
        );

        return mapper.toResponse(guardada);
    }

    // H-04: retiro controlado de un BORRADOR
    // que no tenga clases programadas.
    @Transactional
    public void retirar(
            UUID asignacionId,
            String correoUsuario) {

        Usuario rector = obtenerRector(correoUsuario);
        AsignacionAcademica asignacion =
                obtenerAsignacion(asignacionId);

        validarModificable(asignacion);

        Map<String, Object> antes = datosAsignacion(asignacion);

        asignacionRepository.delete(asignacion);

        auditoria.registrar(
                rector,
                RegistroAuditoriaService.ACCION_DELETE,
                "AsignacionAcademica",
                asignacionId,
                antes,
                null,
                null
        );
    }

    // Solo el rector puede corregir o retirar asignaciones.
    private Usuario obtenerRector(String correo) {

        if (correo == null || correo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Se requiere autenticación del rector");
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Usuario no autorizado"));

        if (!usuario.isActivo()
                || usuario.getRol() != RolUsuario.RECTOR) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el rector puede corregir asignaciones");
        }

        return usuario;
    }

    private AsignacionAcademica obtenerAsignacion(UUID id) {

        return asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignación académica no existe"));
    }

    // Protege asignaciones confirmadas, clases registradas
    // y horarios ya publicados.
    private void validarModificable(
            AsignacionAcademica asignacion) {

        if (asignacion.getEstado()
                != EstadoAsignacionAcademica.BORRADOR) {

            throw new TransicionEstadoNoPermitidaException(
                    "Solo pueden corregirse o retirarse "
                            + "asignaciones en BORRADOR");
        }

        Long clases = entityManager.createQuery("""
                select count(c)
                from ClaseProgramada c
                where c.asignacionAcademicaId.id = :id
                """, Long.class)
                .setParameter("id", asignacion.getId())
                .getSingleResult();

        if (clases > 0) {
            throw new TransicionEstadoNoPermitidaException(
                    "La asignación tiene clases programadas. "
                            + "No puede modificarse ni retirarse");
        }

        UUID anioId = asignacion.getAnioEscolar().getId();
        UUID sedeId = asignacion.getGrupo().getSede().getId();

        Long publicados = entityManager.createQuery("""
                select count(h)
                from Horario h
                where h.anioEscolarId.id = :anioId
                  and h.sedeId.id = :sedeId
                  and h.estado = :estado
                """, Long.class)
                .setParameter("anioId", anioId)
                .setParameter("sedeId", sedeId)
                .setParameter("estado", EstadoHorario.PUBLICADO)
                .getSingleResult();

        if (publicados > 0) {
            throw new TransicionEstadoNoPermitidaException(
                    "No se pueden modificar asignaciones "
                            + "de una sede con horario publicado");
        }
    }

    // Conserva la validación de idoneidad vigente.
    private void validarIdoneidad(
            Docente docente,
            Asignatura asignatura) {

        LocalDate hoy = LocalDate.now();

        List<Idoneidad> idoneidades =
                idoneidadRepository.findByDocente_Id(
                        docente.getId());

        boolean habilitado = idoneidades.stream().anyMatch(
                idoneidad -> {

                    boolean cubre;

                    if (idoneidad.getAsignatura() != null) {

                        cubre = idoneidad.getAsignatura()
                                .getId().equals(asignatura.getId());

                    } else {

                        cubre = idoneidad.getArea() != null
                                && asignatura.getArea() != null
                                && idoneidad.getArea().getId()
                                .equals(asignatura.getArea().getId());
                    }

                    boolean vigente =
                            idoneidad.getVigenteDesde() != null
                                    && !idoneidad.getVigenteDesde()
                                    .isAfter(hoy)
                                    && (idoneidad.getVigenteHasta() == null
                                    || !idoneidad.getVigenteHasta()
                                    .isBefore(hoy));

                    return cubre && vigente;
                });

        if (!habilitado) {
            throw new RelacionAcademicaInvalidaException(
                    "El docente no tiene idoneidad vigente "
                            + "para esta asignatura");
        }
    }

    // Misma regla H-05: suma las asignaciones
    // del año escolar, excluyendo la que se está editando.
    private void validarCargaDocente(
            AsignacionAcademica actual,
            Docente docente,
            int horasNuevas) {

        int ordinarias = docente.getHorasSemanalesContratadas();
        int extras = docente.getMaxHorasExtra();

        if (ordinarias < 0 || extras < 0) {
            throw new RelacionAcademicaInvalidaException(
                    "La carga horaria del docente no es válida");
        }

        long limite = Math.min(
                32L, (long) ordinarias + extras);

        long yaAsignadas = asignacionRepository
                .findByDocente_Id(docente.getId())
                .stream()
                .filter(item ->
                        !actual.getId().equals(item.getId())
                                && item.getAnioEscolar() != null
                                && actual.getAnioEscolar().getId()
                                .equals(item.getAnioEscolar().getId()))
                .mapToLong(AsignacionAcademica::getHorasSemanales)
                .sum();

        if (limite <= 0 || yaAsignadas + horasNuevas > limite) {
            throw new RelacionAcademicaInvalidaException(
                    "La carga semanal del docente superaría "
                            + "el límite permitido de " + limite
                            + " horas");
        }
    }

    // Datos mínimos para la trazabilidad HU-14.
    private Map<String, Object> datosAsignacion(
            AsignacionAcademica asignacion) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", asignacion.getId());
        datos.put("anioEscolarId",
                asignacion.getAnioEscolar().getId());
        datos.put("grupoId",
                asignacion.getGrupo().getId());
        datos.put("asignaturaId",
                asignacion.getAsignatura().getId());
        datos.put("docenteId",
                asignacion.getDocente().getId());
        datos.put("horasSemanales",
                asignacion.getHorasSemanales());
        datos.put("estado", asignacion.getEstado().name());
        datos.put("observacion", asignacion.getObservacion());

        return datos;
    }
}
