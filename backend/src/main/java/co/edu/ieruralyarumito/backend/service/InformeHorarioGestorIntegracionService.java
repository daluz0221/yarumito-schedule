package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Integra directamente los informes institucionales
 * con el Gestor de Horarios existente.
 *
 * Reutiliza HorarioService como fuente oficial.
 * No duplica entidades, repositorios ni persistencia
 * del Gestor de Horarios.
 */
@Service
public class InformeHorarioGestorIntegracionService {

    private final HorarioService horarioService;
    private final InformeHorariosService informesService;

    public InformeHorarioGestorIntegracionService(
            HorarioService horarioService,
            InformeHorariosService informesService) {

        this.horarioService = Objects.requireNonNull(horarioService);
        this.informesService = Objects.requireNonNull(informesService);
    }

    /**
     * Genera el informe institucional para uno o varios grupos.
     *
     * Consulta directamente el tablero oficial del Gestor.
     */
    public InformeHorariosResponse generarInstitucional(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        return generarInforme(
                grupoIds,
                nombreInstitucion,
                filtros,
                false
        );
    }

    /**
     * Genera el informe exclusivamente con horarios PUBLICADOS.
     */
    public InformeHorariosResponse generarPublicados(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        return generarInforme(
                grupoIds,
                nombreInstitucion,
                filtros,
                true
        );
    }

    /**
     * Obtiene las franjas oficiales de descanso del grupo.
     *
     * No calcula ni inventa descansos.
     */
    public List<FranjaHorariaResponse> obtenerDescansosOficiales(
            UUID grupoId) {

        Objects.requireNonNull(
                grupoId,
                "El grupo es obligatorio."
        );

        HorarioGrupoResponse tablero =
                obtenerTablero(grupoId);

        if (tablero.getFranjas() == null) {
            return List.of();
        }

        return tablero.getFranjas()
                .stream()
                .filter(Objects::nonNull)
                .filter(FranjaHorariaResponse::isEsDescanso)
                .filter(franja ->
                        franja.getHoraInicio() != null
                                && franja.getHoraFin() != null)
                .sorted(java.util.Comparator.comparing(
                        FranjaHorariaResponse::getHoraInicio
                ))
                .toList();
    }

    private InformeHorariosResponse generarInforme(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros,
            boolean soloPublicados) {

        Objects.requireNonNull(
                grupoIds,
                "La lista de grupos es obligatoria."
        );

        Objects.requireNonNull(
                filtros,
                "Los filtros son obligatorios."
        );

        if (grupoIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un grupo."
            );
        }

        List<HorarioGrupoResponse> tableros =
                grupoIds.stream()
                        .distinct()
                        .map(grupoId -> {
                            Objects.requireNonNull(
                                    grupoId,
                                    "No se permiten identificadores nulos."
                            );

                            HorarioGrupoResponse tablero =
                                    obtenerTablero(grupoId);

                            if (!grupoId.equals(tablero.getGrupoId())) {
                                throw new IllegalStateException(
                                        "El grupo consultado no coincide."
                                );
                            }

                            if (soloPublicados
                                    && !"PUBLICADO".equals(
                                    validarEstado(tablero))) {

                                throw new IllegalStateException(
                                        "El grupo "
                                                + grupoId
                                                + " no tiene un horario PUBLICADO."
                                );
                            }

                            return tablero;
                        })
                        .toList();

        HorarioGrupoResponse primero =
                tableros.get(0);

        List<InformeHorarioClaseDetalleResponse> detalle =
                tableros.stream()
                        .flatMap(tablero -> {

                            validarMismoContexto(
                                    primero,
                                    tablero
                            );

                            String estado =
                                    validarEstado(tablero);

                            String jornada =
                                    normalizarJornada(
                                            tablero.getTurnoNombre()
                                    );

                            List<ClaseProgramadaResponse> clases =
                                    tablero.getClasesProgramadas() == null
                                            ? List.of()
                                            : tablero.getClasesProgramadas();

                            return clases.stream()
                                    .map(clase ->
                                            convertirClase(
                                                    tablero,
                                                    clase,
                                                    estado,
                                                    jornada
                                            )
                                    );
                        })
                        .toList();

        return informesService.construirInforme(
                primero.getAnioEscolarId(),
                String.valueOf(
                        primero.getAnioEscolar()
                ),
                nombreInstitucion,
                primero.getSedeNombre(),
                filtros,
                detalle
        );
    }

    private HorarioGrupoResponse obtenerTablero(
            UUID grupoId) {

        return Objects.requireNonNull(
                horarioService.obtenerTablero(grupoId),
                "El Gestor no devolvio el tablero."
        );
    }

    private InformeHorarioClaseDetalleResponse convertirClase(
            HorarioGrupoResponse tablero,
            ClaseProgramadaResponse clase,
            String estado,
            String jornada) {

        Objects.requireNonNull(
                clase,
                "No se permiten clases nulas."
        );

        return new InformeHorarioClaseDetalleResponse(
                tablero.getHorarioId(),
                tablero.getVersionHorario(),
                estado,
                clase.getDocenteId(),
                clase.getDocenteNombre(),
                tablero.getGrupoId(),
                tablero.getGradoNombre(),
                tablero.getGrupoCodigo(),
                clase.getAsignaturaId(),
                clase.getAsignaturaNombre(),
                clase.getDia() == null
                        ? null
                        : clase.getDia().name(),
                clase.getHoraInicio(),
                clase.getHoraFin(),
                jornada,
                clase.getAulaId(),
                clase.getAulaNombre(),
                clase.isEsHoraExtra()
        );
    }

    private String validarEstado(
            HorarioGrupoResponse tablero) {

        if (tablero.getEstadoHorario() == null) {
            throw new IllegalArgumentException(
                    "El estado del horario es obligatorio."
            );
        }

        String estado =
                tablero.getEstadoHorario()
                        .name()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!Set.of(
                "BORRADOR",
                "PUBLICADO",
                "ARCHIVADO"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "Estado incompatible con el ER V2.1: "
                            + estado
            );
        }

        return estado;
    }

    private String normalizarJornada(
            String turno) {

        if (turno == null || turno.isBlank()) {
            throw new IllegalStateException(
                    "El Gestor no informo el turno."
            );
        }

        String valor =
                Normalizer.normalize(
                                turno.trim()
                                        .toUpperCase(Locale.ROOT),
                                Normalizer.Form.NFD
                        )
                        .replaceAll("\\p{M}", "");

        return switch (valor) {
            case "REGULAR", "MANANA", "TARDE" ->
                    "REGULAR";

            case "CONTRAJORNADA" ->
                    "CONTRAJORNADA";

            default ->
                    throw new IllegalStateException(
                            "Turno no reconocido: "
                                    + turno
                    );
        };
    }

    private void validarMismoContexto(
            HorarioGrupoResponse primero,
            HorarioGrupoResponse actual) {

        if (!Objects.equals(
                primero.getAnioEscolarId(),
                actual.getAnioEscolarId())
                || !Objects.equals(
                primero.getSedeNombre(),
                actual.getSedeNombre())) {

            throw new IllegalStateException(
                    "No se pueden mezclar grupos "
                            + "de diferentes años o sedes."
            );
        }

        if (actual.getHorarioId() == null
                || actual.getGrupoId() == null) {

            throw new IllegalArgumentException(
                    "Faltan datos de identificacion del horario."
            );
        }
    }
}
