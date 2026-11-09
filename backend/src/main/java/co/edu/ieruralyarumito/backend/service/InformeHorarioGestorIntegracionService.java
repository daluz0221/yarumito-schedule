package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class InformeHorarioGestorIntegracionService {

    private final InformeHorariosService informesService;

    public InformeHorarioGestorIntegracionService(
            InformeHorariosService informesService) {

        this.informesService = informesService;
    }

    /*
     * Contrato temporal de entrada.
     * No duplica entidades, repositorios ni servicios
     * del Gestor de Horarios.
     */
    public record ClaseGestor(
            UUID docenteId,
            String docenteNombre,
            UUID asignaturaId,
            String asignaturaNombre,
            String diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            UUID aulaId,
            String aulaNombre,
            boolean esHoraExtra
    ) {
    }

    public record TableroGestor(
            UUID horarioId,
            Integer versionHorario,
            String estadoHorario,
            UUID grupoId,
            String grado,
            String nombreGrupo,
            UUID anioEscolarId,
            String nombreAnioEscolar,
            String nombreSede,
            String jornada,
            List<ClaseGestor> clases
    ) {
    }

    public InformeHorariosResponse generarInforme(
            TableroGestor tablero,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        Objects.requireNonNull(
                tablero, "El tablero es obligatorio.");

        Objects.requireNonNull(
                filtros, "Los filtros son obligatorios.");

        if (tablero.horarioId() == null
                || tablero.versionHorario() == null
                || tablero.grupoId() == null) {

            throw new IllegalArgumentException(
                    "Faltan datos de identificacion del horario.");
        }

        String estado = validarEstado(tablero.estadoHorario());
        String jornada = validarJornada(tablero.jornada());

        List<ClaseGestor> origen = tablero.clases() == null
                ? List.of()
                : tablero.clases();

        List<InformeHorarioClaseDetalleResponse> detalle =
                origen.stream()
                        .map(clase -> convertir(
                                tablero, clase, estado, jornada))
                        .toList();

        return informesService.construirInforme(
                tablero.anioEscolarId(),
                tablero.nombreAnioEscolar(),
                nombreInstitucion,
                tablero.nombreSede(),
                filtros,
                detalle
        );
    }

    private InformeHorarioClaseDetalleResponse convertir(
            TableroGestor tablero,
            ClaseGestor clase,
            String estado,
            String jornada) {

        Objects.requireNonNull(
                clase, "No se permiten clases nulas.");

        return new InformeHorarioClaseDetalleResponse(
                tablero.horarioId(),
                tablero.versionHorario(),
                estado,
                clase.docenteId(),
                clase.docenteNombre(),
                tablero.grupoId(),
                tablero.grado(),
                tablero.nombreGrupo(),
                clase.asignaturaId(),
                clase.asignaturaNombre(),
                clase.diaSemana(),
                clase.horaInicio(),
                clase.horaFin(),
                jornada,
                clase.aulaId(),
                clase.aulaNombre(),
                clase.esHoraExtra()
        );
    }

    private String validarEstado(String estado) {

        if (estado == null) {
            throw new IllegalArgumentException(
                    "El estado del horario es obligatorio.");
        }

        String valor = estado.trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of("BORRADOR", "PUBLICADO", "ARCHIVADO")
                .contains(valor)) {

            throw new IllegalArgumentException(
                    "Estado incompatible con el ER V2.1: "
                            + estado);
        }

        return valor;
    }

    private String validarJornada(String jornada) {

        if (jornada == null) {
            throw new IllegalArgumentException(
                    "La jornada es obligatoria.");
        }

        String valor = jornada.trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of("REGULAR", "CONTRAJORNADA")
                .contains(valor)) {

            throw new IllegalArgumentException(
                    "Jornada no reconocida: " + jornada);
        }

        return valor;
    }


    public InformeHorariosResponse generarInformePorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros,
            java.util.function.Function<UUID, TableroGestor> consultarGrupo) {

        Objects.requireNonNull(
                grupoId, "El grupo es obligatorio.");

        Objects.requireNonNull(
                consultarGrupo, "La consulta al Gestor es obligatoria.");

        TableroGestor tablero = Objects.requireNonNull(
                consultarGrupo.apply(grupoId),
                "El Gestor no devolvio el tablero.");

        if (!grupoId.equals(tablero.grupoId())) {
            throw new IllegalStateException(
                    "El grupo consultado no coincide con el solicitado.");
        }

        return generarInforme(
                tablero,
                nombreInstitucion,
                filtros
        );
    }


    /**
     * HU-04: construye el informe institucional
     * a partir de los tableros de varios grupos.
     *
     * Reutiliza la consulta del Gestor y el
     * procesamiento existente de informes.
     */
    public InformeHorariosResponse generarInformeInstitucional(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros,
            java.util.function.Function<UUID, TableroGestor> consultarGrupo) {

        Objects.requireNonNull(grupoIds,
                "La lista de grupos es obligatoria.");
        Objects.requireNonNull(filtros,
                "Los filtros son obligatorios.");
        Objects.requireNonNull(consultarGrupo,
                "La consulta del Gestor es obligatoria.");

        if (grupoIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un grupo.");
        }

        List<TableroGestor> tableros = grupoIds.stream()
                .distinct()
                .map(grupoId -> {
                    Objects.requireNonNull(grupoId,
                            "No se permiten identificadores nulos.");

                    TableroGestor tablero = Objects.requireNonNull(
                            consultarGrupo.apply(grupoId),
                            "El Gestor no devolvio el tablero.");

                    if (!grupoId.equals(tablero.grupoId())) {
                        throw new IllegalStateException(
                                "El grupo consultado no coincide.");
                    }

                    return tablero;
                })
                .toList();

        TableroGestor primero = tableros.get(0);

        List<InformeHorarioClaseDetalleResponse> detalle =
                tableros.stream()
                        .flatMap(tablero -> {

                            if (!Objects.equals(
                                    primero.anioEscolarId(),
                                    tablero.anioEscolarId())
                                    || !Objects.equals(
                                    primero.nombreSede(),
                                    tablero.nombreSede())) {

                                throw new IllegalStateException(
                                        "No se pueden mezclar grupos "
                                                + "de diferentes años o sedes.");
                            }

                            String estado =
                                    validarEstado(tablero.estadoHorario());

                            String jornada =
                                    validarJornada(tablero.jornada());

                            List<ClaseGestor> clases =
                                    tablero.clases() == null
                                            ? List.of()
                                            : tablero.clases();

                            return clases.stream()
                                    .map(clase -> convertir(
                                            tablero,
                                            clase,
                                            estado,
                                            jornada));
                        })
                        .toList();

        return informesService.construirInforme(
                primero.anioEscolarId(),
                primero.nombreAnioEscolar(),
                nombreInstitucion,
                primero.nombreSede(),
                filtros,
                detalle
        );
    }


    /**
     * Genera el informe institucional exclusivamente
     * con horarios PUBLICADOS, segun el ER V2.1.
     */
    public InformeHorariosResponse generarInformePublicados(
            List<UUID> grupoIds,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros,
            java.util.function.Function<UUID, TableroGestor> consultarGrupo) {

        return generarInformeInstitucional(
                grupoIds,
                nombreInstitucion,
                filtros,
                grupoId -> {
                    TableroGestor tablero =
                            Objects.requireNonNull(
                                    consultarGrupo.apply(grupoId),
                                    "El Gestor no devolvio el tablero."
                            );

                    String estado =
                            validarEstado(tablero.estadoHorario());

                    if (!"PUBLICADO".equals(estado)) {
                        throw new IllegalStateException(
                                "El grupo " + grupoId
                                        + " no tiene un horario PUBLICADO "
                                        + "en la respuesta del Gestor."
                        );
                    }

                    return tablero;
                }
        );
    }

}
