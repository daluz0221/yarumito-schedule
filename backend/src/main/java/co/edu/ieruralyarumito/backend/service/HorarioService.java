package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignacionPendienteResponse;
import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.ResumenHorarioResponse;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.RestriccionDocente;
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;
import co.edu.ieruralyarumito.backend.exception.ConflictoHorarioException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.mapper.HorarioMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    private final GrupoRepository grupoRepository;
    private final AsignacionAcademicaRepository asignacionAcademicaRepository;
    private final ClaseProgramadaRepository claseProgramadaRepository;
    private final HorarioRepository horarioRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;
    private final AulaRepository aulaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final HorarioMapper horarioMapper;
    private final ConflictoHorarioRegistroService conflictoHorarioRegistroService;
    private final RestriccionDocenteRepository restriccionDocenteRepository;

    private RegistroAuditoriaService registroAuditoriaService;

    @Autowired
    public HorarioService(
            GrupoRepository grupoRepository,
            AsignacionAcademicaRepository asignacionAcademicaRepository,
            ClaseProgramadaRepository claseProgramadaRepository,
            HorarioRepository horarioRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            AulaRepository aulaRepository,
            UsuarioRepository usuarioRepository,
            PlanEstudiosRepository planEstudiosRepository,
            HorarioMapper horarioMapper,
            ConflictoHorarioRegistroService conflictoHorarioRegistroService,
            RestriccionDocenteRepository restriccionDocenteRepository) {

        this.grupoRepository = grupoRepository;
        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.claseProgramadaRepository = claseProgramadaRepository;
        this.horarioRepository = horarioRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.aulaRepository = aulaRepository;
        this.usuarioRepository = usuarioRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.horarioMapper = horarioMapper;
        this.conflictoHorarioRegistroService =
                conflictoHorarioRegistroService;
        this.restriccionDocenteRepository =
                Objects.requireNonNull(restriccionDocenteRepository);
    }

    // Constructor conservado para compatibilidad con pruebas existentes.
    HorarioService(
            GrupoRepository grupoRepository,
            AsignacionAcademicaRepository asignacionAcademicaRepository,
            ClaseProgramadaRepository claseProgramadaRepository,
            HorarioRepository horarioRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            AulaRepository aulaRepository,
            UsuarioRepository usuarioRepository,
            PlanEstudiosRepository planEstudiosRepository,
            HorarioMapper horarioMapper,
            ConflictoHorarioRegistroService conflictoHorarioRegistroService) {

        this.grupoRepository = grupoRepository;
        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.claseProgramadaRepository = claseProgramadaRepository;
        this.horarioRepository = horarioRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.aulaRepository = aulaRepository;
        this.usuarioRepository = usuarioRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.horarioMapper = horarioMapper;
        this.conflictoHorarioRegistroService =
                conflictoHorarioRegistroService;
        this.restriccionDocenteRepository = null;
    }

    // HU-14: integración con el servicio de auditoría.
    @Autowired
    public void configurarAuditoria(
            RegistroAuditoriaService servicio) {

        this.registroAuditoriaService =
                Objects.requireNonNull(servicio);
    }

    // =====================================================
    // CONSULTA DEL TABLERO DEL GRUPO
    // =====================================================

    @Transactional(readOnly = true)
    public HorarioGrupoResponse obtenerTablero(UUID grupoId) {

        Grupo grupo = obtenerGrupo(grupoId);

        List<AsignacionAcademica> asignaciones =
                asignacionAcademicaRepository
                        .findByAnioEscolar_IdAndGrupo_Id(
                                grupo.getAnioEscolar().getId(),
                                grupo.getId());

        Optional<Horario> horarioActual =
                horarioRepository
                        .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                                grupo.getAnioEscolar().getId(),
                                grupo.getSede().getId());

        List<ClaseProgramada> clases = horarioActual
                .map(horario -> claseProgramadaRepository
                        .findByHorarioAndGrupo(
                                horario.getId(),
                                grupo.getId()))
                .orElse(List.of());

        Map<UUID, Long> bloquesPorAsignacion = clases.stream()
                .collect(Collectors.groupingBy(
                        clase -> clase.getAsignacionAcademicaId().getId(),
                        Collectors.counting()));

        List<AsignacionPendienteResponse> pendientes =
                asignaciones.stream()
                        .map(asignacion ->
                                horarioMapper.toPendienteResponse(
                                        asignacion,
                                        bloquesPorAsignacion
                                                .getOrDefault(
                                                        asignacion.getId(),
                                                        0L)
                                                .intValue()))
                        .filter(pendiente ->
                                pendiente.getHorasPendientes() > 0)
                        .sorted(Comparator.comparing(
                                AsignacionPendienteResponse::
                                        getAsignaturaNombre))
                        .toList();

        int totalAsignaciones = asignaciones.size();

        int totalHoras = asignaciones.stream()
                .mapToInt(AsignacionAcademica::getHorasSemanales)
                .sum();

        int horasProgramadas = clases.size();

        int horasPendientes = pendientes.stream()
                .mapToInt(AsignacionPendienteResponse::getHorasPendientes)
                .sum();

        int asignacionesProgramadas =
                (int) asignaciones.stream()
                        .filter(asignacion -> {
                            int programadas = bloquesPorAsignacion
                                    .getOrDefault(
                                            asignacion.getId(),
                                            0L)
                                    .intValue();

                            return programadas >=
                                    asignacion.getHorasSemanales();
                        })
                        .count();

        int advertencias = (int) clases.stream()
                .filter(clase -> haySobreaforo(
                        clase.getAsignacionAcademicaId().getGrupo(),
                        clase.getAulaId()))
                .count();

        ResumenHorarioResponse resumen =
                new ResumenHorarioResponse();

        resumen.setAsignacionesProgramadas(
                asignacionesProgramadas);
        resumen.setTotalAsignaciones(totalAsignaciones);
        resumen.setHorasProgramadas(horasProgramadas);
        resumen.setTotalHoras(totalHoras);
        resumen.setHorasPendientes(horasPendientes);
        resumen.setConflictos(0);
        resumen.setAdvertencias(advertencias);

        Turno turno = resolverTurno(grupo);

        List<FranjaHorariaResponse> franjas =
                turno == null
                        ? List.of()
                        : franjaHorariaRepository
                        .findByTurno_IdOrderByOrdenAsc(
                                turno.getId())
                        .stream()
                        .map(horarioMapper::toFranjaResponse)
                        .toList();

        List<ClaseProgramadaResponse> clasesProgramadas =
                clases.stream()
                        .sorted(Comparator
                                .comparing(ClaseProgramada::getDia)
                                .thenComparing(clase ->
                                        clase.getFranjaHorariaId()
                                                .getOrden()))
                        .map(horarioMapper::toClaseResponse)
                        .toList();

        HorarioGrupoResponse response =
                new HorarioGrupoResponse();

        response.setGrupoId(grupo.getId());
        response.setGrupoCodigo(grupo.getCodigo());
        response.setGradoId(grupo.getGrado().getId());
        response.setGradoNombre(grupo.getGrado().getNombre());
        response.setAnioEscolarId(
                grupo.getAnioEscolar().getId());
        response.setAnioEscolar(
                grupo.getAnioEscolar().getAnio());
        response.setSedeId(grupo.getSede().getId());
        response.setSedeNombre(grupo.getSede().getNombre());

        horarioActual.ifPresent(horario -> {
            response.setHorarioId(horario.getId());
            response.setVersionHorario(horario.getVersion());
        });

        if (turno != null) {
            response.setTurnoId(turno.getId());
            response.setTurnoNombre(turno.getNombre());
        }

        response.setEstadoHorario(determinarEstadoHorario(
                horarioActual.orElse(null),
                asignaciones));

        response.setResumen(resumen);
        response.setFranjas(franjas);
        response.setClasesProgramadas(clasesProgramadas);
        response.setAsignacionesPendientes(pendientes);

        return response;
    }

    // =====================================================
    // CREAR CLASE PROGRAMADA
    // =====================================================

    @Transactional(
            noRollbackFor = ConflictoHorarioException.class)
    public ClaseProgramadaResponse crearClaseProgramada(
            CrearClaseProgramadaRequest request,
            String correoUsuario) {

        AsignacionAcademica asignacion = obtenerAsignacion(
                request.getAsignacionAcademicaId());

        Grupo grupo = asignacion.getGrupo();

        FranjaHoraria franja = obtenerFranja(
                request.getFranjaHorariaId());

        validarFranjaProgramable(franja);

        Aula aula = obtenerAula(request.getAulaId());

        // H-08: aulas activas y pertenecientes a la sede.
        validarAulaParaGrupo(grupo, aula);

        Horario horario = obtenerOCrearHorario(
                grupo,
                correoUsuario);

        // H-10: proteger horarios publicados o archivados.
        validarHorarioEditable(horario);

        // H-02: máximo dos clases de una asignatura por día.
        validarMaximoDiarioAsignatura(
                horario,
                asignacion,
                request.getDia(),
                null);

        // H-01: grupo ocupado.
        validarGrupoDisponible(
                horario,
                grupo,
                request.getDia(),
                franja,
                null);

        // Docente ocupado.
        validarDocenteDisponible(
                horario,
                asignacion,
                request.getDia(),
                franja,
                null);

        // H-06B: disponibilidad docente.
        validarRestriccionDocente(
                horario,
                asignacion,
                request.getDia(),
                franja);

        // Aula ocupada.
        validarAulaDisponible(
                horario,
                aula,
                request.getDia(),
                franja,
                null);

        // H-07: carga máxima semanal.
        long cargaActual = obtenerCargaSemanalVigente(
                horario.getAnioEscolarId().getId(),
                asignacion.getDocente().getId()).size();

        if (cargaActual >= 32) {

            throw new RelacionAcademicaInvalidaException(
                    "El docente alcanzó el máximo de 32 bloques "
                            + "semanales (22 ordinarios y 10 extra)");
        }

        ClaseProgramada clase = new ClaseProgramada();

        clase.setHorarioId(horario);
        clase.setAsignacionAcademicaId(asignacion);
        clase.setDia(request.getDia());
        clase.setFranjaHorariaId(franja);
        clase.setAulaId(aula);
        clase.setEsHoraExtra(cargaActual >= 22);
        clase.setBloqueada(false);

        ClaseProgramada guardada =
                claseProgramadaRepository.save(clase);

        recalcularHorasExtrasVigentes(
                horario.getAnioEscolarId().getId(),
                asignacion.getDocente().getId());

        ClaseProgramadaResponse response =
                horarioMapper.toClaseResponse(guardada);

        response.setWarnings(calcularAdvertencias(
                horario,
                grupo,
                aula));

        // HU-14: registrar creación exitosa.
        if (registroAuditoriaService != null) {

            auditarClase(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardada.getId(),
                    null,
                    datosClase(guardada),
                    correoUsuario
            );
        }

        return response;
    }

    // =====================================================
    // ACTUALIZAR CLASE PROGRAMADA
    // =====================================================

    @Transactional(
            noRollbackFor = ConflictoHorarioException.class)
    public ClaseProgramadaResponse actualizarClaseProgramada(
            UUID id,
            ActualizarClaseProgramadaRequest request) {

        ClaseProgramada clase = obtenerClase(id);

        // HU-14: conservar el estado anterior.
        Map<String, Object> datosAntesAuditoria =
                registroAuditoriaService == null
                        ? null
                        : datosClase(clase);

        // H-10: proteger clases bloqueadas o de horarios cerrados.
        validarClaseEditable(clase);

        FranjaHoraria franja = obtenerFranja(
                request.getFranjaHorariaId());

        validarFranjaProgramable(franja);

        Aula aula = obtenerAula(request.getAulaId());

        AsignacionAcademica asignacion =
                clase.getAsignacionAcademicaId();

        Horario horario = clase.getHorarioId();

        // H-08: validar aula.
        validarAulaParaGrupo(
                asignacion.getGrupo(),
                aula);

        // H-01: grupo ocupado.
        validarGrupoDisponible(
                horario,
                asignacion.getGrupo(),
                request.getDia(),
                franja,
                clase.getId());

        // Docente ocupado.
        validarDocenteDisponible(
                horario,
                asignacion,
                request.getDia(),
                franja,
                clase.getId());

        // Restricción del docente.
        validarRestriccionDocente(
                horario,
                asignacion,
                request.getDia(),
                franja);

        // Aula ocupada.
        validarAulaDisponible(
                horario,
                aula,
                request.getDia(),
                franja,
                clase.getId());

        // H-02: límite diario de asignatura.
        validarMaximoDiarioAsignatura(
                horario,
                asignacion,
                request.getDia(),
                clase.getId());

        clase.setDia(request.getDia());
        clase.setFranjaHorariaId(franja);
        clase.setAulaId(aula);

        ClaseProgramada guardada =
                claseProgramadaRepository.save(clase);

        // H-07: mantener carga semanal.
        recalcularHorasExtrasVigentes(
                horario.getAnioEscolarId().getId(),
                asignacion.getDocente().getId());

        ClaseProgramadaResponse response =
                horarioMapper.toClaseResponse(guardada);

        response.setWarnings(calcularAdvertencias(
                horario,
                asignacion.getGrupo(),
                aula));

        // HU-14: registrar actualización.
        if (registroAuditoriaService != null) {

            auditarClase(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    guardada.getId(),
                    datosAntesAuditoria,
                    datosClase(guardada),
                    null
            );
        }

        return response;
    }

    // =====================================================
    // ELIMINAR CLASE PROGRAMADA
    // =====================================================

    @Transactional
    public void eliminarClaseProgramada(UUID id) {

        ClaseProgramada clase = obtenerClase(id);

        Map<String, Object> datosAntesAuditoria =
                registroAuditoriaService == null
                        ? null
                        : datosClase(clase);

        // H-10: impedir eliminar clases protegidas.
        validarClaseEditable(clase);

        UUID anioEscolarId =
                clase.getHorarioId()
                        .getAnioEscolarId()
                        .getId();

        UUID docenteId =
                clase.getAsignacionAcademicaId()
                        .getDocente()
                        .getId();

        claseProgramadaRepository.delete(clase);

        // H-07: redistribuir horas extras.
        recalcularHorasExtrasVigentes(
                anioEscolarId,
                docenteId);

        // HU-14: registrar eliminación.
        if (registroAuditoriaService != null) {

            auditarClase(
                    RegistroAuditoriaService.ACCION_DELETE,
                    id,
                    datosAntesAuditoria,
                    null,
                    null
            );
        }
    }

    // =====================================================
    // H-07: CARGA SEMANAL VIGENTE
    // =====================================================

    private List<ClaseProgramada> obtenerCargaSemanalVigente(
            UUID anioEscolarId,
            UUID docenteId) {

        List<ClaseProgramada> todas =
                claseProgramadaRepository
                        .findByDocenteAndAnioEscolar(
                                anioEscolarId,
                                docenteId);

        if (todas == null || todas.isEmpty()) {
            return List.of();
        }

        Map<UUID, UUID> horarioVigentePorSede =
                new HashMap<>();

        Set<UUID> sedesSinHorario = new HashSet<>();

        List<ClaseProgramada> vigentes =
                new ArrayList<>();

        for (ClaseProgramada clase : todas) {

            Horario horarioClase = clase.getHorarioId();

            if (horarioClase == null
                    || horarioClase.getSedeId() == null
                    || horarioClase.getSedeId().getId() == null
                    || clase.getFranjaHorariaId() == null
                    || clase.getFranjaHorariaId().isEsDescanso()) {

                continue;
            }

            UUID sedeId =
                    horarioClase.getSedeId().getId();

            if (!horarioVigentePorSede.containsKey(sedeId)
                    && !sedesSinHorario.contains(sedeId)) {

                Optional<Horario> ultimo =
                        horarioRepository
                                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                                        anioEscolarId,
                                        sedeId);

                if (ultimo.isPresent()) {

                    horarioVigentePorSede.put(
                            sedeId,
                            ultimo.get().getId());

                } else {

                    sedesSinHorario.add(sedeId);
                }
            }

            if (Objects.equals(
                    horarioVigentePorSede.get(sedeId),
                    horarioClase.getId())) {

                vigentes.add(clase);
            }
        }

        return vigentes;
    }

    // =====================================================
    // H-07: RECALCULAR HORAS EXTRAS
    // =====================================================

    private void recalcularHorasExtrasVigentes(
            UUID anioEscolarId,
            UUID docenteId) {

        List<ClaseProgramada> clases =
                new ArrayList<>(
                        obtenerCargaSemanalVigente(
                                anioEscolarId,
                                docenteId));

        clases.sort(Comparator
                .comparing((ClaseProgramada clase) ->
                        clase.getDia() == null
                                ? 99
                                : clase.getDia().ordinal())
                .thenComparing(
                        clase ->
                                clase.getFranjaHorariaId().getOrden(),
                        Comparator.nullsLast(
                                Comparator.naturalOrder()))
                .thenComparing(
                        clase ->
                                clase.getId() == null
                                        ? ""
                                        : clase.getId().toString()));

        for (int i = 0; i < clases.size(); i++) {

            ClaseProgramada clase = clases.get(i);

            boolean esExtra = i >= 22;

            if (clase.isEsHoraExtra() != esExtra) {

                clase.setEsHoraExtra(esExtra);
                claseProgramadaRepository.save(clase);
            }
        }
    }

    // =====================================================
    // CONSULTAS DE ENTIDADES
    // =====================================================

    private Grupo obtenerGrupo(UUID grupoId) {

        return grupoRepository.findById(grupoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grupo no existe"));
    }

    private AsignacionAcademica obtenerAsignacion(
            UUID asignacionId) {

        return asignacionAcademicaRepository
                .findById(asignacionId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignación académica no existe"));
    }

    private FranjaHoraria obtenerFranja(UUID franjaId) {

        return franjaHorariaRepository.findById(franjaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La franja horaria no existe"));
    }

    private Aula obtenerAula(UUID aulaId) {

        return aulaRepository.findById(aulaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El aula no existe"));
    }

    private ClaseProgramada obtenerClase(UUID id) {

        return claseProgramadaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La clase programada no existe"));
    }

    // =====================================================
    // VALIDACIONES ACADÉMICAS
    // =====================================================

    private void validarFranjaProgramable(
            FranjaHoraria franja) {

        if (franja.isEsDescanso()) {

            throw new RelacionAcademicaInvalidaException(
                    "La franja seleccionada corresponde "
                            + "a un descanso y no es programable");
        }
    }

    // H-02: máximo dos bloques diarios de una asignatura.
    private void validarMaximoDiarioAsignatura(
            Horario horario,
            AsignacionAcademica asignacion,
            DiasSemana dia,
            UUID claseIdExcluir) {

        List<ClaseProgramada> clases =
                claseProgramadaRepository
                        .findByHorarioAndGrupo(
                                horario.getId(),
                                asignacion.getGrupo().getId());

        UUID asignaturaId =
                asignacion.getAsignatura().getId();

        long total = clases.stream()
                .filter(clase ->
                        !Objects.equals(
                                clase.getId(),
                                claseIdExcluir))
                .filter(clase ->
                        clase.getDia() == dia)
                .filter(clase ->
                        !clase.getFranjaHorariaId()
                                .isEsDescanso())
                .filter(clase ->
                        Objects.equals(
                                clase.getAsignacionAcademicaId()
                                        .getAsignatura()
                                        .getId(),
                                asignaturaId))
                .count();

        if (total >= 2) {

            throw new RelacionAcademicaInvalidaException(
                    "El grupo ya tiene dos clases de esta "
                            + "asignatura programadas para el "
                            + "día seleccionado");
        }
    }

    // H-10: proteger horarios publicados y archivados.
    private void validarHorarioEditable(Horario horario) {

        EstadoHorario estado = horario.getEstado();

        if (estado == EstadoHorario.PUBLICADO
                || estado == EstadoHorario.ARCHIVADO) {

            throw new RelacionAcademicaInvalidaException(
                    "No se pueden modificar clases de un "
                            + "horario publicado o archivado");
        }
    }

    // H-10: proteger clases bloqueadas.
    private void validarClaseEditable(
            ClaseProgramada clase) {

        validarHorarioEditable(clase.getHorarioId());

        if (clase.isBloqueada()) {

            throw new RelacionAcademicaInvalidaException(
                    "La clase programada está bloqueada "
                            + "y no puede modificarse ni eliminarse");
        }
    }

    // H-08: validar aula activa y sede correcta.
    private void validarAulaParaGrupo(
            Grupo grupo,
            Aula aula) {

        if (!aula.isActiva()) {

            throw new RelacionAcademicaInvalidaException(
                    "No se puede programar una clase "
                            + "en un aula inactiva");
        }

        if (aula.getSede() == null
                || grupo.getSede() == null
                || !Objects.equals(
                aula.getSede().getId(),
                grupo.getSede().getId())) {

            throw new RelacionAcademicaInvalidaException(
                    "El aula seleccionada no pertenece "
                            + "a la sede del grupo");
        }
    }

    // H-01: grupo ocupado en una franja.
    private void validarGrupoDisponible(
            Horario horario,
            Grupo grupo,
            DiasSemana dia,
            FranjaHoraria franja,
            UUID claseId) {

        boolean ocupado = claseId == null
                ? claseProgramadaRepository
                .existeGrupoOcupado(
                        horario.getId(),
                        grupo.getId(),
                        dia,
                        franja.getId())
                : claseProgramadaRepository
                .existeGrupoOcupadoExcluyendo(
                        horario.getId(),
                        grupo.getId(),
                        dia,
                        franja.getId(),
                        claseId);

        if (ocupado) {

            throw new ConflictoHorarioException(
                    "GRUPO_OCUPADO",
                    "El grupo ya tiene una clase "
                            + "programada en esta misma franja.");
        }
    }

    // H-06B: restricciones del docente.
    private void validarRestriccionDocente(
            Horario horario,
            AsignacionAcademica asignacion,
            DiasSemana dia,
            FranjaHoraria franja) {

        if (restriccionDocenteRepository == null) {
            return;
        }

        DiaSemana diaRestriccion =
                DiaSemana.valueOf(dia.name());

        Optional<RestriccionDocente> restriccion =
                restriccionDocenteRepository
                        .findByDocente_IdAndAnioEscolar_IdAndDiaAndFranjaHoraria_Id(
                                asignacion.getDocente().getId(),
                                horario.getAnioEscolarId().getId(),
                                diaRestriccion,
                                franja.getId());

        if (restriccion.isPresent()
                && restriccion.get().getTipo()
                == TipoRestriccionDocente.NO_DISPONIBLE) {

            throw new ConflictoHorarioException(
                    "DOCENTE_NO_DISPONIBLE",
                    "El docente tiene registrada una "
                            + "restricción de no disponibilidad "
                            + "para el día y la franja seleccionados.");
        }
    }

    // Docente ocupado.
    private void validarDocenteDisponible(
            Horario horario,
            AsignacionAcademica asignacion,
            DiasSemana dia,
            FranjaHoraria franja,
            UUID claseId) {

        UUID anioEscolarId =
                horario.getAnioEscolarId().getId();

        UUID docenteId =
                asignacion.getDocente().getId();

        UUID franjaId = franja.getId();

        boolean ocupado = claseId == null
                ? claseProgramadaRepository
                .existeDocenteOcupado(
                        anioEscolarId,
                        docenteId,
                        dia,
                        franjaId)
                : claseProgramadaRepository
                .existeDocenteOcupadoExcluyendo(
                        anioEscolarId,
                        docenteId,
                        dia,
                        franjaId,
                        claseId);

        if (ocupado) {

            String mensaje =
                    "El docente ya tiene una clase "
                            + "programada en esta misma franja.";

            conflictoHorarioRegistroService
                    .registrarDocenteOcupado(
                            horario,
                            mensaje);

            throw new ConflictoHorarioException(
                    "DOCENTE_OCUPADO",
                    mensaje);
        }
    }

    // Aula ocupada.
    private void validarAulaDisponible(
            Horario horario,
            Aula aula,
            DiasSemana dia,
            FranjaHoraria franja,
            UUID claseId) {

        UUID horarioId = horario.getId();
        UUID aulaId = aula.getId();
        UUID franjaId = franja.getId();

        boolean ocupada = claseId == null
                ? claseProgramadaRepository
                .existeAulaOcupada(
                        horarioId,
                        aulaId,
                        dia,
                        franjaId)
                : claseProgramadaRepository
                .existeAulaOcupadaExcluyendo(
                        horarioId,
                        aulaId,
                        dia,
                        franjaId,
                        claseId);

        if (ocupada) {

            String mensaje =
                    "El aula seleccionada ya está ocupada "
                            + "en la franja seleccionada.";

            conflictoHorarioRegistroService
                    .registrarAulaOcupada(
                            horario,
                            mensaje);

            throw new ConflictoHorarioException(
                    "AULA_OCUPADA",
                    mensaje);
        }
    }

    // Advertencia no bloqueante por capacidad.
    private List<String> calcularAdvertencias(
            Horario horario,
            Grupo grupo,
            Aula aula) {

        if (haySobreaforo(grupo, aula)) {

            String mensaje =
                    "La cantidad de estudiantes supera "
                            + "la capacidad del aula.";

            conflictoHorarioRegistroService
                    .registrarCapacidadAula(
                            horario,
                            mensaje);

            return List.of(mensaje);
        }

        return List.of();
    }

    private boolean haySobreaforo(
            Grupo grupo,
            Aula aula) {

        Integer cantidadEstudiantes =
                grupo.getCantidadEstudiantes();

        Integer capacidad = aula.getCapacidad();

        return cantidadEstudiantes != null
                && capacidad != null
                && cantidadEstudiantes > capacidad;
    }

    // =====================================================
    // CREACIÓN Y CONSULTA DEL HORARIO
    // =====================================================

    private Horario obtenerOCrearHorario(
            Grupo grupo,
            String correoUsuario) {

        return horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        grupo.getAnioEscolar().getId(),
                        grupo.getSede().getId())
                .orElseGet(() ->
                        crearHorario(
                                grupo,
                                correoUsuario));
    }

    private Horario crearHorario(
            Grupo grupo,
            String correoUsuario) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(correoUsuario)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El usuario autenticado no existe"));

        Horario horario = new Horario();

        horario.setAnioEscolarId(
                grupo.getAnioEscolar());

        horario.setSedeId(
                grupo.getSede());

        horario.setVersion(1);

        horario.setNombre(
                "Horario "
                        + grupo.getAnioEscolar().getAnio()
                        + " - "
                        + grupo.getSede().getNombre());

        horario.setEstado(EstadoHorario.BORRADOR);
        horario.setFechaPublicacion(null);
        horario.setCreadoPor(usuario);

        return horarioRepository.save(horario);
    }

    private Turno resolverTurno(
            Grupo grupo) {

        return planEstudiosRepository
                .findByAnioEscolar_IdAndGrado_Id(
                        grupo.getAnioEscolar().getId(),
                        grupo.getGrado().getId())
                .stream()
                .map(PlanEstudios::getTurno)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    // H-03: conservar el estado real del horario.
    static EstadoHorario determinarEstadoHorario(
            Horario horario,
            List<AsignacionAcademica> asignaciones) {

        if (horario == null
                || horario.getEstado() == null) {

            return EstadoHorario.BORRADOR;
        }

        return horario.getEstado();
    }

    // =====================================================
    // HU-14: REGISTRO AUTOMÁTICO DE AUDITORÍA
    // =====================================================

    /**
     * Registra cambios de clases programadas
     * dentro de la transacción de negocio.
     */
    private void auditarClase(
            String accion,
            UUID id,
            Object antes,
            Object despues,
            String correoUsuario) {

        // Compatibilidad con las pruebas unitarias existentes.
        if (registroAuditoriaService == null) {
            return;
        }

        String correo = correoUsuario;

        if (correo == null) {

            Authentication auth =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (auth == null
                    || !auth.isAuthenticated()) {

                throw new IllegalStateException(
                        "No hay usuario autenticado para auditar");
            }

            correo = auth.getName();
        }

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(correo)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no identificado "
                                                + "para auditoria"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "ClaseProgramada",
                id,
                antes,
                despues,
                null
        );
    }

    /**
     * Obtiene una instantánea mínima de la clase.
     * No serializa entidades ni relaciones JPA completas.
     */
    private Map<String, Object> datosClase(
            ClaseProgramada clase) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        datos.put(
                "id",
                clase.getId());

        datos.put(
                "horarioId",
                clase.getHorarioId().getId());

        datos.put(
                "asignacionAcademicaId",
                clase.getAsignacionAcademicaId().getId());

        datos.put(
                "dia",
                clase.getDia() == null
                        ? null
                        : clase.getDia().name());

        datos.put(
                "franjaHorariaId",
                clase.getFranjaHorariaId().getId());

        datos.put(
                "aulaId",
                clase.getAulaId().getId());

        datos.put(
                "esHoraExtra",
                clase.isEsHoraExtra());

        return datos;
    }
}
