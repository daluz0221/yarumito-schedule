package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioVersionResponse;
import co.edu.ieruralyarumito.backend.dto.PublicarHorarioResponse;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAnioEscolar;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.ConflictoRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Publica un horario en BORRADOR después de validar que pueda salir
// a producción. Reutiliza la consulta de conflictos y la auditoría
// existentes para no duplicar reglas.
@Service
public class PublicacionHorarioService {

    private final HorarioRepository horarioRepository;
    private final ClaseProgramadaRepository claseProgramadaRepository;
    private final ConflictoRepository conflictoRepository;
    private final ConflictoService conflictoService;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAuditoriaService registroAuditoriaService;
    private final HorarioService horarioService;

    public PublicacionHorarioService(
            HorarioRepository horarioRepository,
            ClaseProgramadaRepository claseProgramadaRepository,
            ConflictoRepository conflictoRepository,
            ConflictoService conflictoService,
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService,
            HorarioService horarioService) {

        this.horarioRepository = horarioRepository;
        this.claseProgramadaRepository = claseProgramadaRepository;
        this.conflictoRepository = conflictoRepository;
        this.conflictoService = conflictoService;
        this.usuarioRepository = usuarioRepository;
        this.registroAuditoriaService = registroAuditoriaService;
        this.horarioService = horarioService;
    }

    // H-10: BORRADOR -> PUBLICADO.
    @Transactional
    public PublicarHorarioResponse publicar(
            UUID horarioId,
            String correoUsuario) {

        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El identificador del horario es obligatorio");
        }

