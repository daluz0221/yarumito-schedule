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
import co.edu.ieruralyarumito.backend.entity.Turno;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
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
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Contiene la lógica de construcción del horario (M-11).
// Valida conflictos de docente y aula, y genera advertencias de aforo.
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

    public HorarioService(
            GrupoRepository grupoRepository,
            AsignacionAcademicaRepository asignacionAcademicaRepository,
            ClaseProgramadaRepository claseProgramadaRepository,
            HorarioRepository horarioRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            AulaRepository aulaRepository,
            UsuarioRepository usuarioRepository,
            PlanEstudiosRepository planEstudiosRepository,
            HorarioMapper horarioMapper) {

        this.grupoRepository = grupoRepository;
        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.claseProgramadaRepository = claseProgramadaRepository;
        this.horarioRepository = horarioRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.aulaRepository = aulaRepository;
        this.usuarioRepository = usuarioRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.horarioMapper = horarioMapper;
    }

    // Construye el tablero completo de un grupo: rejilla, contadores y
    // panel de asignaciones con horas pendientes.
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

        // Cada bloque de la rejilla representa una hora académica.
        Map<UUID, Long> bloquesPorAsignacion = clases.stream()
                .collect(Collectors.groupingBy(
                        clase -> clase.getAsignacionAcademicaId().getId(),
                        Collectors.counting()));

        List<AsignacionPendienteResponse> pendientes = asignaciones.stream()
                .map(asignacion -> horarioMapper.toPendienteResponse(
                        asignacion,
                        bloquesPorAsignacion
                                .getOrDefault(asignacion.getId(), 0L)
                                .intValue()))
                .filter(pendiente -> pendiente.getHorasPendientes() > 0)
                .sorted(Comparator.comparing(
                        AsignacionPendienteResponse::getAsignaturaNombre))
                .toList();

        int totalAsignaciones = asignaciones.size();
        int totalHoras = asignaciones.stream()
                .mapToInt(AsignacionAcademica::getHorasSemanales)
                .sum();
        int horasProgramadas = clases.size();
        int horasPendientes = pendientes.stream()
                .mapToInt(AsignacionPendienteResponse::getHorasPendientes)
                .sum();

        // Se considera programada la asignación cuyas horas ya fueron
        // ubicadas por completo en la rejilla.
        int asignacionesProgramadas = (int) asignaciones.stream()
                .filter(asignacion -> {
                    int programadas = bloquesPorAsignacion
                            .getOrDefault(asignacion.getId(), 0L)
                            .intValue();
                    return programadas >= asignacion.getHorasSemanales();
                })
                .count();

        int advertencias = (int) clases.stream()
                .filter(clase -> haySobreaforo(
                        clase.getAsignacionAcademicaId().getGrupo(),
                        clase.getAulaId()))
                .count();

        ResumenHorarioResponse resumen = new ResumenHorarioResponse();
        resumen.setAsignacionesProgramadas(asignacionesProgramadas);
        resumen.setTotalAsignaciones(totalAsignaciones);
        resumen.setHorasProgramadas(horasProgramadas);
        resumen.setTotalHoras(totalHoras);
        resumen.setHorasPendientes(horasPendientes);
        resumen.setConflictos(0);
        resumen.setAdvertencias(advertencias);

        Turno turno = resolverTurno(grupo);

        List<FranjaHorariaResponse> franjas = turno == null
                ? List.of()
                : franjaHorariaRepository
                        .findByTurno_IdOrderByOrdenAsc(turno.getId())
                        .stream()
                        .map(horarioMapper::toFranjaResponse)
                        .toList();

        List<ClaseProgramadaResponse> clasesProgramadas = clases.stream()
                .sorted(Comparator
                        .comparing(ClaseProgramada::getDia)
                        .thenComparing(clase ->
                                clase.getFranjaHorariaId().getOrden()))
                .map(horarioMapper::toClaseResponse)
                .toList();

        HorarioGrupoResponse response = new HorarioGrupoResponse();
        response.setGrupoId(grupo.getId());
        response.setGrupoCodigo(grupo.getCodigo());
        response.setGradoId(grupo.getGrado().getId());
        response.setGradoNombre(grupo.getGrado().getNombre());
        response.setAnioEscolarId(grupo.getAnioEscolar().getId());
        response.setAnioEscolar(grupo.getAnioEscolar().getAnio());
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

    // Ubica una asignación académica en un día, franja y aula.
    @Transactional
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

        Horario horario = obtenerOCrearHorario(
                grupo,
                correoUsuario);

        // Validaciones bloqueantes antes de persistir.
        validarDocenteDisponible(
                horario,
                asignacion,
                request.getDia(),
                franja,
                null);
        validarAulaDisponible(
                horario,
                aula,
                request.getDia(),
                franja,
                null);

        ClaseProgramada clase = new ClaseProgramada();
        clase.setHorarioId(horario);
        clase.setAsignacionAcademicaId(asignacion);
        clase.setDia(request.getDia());
        clase.setFranjaHorariaId(franja);
        clase.setAulaId(aula);
        clase.setEsHoraExtra(false);
        clase.setBloqueada(false);

        ClaseProgramadaResponse response = horarioMapper.toClaseResponse(
                claseProgramadaRepository.save(clase));
        response.setWarnings(calcularAdvertencias(grupo, aula));

        return response;
    }

    // Mueve un bloque existente. No modifica la asignación académica.
    @Transactional
    public ClaseProgramadaResponse actualizarClaseProgramada(
            UUID id,
            ActualizarClaseProgramadaRequest request) {

        ClaseProgramada clase = obtenerClase(id);

        FranjaHoraria franja = obtenerFranja(
                request.getFranjaHorariaId());
        validarFranjaProgramable(franja);

        Aula aula = obtenerAula(request.getAulaId());

        AsignacionAcademica asignacion = clase.getAsignacionAcademicaId();
        Horario horario = clase.getHorarioId();

        // Validaciones bloqueantes antes de persistir, excluyendo
        // la clase que se está editando.
        validarDocenteDisponible(
                horario,
                asignacion,
                request.getDia(),
                franja,
                clase.getId());
        validarAulaDisponible(
                horario,
                aula,
                request.getDia(),
                franja,
                clase.getId());

        clase.setDia(request.getDia());
        clase.setFranjaHorariaId(franja);
        clase.setAulaId(aula);

        ClaseProgramadaResponse response = horarioMapper.toClaseResponse(
                claseProgramadaRepository.save(clase));
        response.setWarnings(calcularAdvertencias(
                asignacion.getGrupo(),
                aula));

        return response;
    }

    // Elimina el bloque. La hora vuelve a quedar pendiente porque los
    // contadores se calculan a partir de las clases programadas.
    @Transactional
    public void eliminarClaseProgramada(UUID id) {

        claseProgramadaRepository.delete(obtenerClase(id));
    }

    // Obtiene el grupo o lanza el error de no encontrado.
    private Grupo obtenerGrupo(UUID grupoId) {

        return grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El grupo no existe"));
    }

    // Obtiene la asignación académica o lanza el error de no encontrado.
    private AsignacionAcademica obtenerAsignacion(UUID asignacionId) {

        return asignacionAcademicaRepository.findById(asignacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignación académica no existe"));
    }

    // Obtiene la franja horaria o lanza el error de no encontrado.
    private FranjaHoraria obtenerFranja(UUID franjaId) {

        return franjaHorariaRepository.findById(franjaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La franja horaria no existe"));
    }

    // Obtiene el aula o lanza el error de no encontrado.
    private Aula obtenerAula(UUID aulaId) {

        return aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El aula no existe"));
    }

    // Obtiene la clase programada o lanza el error de no encontrado.
    private ClaseProgramada obtenerClase(UUID id) {

        return claseProgramadaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La clase programada no existe"));
    }

    // Regla estricta: los descansos son franjas no programables.
    private void validarFranjaProgramable(FranjaHoraria franja) {

        if (franja.isEsDescanso()) {
            throw new RelacionAcademicaInvalidaException(
                    "La franja seleccionada corresponde a un descanso "
                            + "y no es programable");
        }
    }

    // Valida que el docente no tenga otra clase en el mismo día y franja.
    private void validarDocenteDisponible(
            Horario horario,
            AsignacionAcademica asignacion,
            DiasSemana dia,
            FranjaHoraria franja,
            UUID claseId) {

        UUID anioEscolarId = horario.getAnioEscolarId().getId();
        UUID docenteId = asignacion.getDocente().getId();
        UUID franjaId = franja.getId();

        boolean ocupado = claseId == null
                ? claseProgramadaRepository.existeDocenteOcupado(
                        anioEscolarId,
                        docenteId,
                        dia,
                        franjaId)
                : claseProgramadaRepository.existeDocenteOcupadoExcluyendo(
                        anioEscolarId,
                        docenteId,
                        dia,
                        franjaId,
                        claseId);

        if (ocupado) {
            throw new ConflictoHorarioException(
                    "DOCENTE_OCUPADO",
                    "El docente ya tiene una clase programada "
                            + "en esta misma franja.");
        }
    }

    // Valida que el aula no esté ocupada en el mismo día y franja.
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
                ? claseProgramadaRepository.existeAulaOcupada(
                        horarioId,
                        aulaId,
                        dia,
                        franjaId)
                : claseProgramadaRepository.existeAulaOcupadaExcluyendo(
                        horarioId,
                        aulaId,
                        dia,
                        franjaId,
                        claseId);

        if (ocupada) {
            throw new ConflictoHorarioException(
                    "AULA_OCUPADA",
                    "El aula seleccionada ya está ocupada "
                            + "en la franja seleccionada.");
        }
    }

    // Genera las advertencias no bloqueantes de la clase.
    private List<String> calcularAdvertencias(Grupo grupo, Aula aula) {

        if (haySobreaforo(grupo, aula)) {
            return List.of(
                    "La cantidad de estudiantes supera "
                            + "la capacidad del aula.");
        }

        return List.of();
    }

    // Compara la cantidad de estudiantes del grupo con la capacidad del aula.
    private boolean haySobreaforo(Grupo grupo, Aula aula) {

        Integer cantidadEstudiantes = grupo.getCantidadEstudiantes();
        Integer capacidad = aula.getCapacidad();

        return cantidadEstudiantes != null
                && capacidad != null
                && cantidadEstudiantes > capacidad;
    }

    // Recupera el horario vigente de la sede y año escolar del grupo.
    // Si aún no existe, lo crea en estado BORRADOR.
    private Horario obtenerOCrearHorario(
            Grupo grupo,
            String correoUsuario) {

        return horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        grupo.getAnioEscolar().getId(),
                        grupo.getSede().getId())
                .orElseGet(() -> crearHorario(
                        grupo,
                        correoUsuario));
    }

    // Crea la primera versión del horario para la sede y año escolar.
    private Horario crearHorario(
            Grupo grupo,
            String correoUsuario) {

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no existe"));

        Horario horario = new Horario();
        horario.setAnioEscolarId(grupo.getAnioEscolar());
        horario.setSedeId(grupo.getSede());
        horario.setVersion(1);
        horario.setNombre(
                "Horario " + grupo.getAnioEscolar().getAnio()
                        + " - " + grupo.getSede().getNombre());
        horario.setEstado(EstadoHorario.BORRADOR);
        horario.setFechaPublicacion(null);
        horario.setCreadoPor(usuario);

        return horarioRepository.save(horario);
    }

    // Resuelve el turno del grado a partir de su plan de estudios.
    private Turno resolverTurno(Grupo grupo) {

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

    // El tablero se publica cuando todas las asignaciones del grupo están
    // confirmadas. Mientras falte alguna, permanece en BORRADOR. Un horario
    // archivado conserva ese estado y uno ya publicado no vuelve a borrador.
    static EstadoHorario determinarEstadoHorario(
            Horario horario,
            List<AsignacionAcademica> asignaciones) {

        if (horario != null
                && horario.getEstado() == EstadoHorario.ARCHIVADO) {
            return EstadoHorario.ARCHIVADO;
        }

        boolean todasConfirmadas = asignaciones != null
                && !asignaciones.isEmpty()
                && asignaciones.stream()
                        .allMatch(asignacion -> asignacion.getEstado()
                                == EstadoAsignacionAcademica.CONFIRMADA);

        if (todasConfirmadas
                || (horario != null
                        && horario.getEstado() == EstadoHorario.PUBLICADO)) {
            return EstadoHorario.PUBLICADO;
        }

        return EstadoHorario.BORRADOR;
    }
}
