package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.ResumenAsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AsignacionAcademicaService {

    private final AsignacionAcademicaRepository asignacionAcademicaRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final DocenteRepository docenteRepository;
    private final GrupoRepository grupoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdoneidadRepository idoneidadRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final AsignacionAcademicaMapper asignacionAcademicaMapper;

    private RegistroAuditoriaService registroAuditoriaService;

    public AsignacionAcademicaService(
            AsignacionAcademicaRepository asignacionAcademicaRepository,
            AnioEscolarRepository anioEscolarRepository,
            DocenteRepository docenteRepository,
            GrupoRepository grupoRepository,
            AsignaturaRepository asignaturaRepository,
            UsuarioRepository usuarioRepository,
            IdoneidadRepository idoneidadRepository,
            PlanEstudiosRepository planEstudiosRepository,
            AsignacionAcademicaMapper asignacionAcademicaMapper) {

        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.docenteRepository = docenteRepository;
        this.grupoRepository = grupoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.idoneidadRepository = idoneidadRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.asignacionAcademicaMapper = asignacionAcademicaMapper;
    }

    @Autowired
    public void configurarAuditoria(
            RegistroAuditoriaService servicio) {

        this.registroAuditoriaService =
                Objects.requireNonNull(servicio);
    }

    // Crea una asignación académica en estado BORRADOR.
    @Transactional
    public AsignacionAcademicaResponse crearAsignacionAcademica(
            CrearAsignacionAcademicaRequest request,
            String correoUsuario) {

        AnioEscolar anioEscolar = anioEscolarRepository
                .findById(request.getAnioEscolarId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El año escolar no existe"));

        Docente docente = docenteRepository
                .findById(request.getDocenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El docente no existe"));

        Grupo grupo = grupoRepository
                .findById(request.getGrupoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El grupo no existe"));

        Asignatura asignatura = asignaturaRepository
                .findById(request.getAsignaturaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignatura no existe"));

        Usuario asignadaPor = usuarioRepository
                .findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no existe"));

        // H-05: coherencia entre grupo y año escolar.
        validarAnioEscolarGrupo(anioEscolar, grupo);

        // H-06A: únicamente docentes activos.
        validarDocenteActivo(docente);

        // Reglas académicas existentes.
        validarUnicidad(anioEscolar, grupo, asignatura);
        validarContinuidadDocente(
                anioEscolar, grupo, asignatura, docente);
        validarIdoneidadDocente(docente, asignatura);

        AsignacionAcademica asignacion =
                asignacionAcademicaMapper.toEntity(
                        request,
                        anioEscolar,
                        docente,
                        grupo,
                        asignatura,
                        asignadaPor);

        AsignacionAcademica guardada =
                asignacionAcademicaRepository.save(asignacion);

        // HU-14: auditoría de creación.
        if (registroAuditoriaService != null) {

            auditarAsignacion(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardada,
                    null,
                    datosAsignacion(guardada),
                    correoUsuario
            );
        }

        sincronizarGruposDelMismoGrado(grupo.getId());

        return asignacionAcademicaMapper.toResponse(guardada);
    }

    // Los grupos activos del mismo año, grado y sede comparten materias
    // y docente. Cada copia tiene sus propias horas y sus propias clases.
    @Transactional
    public void sincronizarGruposDelMismoGrado(UUID grupoId) {

        Grupo grupo = grupoRepository
                .findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El grupo no existe"));

        if (grupo.getGrado() == null
                || grupo.getAnioEscolar() == null
                || !grupo.isActivo()) {

            return;
        }

        List<Grupo> grupos = grupoRepository
                .findByGrado_IdAndAnioEscolar_Id(
                        grupo.getGrado().getId(),
                        grupo.getAnioEscolar().getId())
                .stream()
                .filter(Grupo::isActivo)
                .filter(otro -> mismaSede(grupo, otro))
                .toList();

        if (grupos.size() < 2) {
            return;
        }

        Map<UUID, List<AsignacionAcademica>> porGrupo =
                new HashMap<>();

        for (Grupo item : grupos) {

            porGrupo.put(
                    item.getId(),
                    new ArrayList<>(
                            asignacionAcademicaRepository
                                    .findByAnioEscolar_IdAndGrupo_Id(
                                            grupo.getAnioEscolar().getId(),
                                            item.getId())));
        }

        for (Grupo origen : grupos) {

            List<AsignacionAcademica> asignaciones =
                    List.copyOf(porGrupo.get(origen.getId()));

            for (AsignacionAcademica asignacion : asignaciones) {

                UUID asignaturaId =
                        asignacion.getAsignatura().getId();

                for (Grupo destino : grupos) {

                    if (destino.getId().equals(origen.getId())
                            || tieneAsignatura(
                            porGrupo.get(destino.getId()),
                            asignaturaId)) {

                        continue;
                    }

                    if (asignacionAcademicaRepository
                            .existsByAnioEscolar_IdAndGrupo_IdAndAsignatura_Id(
                                    grupo.getAnioEscolar().getId(),
                                    destino.getId(),
                                    asignaturaId)) {

                        continue;
                    }

                    AsignacionAcademica guardada =
                            asignacionAcademicaRepository.save(
                                    copiarAsignacion(asignacion, destino));

                    porGrupo.get(destino.getId()).add(guardada);

                    if (registroAuditoriaService != null) {

                        String correo =
                                guardada.getAsignadaPor() == null
                                        ? null
                                        : guardada.getAsignadaPor()
                                        .getCorreo();

                        auditarAsignacion(
                                RegistroAuditoriaService.ACCION_CREATE,
                                guardada,
                                null,
                                datosAsignacion(guardada),
                                correo);
                    }
                }
            }
        }
    }

    private boolean mismaSede(Grupo referencia, Grupo otro) {

        UUID sedeReferencia = referencia.getSede() == null
                ? null
                : referencia.getSede().getId();

        UUID sedeOtro = otro.getSede() == null
                ? null
                : otro.getSede().getId();

        return Objects.equals(sedeReferencia, sedeOtro);
    }

    private boolean tieneAsignatura(
            List<AsignacionAcademica> asignaciones,
            UUID asignaturaId) {

        return asignaciones.stream()
                .anyMatch(asignacion ->
                        asignaturaId.equals(
                                asignacion.getAsignatura().getId()));
    }

    private AsignacionAcademica copiarAsignacion(
            AsignacionAcademica origen,
            Grupo destino) {

        AsignacionAcademica copia = new AsignacionAcademica();

        copia.setAnioEscolar(origen.getAnioEscolar());
        copia.setDocente(origen.getDocente());
        copia.setGrupo(destino);
        copia.setAsignatura(origen.getAsignatura());
        copia.setHorasSemanales(origen.getHorasSemanales());
        copia.setHorasExtras(origen.getHorasExtras());
        copia.setEstado(origen.getEstado());
        copia.setObservacion(origen.getObservacion());
        copia.setAsignadaPor(origen.getAsignadaPor());

        return copia;
    }

    // H-05: evita asignaciones entre años diferentes.
    private void validarAnioEscolarGrupo(
            AnioEscolar anioEscolar,
            Grupo grupo) {

        if (grupo.getAnioEscolar() == null
                || !anioEscolar.getId().equals(
                grupo.getAnioEscolar().getId())) {

            throw new RelacionAcademicaInvalidaException(
                    "El grupo no pertenece al año escolar indicado");
        }
    }

    // H-06A: verifica el estado del docente.
    private void validarDocenteActivo(Docente docente) {

        if (docente.getEstado() != EstadoDocente.ACTIVO) {

            throw new RelacionAcademicaInvalidaException(
                    "Solo un docente activo puede recibir "
                            + "una asignación académica");
        }
    }

    @Transactional(readOnly = true)
    public AsignacionAcademicaResponse consultarAsignacionAcademica(
            UUID id) {

        return asignacionAcademicaMapper.toResponse(
                obtenerAsignacion(id));
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarBorradores() {

        return asignacionAcademicaRepository
                .findByEstado(EstadoAsignacionAcademica.BORRADOR)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarPorDocente(
            UUID docenteId) {

        if (!docenteRepository.existsById(docenteId)) {

            throw new RecursoNoEncontradoException(
                    "El docente no existe");
        }

        return asignacionAcademicaRepository
                .findByDocente_Id(docenteId)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarPorGrupo(
            UUID grupoId) {

        if (!grupoRepository.existsById(grupoId)) {

            throw new RecursoNoEncontradoException(
                    "El grupo no existe");
        }

        return asignacionAcademicaRepository
                .findByGrupo_Id(grupoId)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    // Confirma una asignación en estado BORRADOR.
    @Transactional
    public AsignacionAcademicaResponse confirmarAsignacionAcademica(
            UUID id) {

        AsignacionAcademica asignacion = obtenerAsignacion(id);

        if (asignacion.getEstado()
                != EstadoAsignacionAcademica.BORRADOR) {

            throw new TransicionEstadoNoPermitidaException(
                    "Solo las asignaciones en estado BORRADOR "
                            + "pueden confirmarse");
        }

        Map<String, Object> datosAntes =
                registroAuditoriaService == null
                        ? null
                        : datosAsignacion(asignacion);

        asignacion.setEstado(
                EstadoAsignacionAcademica.CONFIRMADA);

        AsignacionAcademica guardada =
                asignacionAcademicaRepository.save(asignacion);

        // HU-14: registrar cambio BORRADOR -> CONFIRMADA.
        if (registroAuditoriaService != null) {

            auditarAsignacion(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    guardada,
                    datosAntes,
                    datosAsignacion(guardada),
                    null
            );
        }

        return asignacionAcademicaMapper.toResponse(guardada);
    }

    // CA-12: no duplicar asignaciones.
    private void validarUnicidad(
            AnioEscolar anioEscolar,
            Grupo grupo,
            Asignatura asignatura) {

        boolean existeAsignacion =
                asignacionAcademicaRepository
                        .existsByAnioEscolar_IdAndGrupo_IdAndAsignatura_Id(
                                anioEscolar.getId(),
                                grupo.getId(),
                                asignatura.getId());

        if (existeAsignacion) {

            throw new RecursoDuplicadoException(
                    "Ya existe una asignación académica "
                            + "para el año escolar, grupo "
                            + "y asignatura indicados");
        }
    }

    // CA-09: continuidad del docente en un mismo grado.
    private void validarContinuidadDocente(
            AnioEscolar anioEscolar,
            Grupo grupo,
            Asignatura asignatura,
            Docente docente) {

        List<AsignacionAcademica> asignacionesExistentes =
                asignacionAcademicaRepository
                        .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                                anioEscolar.getId(),
                                grupo.getGrado().getId(),
                                asignatura.getId());

        boolean asignadaAOtroDocente =
                asignacionesExistentes.stream()
                        .anyMatch(asignacion ->
                                !asignacion.getDocente()
                                        .getId()
                                        .equals(docente.getId()));

        if (asignadaAOtroDocente) {

            throw new RelacionAcademicaInvalidaException(
                    "La asignatura ya está asignada "
                            + "a otro docente en el grado "
                            + grupo.getGrado().getNombre()
                            + "; debe ser dictada por el mismo "
                            + "docente en todos los grupos del grado");
        }
    }

    // CA-07: idoneidad vigente del docente.
    private void validarIdoneidadDocente(
            Docente docente,
            Asignatura asignatura) {

        LocalDate hoy = LocalDate.now();

        List<Idoneidad> idoneidades =
                idoneidadRepository.findByDocente_Id(
                        docente.getId());

        boolean habilitado =
                idoneidades.stream()
                        .anyMatch(idoneidad ->
                                cubreAsignatura(
                                        idoneidad, asignatura)
                                        && estaVigente(
                                        idoneidad, hoy));

        if (!habilitado) {

            throw new RelacionAcademicaInvalidaException(
                    "El docente no cuenta con una idoneidad "
                            + "vigente para la asignatura indicada");
        }
    }

    private boolean cubreAsignatura(
            Idoneidad idoneidad,
            Asignatura asignatura) {

        if (idoneidad.getAsignatura() != null) {

            return idoneidad.getAsignatura()
                    .getId()
                    .equals(asignatura.getId());
        }

        return idoneidad.getArea() != null
                && asignatura.getArea() != null
                && idoneidad.getArea()
                .getId()
                .equals(asignatura.getArea().getId());
    }

    private boolean estaVigente(
            Idoneidad idoneidad,
            LocalDate fecha) {

        boolean yaInicio =
                !idoneidad.getVigenteDesde().isAfter(fecha);

        boolean noHaFinalizado =
                idoneidad.getVigenteHasta() == null
                        || !idoneidad.getVigenteHasta()
                        .isBefore(fecha);

        return yaInicio && noHaFinalizado;
    }

    private AsignacionAcademica obtenerAsignacion(UUID id) {

        return asignacionAcademicaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignación académica no existe"));
    }

    // Q-01: resumen académico.
    @Transactional(readOnly = true)
    public ResumenAsignacionAcademicaResponse
    generateResumenAsignacionAcademica(
            UUID anioEscolarId,
            UUID grupoId) {

        AnioEscolar anioEscolar = anioEscolarRepository
                .findById(anioEscolarId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Año escolar no encontrado"));

        Grupo grupo = grupoRepository
                .findById(grupoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Grupo no encontrado"));

        validarAnioEscolarGrupo(anioEscolar, grupo);

        Grado grado = grupo.getGrado();

        List<PlanEstudios> planEstudiosList =
                planEstudiosRepository
                        .findByAnioEscolar_IdAndGrado_Id(
                                anioEscolar.getId(),
                                grado.getId());

        if (planEstudiosList.isEmpty()) {

            throw new IllegalArgumentException(
                    "No se encontraron planes de estudio "
                            + "para el año escolar y grupo especificados");
        }

        int horasRequeridas =
                planEstudiosList.stream()
                        .mapToInt(PlanEstudios::getHorasSemanales)
                        .sum();

        EstadoAsignacionAcademica estado;

        if (horasRequeridas
                < grado.getHorasSemanalesEsperadas()) {

            estado = EstadoAsignacionAcademica.BORRADOR;

        } else {

            estado = EstadoAsignacionAcademica.CONFIRMADA;
        }

        return new ResumenAsignacionAcademicaResponse(
                grupo.getCodigo(),
                grado.getNombre(),
                horasRequeridas,
                grado.getHorasSemanalesEsperadas(),
                estado);
    }

    // HU-14: registra el cambio en la misma transacción.
    private void auditarAsignacion(
            String accion,
            AsignacionAcademica asignacion,
            Object antes,
            Object despues,
            String correoUsuario) {

        if (registroAuditoriaService == null) {
            return;
        }

        String correo = correoUsuario;

        if (correo == null) {

            Authentication auth =
                    SecurityContextHolder.getContext()
                            .getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {

                throw new IllegalStateException(
                        "No hay usuario autenticado para auditar");
            }

            correo = auth.getName();
        }

        Usuario usuario =
                usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no identificado "
                                                + "para auditoria"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "AsignacionAcademica",
                asignacion.getId(),
                antes,
                despues,
                null
        );
    }

    // HU-14: datos de negocio sin serializar entidades JPA.
    private Map<String, Object> datosAsignacion(
            AsignacionAcademica asignacion) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        datos.put("id", asignacion.getId());

        datos.put(
                "anioEscolarId",
                asignacion.getAnioEscolar().getId());

        datos.put(
                "grupoId",
                asignacion.getGrupo().getId());

        datos.put(
                "docenteId",
                asignacion.getDocente().getId());

        datos.put(
                "asignaturaId",
                asignacion.getAsignatura().getId());

        datos.put(
                "estado",
                asignacion.getEstado().name());

        datos.put(
                "horasSemanales",
                asignacion.getHorasSemanales());

        return datos;
    }
}
