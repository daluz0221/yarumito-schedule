package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.HorarioGrupoResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;

/**
 * HU-04 / SCRUM-31.
 *
 * Conecta el Gestor de Horarios existente
 * con los informes institucionales.
 *
 * No duplica consultas ni persistencia.
 */
@Service
public class InformeHorarioJuanCamiloBridgeService {

    private final HorarioService horarioService;
    private final InformeHorarioGestorIntegracionService integracion;

    public InformeHorarioJuanCamiloBridgeService(
            HorarioService horarioService,
            InformeHorarioGestorIntegracionService integracion) {

        this.horarioService = horarioService;
        this.integracion = integracion;
    }

    public InformeHorariosResponse generarPublicados(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        return integracion.generarInformePublicados(
                grupoIds,
                nombreInstitucion,
                filtros,
                this::consultarGrupo
        );
    }

    private InformeHorarioGestorIntegracionService.TableroGestor
    consultarGrupo(UUID grupoId) {

        // Reutiliza directamente el servicio de Juan Camilo.
        HorarioGrupoResponse tablero = Objects.requireNonNull(
                horarioService.obtenerTablero(grupoId),
                "El Gestor no devolvio el tablero."
        );

        String estado = tablero.getEstadoHorario() == null
                ? null
                : tablero.getEstadoHorario().name();

        List<InformeHorarioGestorIntegracionService.ClaseGestor>
                clases = tablero.getClasesProgramadas() == null
                ? List.of()
                : tablero.getClasesProgramadas()
                .stream()
                .map(this::convertirClase)
                .toList();

        return new InformeHorarioGestorIntegracionService.TableroGestor(
                tablero.getHorarioId(),
                tablero.getVersionHorario(),
                estado,
                tablero.getGrupoId(),
                tablero.getGradoNombre(),
                tablero.getGrupoCodigo(),
                tablero.getAnioEscolarId(),
                String.valueOf(tablero.getAnioEscolar()),
                tablero.getSedeNombre(),
                normalizarJornada(tablero.getTurnoNombre()),
                clases
        );
    }

    private InformeHorarioGestorIntegracionService.ClaseGestor
    convertirClase(ClaseProgramadaResponse clase) {

        return new InformeHorarioGestorIntegracionService.ClaseGestor(
                clase.getDocenteId(),
                clase.getDocenteNombre(),
                clase.getAsignaturaId(),
                clase.getAsignaturaNombre(),
                clase.getDia() == null
                        ? null
                        : clase.getDia().name(),
                clase.getHoraInicio(),
                clase.getHoraFin(),
                clase.getAulaId(),
                clase.getAulaNombre(),
                clase.isEsHoraExtra()
        );
    }

    private String normalizarJornada(String turno) {

        if (turno == null || turno.isBlank()) {
            throw new IllegalStateException(
                    "El Gestor no informo el turno.");
        }

        String valor = Normalizer.normalize(
                turno.trim().toUpperCase(Locale.ROOT),
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return switch (valor) {
            case "REGULAR", "MANANA", "TARDE" -> "REGULAR";
            case "CONTRAJORNADA" -> "CONTRAJORNADA";
            default -> throw new IllegalStateException(
                    "Turno no reconocido: " + turno);
        };
    }


    /**
     * HU-04: informe institucional de horarios.
     * Reutiliza la consulta del Gestor de Horarios.
     * Los filtros determinan el estado solicitado.
     */
    public InformeHorariosResponse generarInstitucional(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        return integracion.generarInformeInstitucional(
                grupoIds,
                nombreInstitucion,
                filtros,
                this::consultarGrupo
        );
    }


    /**
     * Obtiene las franjas de descanso oficiales
     * del turno correspondiente al grupo.
     *
     * Reutiliza el Gestor de Horarios.
     * No calcula descansos ni consulta PostgreSQL
     * directamente desde HU-04.
     */
    public List<FranjaHorariaResponse> obtenerDescansosOficiales(
            UUID grupoId) {

        Objects.requireNonNull(
                grupoId,
                "El grupo es obligatorio."
        );

        HorarioGrupoResponse tablero =
                Objects.requireNonNull(
                        horarioService.obtenerTablero(grupoId),
                        "El Gestor no devolvio el tablero."
                );

        if (tablero.getFranjas() == null) {
            return List.of();
        }

        return tablero.getFranjas().stream()
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

}
