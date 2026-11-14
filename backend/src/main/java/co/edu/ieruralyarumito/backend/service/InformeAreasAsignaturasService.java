
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAsignaturaDetalleResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * HU-04 / SCRUM-31.
 *
 * Servicio de consultas para el informe
 * de áreas y asignaturas institucionales.
 *
 * No modifica registros ni crea entidades.
 */
@Service
public class InformeAreasAsignaturasService {

    private final AreaRepository areaRepository;
    private final AsignaturaRepository asignaturaRepository;

    public InformeAreasAsignaturasService(
            AreaRepository areaRepository,
            AsignaturaRepository asignaturaRepository) {

        this.areaRepository = areaRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    /**
     * Genera el informe aplicando filtros opcionales.
     *
     * areaId null = todas las áreas.
     * activa null = áreas activas e inactivas.
     *
     * El estado activa corresponde al área,
     * no al estado individual de sus asignaturas.
     */
    @Transactional(readOnly = true)
    public InformeAreasAsignaturasResponse generarInforme(
            UUID areaId,
            Boolean activa) {

        Specification<Area> filtros =
                (root, query, cb) -> {

                    var condiciones =
                            new ArrayList<jakarta.persistence.criteria.Predicate>();

                    if (areaId != null) {
                        condiciones.add(
                                cb.equal(root.get("id"), areaId)
                        );
                    }

                    if (activa != null) {
                        condiciones.add(
                                cb.equal(root.get("activa"), activa)
                        );
                    }

                    return cb.and(condiciones.toArray(
                            new jakarta.persistence.criteria.Predicate[0]
                    ));
                };

        /*
         * Primero recuperamos las áreas.
         * Así conservamos incluso aquellas que
         * todavía no tienen asignaturas.
         */
        List<Area> areasEncontradas =
                areaRepository.findAll(filtros);

        Map<UUID, Area> areasUnicas =
                areasEncontradas.stream()
                        .collect(Collectors.toMap(
                                Area::getId,
                                area -> area,
                                (primera, repetida) -> primera,
                                LinkedHashMap::new
                        ));

        if (areasUnicas.isEmpty()) {
            return new InformeAreasAsignaturasResponse(
                    0, 0, List.of()
            );
        }

        /*
         * Consultamos solamente las asignaturas
         * pertenecientes a las áreas seleccionadas.
         *
         * No recuperamos indiscriminadamente
         * todas las asignaturas institucionales.
         */
        Specification<Asignatura> filtroAsignaturas =
                (root, query, cb) ->
                        root.get("area")
                                .get("id")
                                .in(areasUnicas.keySet());

        List<Asignatura> asignaturasEncontradas =
                asignaturaRepository.findAll(filtroAsignaturas);

        /*
         * Agrupamos por área y por UUID de asignatura
         * para impedir duplicaciones en los resultados.
         */
        Map<UUID, Map<UUID, Asignatura>> asignaturasPorArea =
                new HashMap<>();

        for (Asignatura asignatura : asignaturasEncontradas) {

            UUID idArea = asignatura.getArea().getId();

            asignaturasPorArea
                    .computeIfAbsent(
                            idArea,
                            id -> new LinkedHashMap<>()
                    )
                    .putIfAbsent(
                            asignatura.getId(),
                            asignatura
                    );
        }

        List<InformeAreaDetalleResponse> detalles =
                new ArrayList<>();

        long totalAsignaturas = 0;

        for (Area area : areasUnicas.values()) {

            Map<UUID, Asignatura> asignaturas =
                    asignaturasPorArea.getOrDefault(
                            area.getId(),
                            Map.of()
                    );

            List<InformeAsignaturaDetalleResponse> detalleAsignaturas =
                    asignaturas.values().stream()
                            .map(this::convertirAsignatura)
                            .sorted(Comparator
                                    .comparing(
                                            InformeAsignaturaDetalleResponse::nombre,
                                            String.CASE_INSENSITIVE_ORDER
                                    )
                                    .thenComparing(
                                            InformeAsignaturaDetalleResponse::asignaturaId
                                    ))
                            .toList();

            totalAsignaturas =
                    Math.addExact(
                            totalAsignaturas,
                            detalleAsignaturas.size()
                    );

            detalles.add(
                    new InformeAreaDetalleResponse(
                            area.getId(),
                            area.getNombre(),
                            area.getCodigo(),
                            area.isActiva(),
                            area.isObligatoria(),
                            area.isSoloMedia(),
                            detalleAsignaturas.size(),
                            detalleAsignaturas
                    )
            );
        }

        detalles.sort(Comparator
                .comparing(
                        InformeAreaDetalleResponse::nombre,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        InformeAreaDetalleResponse::areaId
                ));

        return new InformeAreasAsignaturasResponse(
                detalles.size(),
                totalAsignaturas,
                detalles
        );
    }

    /**
     * Convierte una asignatura a datos
     * exclusivamente informativos.
     */
    private InformeAsignaturaDetalleResponse convertirAsignatura(
            Asignatura asignatura) {

        return new InformeAsignaturaDetalleResponse(
                asignatura.getId(),
                asignatura.getNombre(),
                asignatura.getCodigo(),
                asignatura.isActiva(),
                asignatura.isEsMediaTecnica(),
                asignatura.isExigeIdoneidadEstricta(),
                asignatura.isRequiereDocenteExclusivo()
        );
    }
}
