package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Optional;

/**
 * HU-04 / SCRUM-31.
 *
 * Organiza las clases para construir una parrilla
 * independiente por grupo y version de horario.
 *
 * No modifica ni consulta PostgreSQL.
 */
@Service
public class InformeHorarioPorGrupoService {

    private static final List<String> DIAS = List.of(
            "LUNES",
            "MARTES",
            "MIERCOLES",
            "JUEVES",
            "VIERNES"
    );

    public List<HorarioGrupo> organizar(
            List<InformeHorarioClaseDetalleResponse> clases) {

        if (clases == null || clases.isEmpty()) {
            return List.of();
        }

        Map<ClaveGrupo, List<InformeHorarioClaseDetalleResponse>>
                agrupadas = new LinkedHashMap<>();

        for (InformeHorarioClaseDetalleResponse clase : clases) {

            Objects.requireNonNull(
                    clase,
                    "No se permiten clases nulas."
            );

            if (clase.grupoId() == null
                    || clase.horarioId() == null
                    || clase.versionHorario() == null) {

                throw new IllegalArgumentException(
                        "La clase debe identificar grupo, "
                                + "horario y version."
                );
            }

            ClaveGrupo clave = new ClaveGrupo(
                    clase.grupoId(),
                    clase.horarioId(),
                    clase.versionHorario()
            );

            agrupadas.computeIfAbsent(
                    clave,
                    ignorada -> new ArrayList<>()
            ).add(clase);
        }

        List<HorarioGrupo> resultado = new ArrayList<>();

        for (Map.Entry<ClaveGrupo,
                List<InformeHorarioClaseDetalleResponse>>
                entrada : agrupadas.entrySet()) {

            List<InformeHorarioClaseDetalleResponse> ordenadas =
                    entrada.getValue()
                            .stream()
                            .sorted(
                                    Comparator
                                            .comparingInt(
                                                    (InformeHorarioClaseDetalleResponse c) ->
                                                            ordenDia(c.diaSemana())
                                            )
                                            .thenComparing(
                                                    InformeHorarioClaseDetalleResponse::horaInicio,
                                                    Comparator.nullsLast(
                                                            Comparator.naturalOrder()
                                                    )
                                            )
                                            .thenComparing(
                                                    InformeHorarioClaseDetalleResponse::horaFin,
                                                    Comparator.nullsLast(
                                                            Comparator.naturalOrder()
                                                    )
                                            )
                            )
                            .toList();

            List<ConflictoGrupo> conflictos =
                    detectarConflictos(ordenadas);

            InformeHorarioClaseDetalleResponse primera =
                    ordenadas.get(0);

            resultado.add(
                    new HorarioGrupo(
                            entrada.getKey().grupoId(),
                            primera.grado(),
                            primera.nombreGrupo(),
                            entrada.getKey().horarioId(),
                            entrada.getKey().version(),
                            ordenadas,
                            conflictos
                    )
            );
        }

        resultado.sort(
                Comparator
                        .comparing(
                                HorarioGrupo::grado,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .thenComparing(
                                HorarioGrupo::nombreGrupo,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .thenComparing(
                                HorarioGrupo::versionHorario
                        )
                        .thenComparing(
                                HorarioGrupo::horarioId
                        )
        );

        return List.copyOf(resultado);
    }


    /**
     * Selecciona el horario publicado de un grupo.
     *
     * No modifica la agrupacion existente.
     * No consulta ni modifica PostgreSQL.
     *
     * Si hay varias versiones publicadas, no elige
     * una arbitrariamente.
     */
    public Optional<HorarioGrupo> seleccionarPublicadoPorGrupo(
            List<InformeHorarioClaseDetalleResponse> clases,
            UUID grupoId) {

        if (grupoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del grupo es obligatorio."
            );
        }

        List<HorarioGrupo> publicados = organizar(clases)
                .stream()
                .filter(grupo ->
                        grupoId.equals(grupo.grupoId())
                )
                .filter(grupo ->
                        grupo.clases().stream().allMatch(
                                clase -> "PUBLICADO".equalsIgnoreCase(
                                        clase.estadoHorario()
                                )
                        )
                )
                .toList();

        if (publicados.size() > 1) {
            throw new IllegalStateException(
                    "El grupo tiene varios horarios publicados. "
                            + "Debe identificarse la version vigente."
            );
        }

        return publicados.stream().findFirst();
    }

    private List<ConflictoGrupo> detectarConflictos(
            List<InformeHorarioClaseDetalleResponse> clases) {

        Map<ClaveFranja, List<InformeHorarioClaseDetalleResponse>>
                porFranja = new LinkedHashMap<>();

        for (InformeHorarioClaseDetalleResponse clase : clases) {

            ClaveFranja clave = new ClaveFranja(
                    clase.jornada(),
                    clase.diaSemana(),
                    clase.horaInicio(),
                    clase.horaFin()
            );

            porFranja.computeIfAbsent(
                    clave,
                    ignorada -> new ArrayList<>()
            ).add(clase);
        }

        List<ConflictoGrupo> conflictos = new ArrayList<>();

        for (Map.Entry<ClaveFranja,
                List<InformeHorarioClaseDetalleResponse>>
                entrada : porFranja.entrySet()) {

            if (entrada.getValue().size() > 1) {

                conflictos.add(
                        new ConflictoGrupo(
                                entrada.getKey().jornada(),
                                entrada.getKey().dia(),
                                entrada.getKey().inicio(),
                                entrada.getKey().fin(),
                                entrada.getValue()
                        )
                );
            }
        }

        return List.copyOf(conflictos);
    }

    private int ordenDia(String dia) {

        if (dia == null) {
            return DIAS.size();
        }

        int indice = DIAS.indexOf(
                dia.trim()
                        .toUpperCase(java.util.Locale.ROOT)
                        .replace('É', 'E')
        );

        return indice < 0 ? DIAS.size() : indice;
    }

    private record ClaveGrupo(
            UUID grupoId,
            UUID horarioId,
            Integer version
    ) {
    }

    private record ClaveFranja(
            String jornada,
            String dia,
            LocalTime inicio,
            LocalTime fin
    ) {
    }

    public record HorarioGrupo(
            UUID grupoId,
            String grado,
            String nombreGrupo,
            UUID horarioId,
            Integer versionHorario,
            List<InformeHorarioClaseDetalleResponse> clases,
            List<ConflictoGrupo> conflictos
    ) {
        public HorarioGrupo {
            clases = clases == null
                    ? List.of()
                    : List.copyOf(clases);

            conflictos = conflictos == null
                    ? List.of()
                    : List.copyOf(conflictos);
        }

        public boolean tieneConflictos() {
            return !conflictos.isEmpty();
        }
    }

    public record ConflictoGrupo(
            String jornada,
            String dia,
            LocalTime inicio,
            LocalTime fin,
            List<InformeHorarioClaseDetalleResponse> clases
    ) {
        public ConflictoGrupo {
            clases = clases == null
                    ? List.of()
                    : List.copyOf(clases);
        }
    }
}