        Horario horario =
                horarioRepository
                        .findById(horarioId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El horario no existe"));

        EstadoHorario estadoActual =
                horario.getEstado();

        if (estadoActual != EstadoHorario.BORRADOR) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo se puede publicar un horario en estado "
                            + "BORRADOR. Estado actual: "
                            + (estadoActual == null
                            ? "DESCONOCIDO"
                            : estadoActual.name()));
        }

        if (!claseProgramadaRepository
                .existsByHorarioId_Id(horarioId)) {

            throw new RelacionAcademicaInvalidaException(
                    "El horario no tiene clases programadas; "
                            + "no es posible publicarlo");
        }

        List<String> incompletos =
                horarioService.describirGruposConHorasPendientes(horario);

        if (!incompletos.isEmpty()) {
            throw new RelacionAcademicaInvalidaException(
                    "No se puede publicar el horario: hay grupos "
                            + "con el horario incompleto: "
                            + String.join(", ", incompletos)
                            + ".");
        }

        // Misma fuente que GET /api/v1/conflictos/horario/{id}/pendientes.
        // Solo los ERROR bloquean; ADVERTENCIA no impide publicar.
        List<ConflictoResponse> pendientes =
                conflictoService.listarPendientes(horarioId);

        List<String> errores =
                pendientes.stream()
                        .filter(conflicto ->
                                conflicto.getSeveridad()
                                        == SeveridadConflicto.ERROR)
                        .map(ConflictoResponse::getMensaje)
                        .distinct()
                        .toList();

        if (!errores.isEmpty()) {
            String detalle = errores.size() == 1
                    ? "1 conflicto bloqueante pendiente"
                    : errores.size() + " conflictos bloqueantes pendientes";

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede publicar el horario: existen "
                            + detalle + ": "
                            + String.join("; ", errores));
        }

        Map<String, Object> datosAntes =
                datosHorario(horario);

        horario.setEstado(EstadoHorario.PUBLICADO);
        horario.setFechaPublicacion(LocalDateTime.now());

        if (horario.getNotas() != null
                && horario.getNotas().startsWith("Borrador de mejora")) {
            horario.setNotas(null);
        }

        Horario guardado =
                horarioRepository.save(horario);

        archivarPublicadosAnteriores(
                guardado,
                correoUsuario);

        registrarAuditoria(
                guardado,
                correoUsuario,
                RegistroAuditoriaService.ACCION_PUBLICAR,
                datosAntes,
                datosHorario(guardado));

        return convertirAResponse(guardado);
    }

    // Copia el horario publicado a un borrador editable.
    // Si ya hay un borrador para el mismo año y sede, se reutiliza.
    @Transactional
    public HorarioVersionResponse crearBorradorDeMejora(
            UUID horarioId,
            String correoUsuario) {

        Horario origen = obtenerHorario(horarioId);

        if (origen.getEstado() != EstadoHorario.PUBLICADO) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo se puede crear un borrador de mejora "
                            + "a partir de un horario PUBLICADO. Estado actual: "
                            + (origen.getEstado() == null
                            ? "DESCONOCIDO"
                            : origen.getEstado().name()));
        }

        validarAnioAbierto(origen);

        if (origen.getAnioEscolarId() == null
                || origen.getSedeId() == null
                || origen.getAnioEscolarId().getId() == null
                || origen.getSedeId().getId() == null) {
            throw new RelacionAcademicaInvalidaException(
                    "El horario no tiene año escolar o sede");
        }

        Optional<Horario> borradorAbierto =
                horarioRepository
                        .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                                origen.getAnioEscolarId().getId(),
                                origen.getSedeId().getId(),
                                EstadoHorario.BORRADOR);

        if (borradorAbierto.isPresent()) {
            HorarioVersionResponse existente =
                    convertirAVersion(borradorAbierto.get());
            existente.setNuevo(false);
            return existente;
        }

        int siguienteVersion = horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                        origen.getAnioEscolarId().getId(),
                        origen.getSedeId().getId())
                .map(horario -> horario.getVersion() + 1)
                .orElse(origen.getVersion() + 1);

        Usuario usuario = usuarioRepository
                .findByCorreo(resolverCorreo(correoUsuario))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Usuario autenticado no identificado "
                                        + "para crear el borrador"));

        Horario borrador = new Horario();
        borrador.setAnioEscolarId(origen.getAnioEscolarId());
        borrador.setSedeId(origen.getSedeId());
        borrador.setVersion(siguienteVersion);
        borrador.setNombre(origen.getNombre());
        borrador.setEstado(EstadoHorario.BORRADOR);
        borrador.setFechaPublicacion(null);
        borrador.setCreadoPor(usuario);
        borrador.setNotas(
                "Borrador de mejora de la versión "
                        + origen.getVersion());

        Horario guardado = horarioRepository.save(borrador);

        for (ClaseProgramada clase : claseProgramadaRepository
                .findByHorarioId_Id(origen.getId())) {
            claseProgramadaRepository.save(copiarClase(clase, guardado));
        }

        registrarAuditoria(
                guardado,
                correoUsuario,
                RegistroAuditoriaService.ACCION_CREATE,
                null,
                datosHorario(guardado));

        HorarioVersionResponse response = convertirAVersion(guardado);
        response.setNuevo(true);
        return response;
    }

    // Elimina el borrador de mejora y deja el publicado como está.
    @Transactional
    public void descartarBorrador(
            UUID horarioId,
            String correoUsuario) {

        Horario borrador = obtenerHorario(horarioId);

        if (borrador.getEstado() != EstadoHorario.BORRADOR) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo se puede descartar un horario en estado "
                            + "BORRADOR. Estado actual: "
                            + (borrador.getEstado() == null
                            ? "DESCONOCIDO"
                            : borrador.getEstado().name()));
        }

        validarAnioAbierto(borrador);

        if (borrador.getAnioEscolarId() == null
                || borrador.getSedeId() == null
                || borrador.getAnioEscolarId().getId() == null
                || borrador.getSedeId().getId() == null) {
            throw new RelacionAcademicaInvalidaException(
                    "El horario no tiene año escolar o sede");
        }

        boolean hayPublicado = horarioRepository
                .findFirstByAnioEscolarId_IdAndSedeId_IdAndEstadoOrderByVersionDesc(
                        borrador.getAnioEscolarId().getId(),
                        borrador.getSedeId().getId(),
                        EstadoHorario.PUBLICADO)
                .isPresent();

        if (!hayPublicado) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo se puede descartar un borrador de mejora "
                            + "cuando el horario publicado sigue vigente");
        }

        Map<String, Object> datosAntes = datosHorario(borrador);

        registrarAuditoria(
                borrador,
                correoUsuario,
                RegistroAuditoriaService.ACCION_DELETE,
                datosAntes,
                null);

        conflictoRepository.eliminarPorHorario(borrador.getId());
        claseProgramadaRepository.eliminarPorHorario(borrador.getId());
        horarioRepository.delete(borrador);
    }

    @Transactional(readOnly = true)
    public List<HorarioVersionResponse> listarVersiones(
            UUID anioEscolarId) {

        if (anioEscolarId == null) {
            throw new IllegalArgumentException(
                    "El identificador del año escolar es obligatorio");
        }

        return horarioRepository
                .findByAnioEscolarId_IdOrderByVersionDesc(anioEscolarId)
                .stream()
                .sorted(Comparator
                        .comparing(
                                (Horario horario) ->
                                        horario.getSedeId() == null
                                                || horario.getSedeId().getNombre() == null
                                                ? ""
                                                : horario.getSedeId().getNombre(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(
                                Horario::getVersion,
                                Comparator.reverseOrder()))
                .map(this::convertirAVersion)
                .toList();
    }

    private void archivarPublicadosAnteriores(
            Horario publicado,
            String correoUsuario) {

        if (publicado.getAnioEscolarId() == null
                || publicado.getSedeId() == null
                || publicado.getAnioEscolarId().getId() == null
                || publicado.getSedeId().getId() == null) {
            return;
        }

        List<Horario> vigentes = horarioRepository
                .findByAnioEscolarId_IdAndSedeId_IdAndEstado(
                        publicado.getAnioEscolarId().getId(),
                        publicado.getSedeId().getId(),
                        EstadoHorario.PUBLICADO);

        for (Horario previo : vigentes) {
            if (previo.getId() == null
                    || previo.getId().equals(publicado.getId())) {
                continue;
            }

            Map<String, Object> datosAntes = datosHorario(previo);
            previo.setEstado(EstadoHorario.ARCHIVADO);
            Horario archivado = horarioRepository.save(previo);

            registrarAuditoria(
                    archivado,
                    correoUsuario,
                    RegistroAuditoriaService.ACCION_UPDATE,
                    datosAntes,
                    datosHorario(archivado));
        }
    }

    private void validarAnioAbierto(Horario horario) {
        if (horario.getAnioEscolarId() != null
                && horario.getAnioEscolarId().getEstado()
                == EstadoAnioEscolar.CERRADO) {
            throw new TransicionEstadoNoPermitidaException(
                    "Un año escolar cerrado no puede modificarse");
        }
    }

    private Horario obtenerHorario(UUID horarioId) {
        if (horarioId == null) {
            throw new IllegalArgumentException(
                    "El identificador del horario es obligatorio");
        }

        return horarioRepository
                .findById(horarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El horario no existe"));
    }

    private ClaseProgramada copiarClase(
            ClaseProgramada origen,
            Horario destino) {

        ClaseProgramada copia = new ClaseProgramada();
        copia.setHorarioId(destino);
        copia.setAsignacionAcademicaId(origen.getAsignacionAcademicaId());
        copia.setDia(origen.getDia());
        copia.setFranjaHorariaId(origen.getFranjaHorariaId());
        copia.setAulaId(origen.getAulaId());
        copia.setEsHoraExtra(origen.isEsHoraExtra());
        copia.setBloqueada(origen.isBloqueada());
        return copia;
    }

    private void registrarAuditoria(
            Horario horario,
            String correoUsuario,
            String accion,
            Map<String, Object> datosAntes,
            Map<String, Object> datosDespues) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(
                                resolverCorreo(correoUsuario))
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no identificado "
                                                + "para auditar la publicación"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "Horario",
                horario.getId(),
                datosAntes,
                datosDespues,
                null);
    }

    private String resolverCorreo(
            String correoUsuario) {

        if (correoUsuario != null
                && !correoUsuario.isBlank()) {

            return correoUsuario;
        }

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        return auth.getName();
    }

    private Map<String, Object> datosHorario(
            Horario horario) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        datos.put("horarioId", horario.getId());
        datos.put(
                "estado",
                horario.getEstado() == null
                        ? null
                        : horario.getEstado().name());
        datos.put(
                "fechaPublicacion",
                horario.getFechaPublicacion() == null
                        ? null
                        : horario.getFechaPublicacion().toString());
        datos.put("version", horario.getVersion());

        return datos;
    }

    private PublicarHorarioResponse convertirAResponse(
            Horario horario) {

        PublicarHorarioResponse response =
                new PublicarHorarioResponse();

        response.setHorarioId(horario.getId());
        response.setEstado(horario.getEstado());
        response.setFechaPublicacion(
                horario.getFechaPublicacion());
        response.setVersion(horario.getVersion());

        return response;
    }

    private HorarioVersionResponse convertirAVersion(
            Horario horario) {

        HorarioVersionResponse response =
                new HorarioVersionResponse();

        response.setId(horario.getId());
        response.setNombre(horario.getNombre());
        response.setVersion(horario.getVersion());
        response.setEstado(horario.getEstado());
        response.setFechaPublicacion(horario.getFechaPublicacion());
        response.setNotas(horario.getNotas());
        response.setNuevo(false);

        if (horario.getAnioEscolarId() != null) {
            response.setAnioEscolarId(
                    horario.getAnioEscolarId().getId());
        }

        if (horario.getSedeId() != null) {
            response.setSedeId(horario.getSedeId().getId());
            response.setSedeNombre(horario.getSedeId().getNombre());
        }

        return response;
    }
}
